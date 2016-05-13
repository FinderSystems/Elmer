package pl.finder.elmer;

import static java.time.Duration.ZERO;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static pl.finder.elmer.extensions.DurationExtensions.isGreaterThan;
import static pl.finder.elmer.extensions.LoggerExtensions.debug;
import static pl.finder.elmer.extensions.LoggerExtensions.trace;
import static pl.finder.elmer.stream.MapCollectors.toImmutableMap;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import pl.finder.elmer.consumer.MessageConsumer;
import pl.finder.elmer.extensions.LoggerExtensions;
import pl.finder.elmer.model.Message;
import pl.finder.elmer.model.MessageRejectionOption;
import pl.finder.elmer.model.ReceivedMessageContext;
import pl.finder.elmer.model.SubscriptionConfig;
import pl.finder.elmer.serialization.MessageSerializer;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
final class InternalConsumer<TMessage> implements Consumer, AutoCloseable {
    private final BlockingQueue<Object> queue;
    private final SubscriptionConfig config;
    private final MessageSerializer serializer;
    private final MessageConsumer<TMessage> consumer;
    private final Channel channel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CountDownLatch shutdown = new CountDownLatch(1);
    private final ExecutorService executor;
    private final Duration shutdownTimeout;

    private String consumerTag;

    @Override
    public void handleConsumeOk(final String consumerTag) {
        log.info(String.format("Consumer '%s' created", consumerTag));
        this.consumerTag = consumerTag;
    }

    @Override
    public void handleCancelOk(final String consumerTag) {
        log.warn(String.format("Consumer '%s' received cancellation singal", consumerTag));
        running.set(false);
    }

    @Override
    public void handleCancel(final String consumerTag) throws IOException {
        log.warn(String.format("Consumer '%s' received cancellation singal", consumerTag));
        running.set(false);
    }

    @Override
    public void handleRecoverOk(final String consumerTag) {
        log.info(String.format("Consumer '%s' recovered", consumerTag));
        this.consumerTag = consumerTag;
    }

    @Override
    public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException sig) {

    }

    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, final BasicProperties properties,
            final byte[] body)
            throws IOException {
        try {
            debug(log, () -> String.format("Consumer: '%s' received message {id: %s, deliveryTag: %d, exchange: '%s'}",
                    consumerTag, properties.getMessageId(), envelope.getDeliveryTag(), envelope.getExchange()));
            final MessageEvent<TMessage> event = createMessageEvent(consumerTag, envelope, properties, body);
            queue.offer(event);
        } catch (final Exception e) {
            log.error(String.format("Error while handling delivery of '%s'", properties.getMessageId()), e);
        }
    }

    private MessageEvent<TMessage> createMessageEvent(final String consumerTag, final Envelope envelope,
            final BasicProperties properties, final byte[] body) {
        final TMessage deserializedBody = serializer.deserialize(body, consumer.messageType());
        final Message<TMessage> message = Message.<TMessage> builder()
                .id(properties.getMessageId())
                .correlationId(properties.getCorrelationId())
                .replyTo(properties.getReplyTo())
                .headers(properties.getHeaders() != null ? properties.getHeaders().entrySet().stream().collect(
                        toImmutableMap(it -> it.getKey(), it -> it.getValue().toString())) : ImmutableMap.of())
                .timestamp(properties.getTimestamp() != null ?
                        Instant.ofEpochMilli(properties.getTimestamp().getTime()) : null)
                .body(deserializedBody)
                .build();
        final ReceivedMessageContext context = RabbitReceivedMessageContext.buildFrom(envelope)
                .confirmUsing(onConfirm())
                .rejectUsing(onReject())
                .build(consumerTag);
        return MessageEvent.of(message, context);
    }

    private java.util.function.Consumer<Long> onConfirm() {
        if (config.autoAckEnabled()) {
            return deliveryTag -> {
            };
        }
        return deliveryTag -> {
            final ConfirmationEvent event = ConfirmationEvent.of(deliveryTag);
            queue.offer(event);
        };
    }

    private BiConsumer<Long, MessageRejectionOption> onReject() {
        return (deliveryTag, options) -> {
            final RejectionEvent event = RejectionEvent.of(deliveryTag, options);
            queue.offer(event);
        };
    }

    @Override
    public void close() {
        closeChannel();
        stop();
        executor.shutdown();
    }

    private void stop() {
        if (running.get()) {
            running.set(false);
            waitUntilShutdownComplete();
        }
    }

    private void waitUntilShutdownComplete() {
        if (isGreaterThan(shutdownTimeout, ZERO)) {
            try {
                shutdown.await(shutdownTimeout.toMillis(), MILLISECONDS);
            } catch (final InterruptedException e) {
                log.warn(String.format("Consumer: '%s' shutdown has been interrupted", consumerTag), e);
            }
        }
    }

    private void closeChannel() {
        try {
            channel.close();
        } catch (final IOException | TimeoutException e) {
            log.error(String.format("Error while closing channel of consumer '%s'", consumerTag), e);
        }
    }

    private void run() {
        while (running.get()) {
            try {
                final Object order = queue.poll(Duration.ofSeconds(5).toMillis(), MILLISECONDS);
                trace(log, () -> String.format("Processing: %s", order));
                // TODO
            } catch (final InterruptedException e) {
                log.info("Consumption interrupted");
            }
        }
        debug(log, () -> String.format("Consumer: '%s' consumption stopped", consumerTag));
    }

    private void confirm(final long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (final IOException e) {
            log.error(String.format("Error while sending ACK signal of %d", deliveryTag), e);
        }
    }

    private void reject(final long deliveryTag, final MessageRejectionOption options) {
        try {
            channel.basicReject(deliveryTag, options.requeue());
        } catch (final IOException e) {
            log.error(String.format("Error while rejecting %d using: %s", deliveryTag, options), e);
        }
    }

    private void safeDispatch(final Message<TMessage> message, final ReceivedMessageContext context) {
        try {
            consumer.onMessage(message, context);
        } catch (final Exception e) {
            log.error(String.format("Error while dispatching: %s", message), e);
        }
    }

    @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @ToString
    private static class ConfirmationEvent {
        private final long deliveryTag;
    }

    @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @ToString
    private static class RejectionEvent {
        private final long deliveryTag;
        private final MessageRejectionOption options;
    }

    @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @ToString
    private static class MessageEvent<T> {
        private final Message<T> message;
        private final ReceivedMessageContext context;
    }
}
