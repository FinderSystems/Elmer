package pl.finder.elmer;

import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.finder.elmer.commons.Durations;
import pl.finder.elmer.configuration.RabbitMqConfig;
import pl.finder.elmer.configuration.RabbitMqConfig.SerializationConfig;
import pl.finder.elmer.core.PublishingException;
import pl.finder.elmer.publication.DeliveryMode;
import pl.finder.elmer.publication.MessagePublisher;
import pl.finder.elmer.publication.MultiPublishOptions;
import pl.finder.elmer.publication.PublishOptions;
import pl.finder.elmer.publication.QueuePublisher;
import pl.finder.elmer.serialization.Message;
import pl.finder.elmer.serialization.MessageSerializer;

@Slf4j
final class RabbitMessagePublisher implements MessagePublisher {
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final RabbitMqConfig config;
    private final Executor publishingExecutor;
    private final MessageSerializer serializer;
    private final Supplier<Channel> channelFactory;

    private Channel channel;

    RabbitMessagePublisher(final RabbitMqConfig config,
            final Executor publishingExecutor,
            final MessageSerializer serializer,
            final Supplier<Channel> channelFactory) {
        this.config = config;
        this.publishingExecutor = publishingExecutor;
        this.serializer = serializer;
        this.channelFactory = channelFactory;
    }

    @Override
    public <T> CompletionStage<MessagePublisher> publish(final PublishOptions options, final T message) {
        checkIsRunning();
        return supplyAsync(() -> {
            final Channel channel = publishChannel();
            publish(channel, options, message);
            return this;
        }, publishingExecutor);
    }

    @Override
    public <T> CompletionStage<MessagePublisher> publishAll(final MultiPublishOptions<T> options,
            final Collection<T> messages) {
        checkIsRunning();
        return supplyAsync(() -> {
            final Channel channel = publishChannel();
            publishAll(channel, options, messages);
            return this;
        }, publishingExecutor);
    }

    @Override
    public <T> QueuePublisher publishFrom(final MultiPublishOptions<T> options, final BlockingQueue<T> messageQueue) {
        final PublishingWorker<T> worker = new PublishingWorker<>(options, messageQueue, channelFactory);
        final Thread workerThread = new Thread(worker::run);
        workerThread.setName("QueuePublishingWorker");
        workerThread.start();
        return worker;
    }

    @Override
    public void close() {
        checkIsRunning();
        running.set(false);
        tryCloseChannel();
    }

    private void tryCloseChannel() {
        try {
            runAsync(this::closeChannel, publishingExecutor).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while closing publishing channel", e);
        }
    }

    private void checkIsRunning() {
        checkState(running.get(), "Publisher has been closed");
    }

    private Channel publishChannel() {
        if (channel == null) {
            channel = channelFactory.get();
        }
        return channel;
    }

    private <T> void publish(final Channel channel, final PublishOptions options, final T message) {
        logPublish(options, message);
        try {
            final Message serialized = serializer.serialize(message);
            final AMQP.BasicProperties properties = messageProperties(options, serialized);
            channel.basicPublish(options.exchange(), options.routingKey(), options.mandatory(), options.immediate(),
                    properties, serialized.body());
        } catch (final IOException e) {
            throw PublishingException.create(options, e);
        }
    }

    private static <T> void logPublish(final PublishOptions options, final T message) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Publishing: %s to: '%s'", message, options.exchange()));
        }
    }

    private <T> void publishAll(final Channel channel, final MultiPublishOptions<T> options,
            final Collection<T> messages) {
        boolean interrupted = false;
        for (final T message : messages) {
            try {
                final Message serialized = serializer.serialize(message);
                final PublishOptions publishOptions = options.optionsOf(message);
                if (interrupted) {
                    options.interrupted(message);
                } else if (!running.get()) {
                    options.closed(message);
                } else {
                    publish(channel, publishOptions, serialized);
                }
            } catch (final Exception e) {
                interrupted = !options.shouldContinueOnError(message, e);
            }
        }
    }

    private void closeChannel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                throw new IllegalStateException("Could not close channel", e);
            }
        }
    }

    private AMQP.BasicProperties messageProperties(final PublishOptions options, final Message message) {
        final SerializationConfig serialization = config.serialization();
        return new AMQP.BasicProperties.Builder()
                .correlationId(options.correlationId())
                .deliveryMode(options.deliveryMode() != null ?
                        options.deliveryMode().value() : defaultDeliveryMode())
                .expiration(Durations.isGreaterThan(options.expirationTime(), Duration.ZERO) ?
                        Long.toString(options.expirationTime().toMillis()) : null)
                .priority(options.priority())
                .replyTo(options.replyTo())
                .headers(options.headers())
                .type(serialization.includeContentType() ? message.type() : null)
                .contentEncoding(serialization.incldeEncoding() ? message.encoding() : null)
                .contentType(serialization.includeContentType() ? message.contentType() : null)
                .build();
    }

    private int defaultDeliveryMode() {
        return config.messagePersitanceEnabled() ?
                DeliveryMode.Persistent.value() : DeliveryMode.NonPersistent.value();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private class PublishingWorker<T> implements QueuePublisher {
        private final CountDownLatch processingLatch = new CountDownLatch(1);
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final MultiPublishOptions<T> options;
        private final BlockingQueue<T> queue;
        private final Supplier<Channel> channelFactory;
        private Channel channel;

        private void run() {
            running.set(true);
            while (shouldProcess()) {
                try {
                    readFromQueue();
                } catch (final InterruptedException e) {
                    log.warn("Publishing has been interuupted", e);
                    break;
                }
            }
            processingLatch.countDown();
        }

        private boolean shouldProcess() {
            return running.get() && RabbitMessagePublisher.this.running.get();
        }

        private void readFromQueue() throws InterruptedException {
            T message = null;
            while ((message = queue.poll(1, SECONDS)) != null) {
                if (!tryPublish(message)) {
                    break;
                }
            }
        }

        private Channel publishChannel() {
            if (channel == null || !channel.isOpen()) {
                channel = channelFactory.get();
            }
            return channel;
        }

        private boolean tryPublish(final T message) {
            if (!shouldProcess()) {
                return false;
            }
            try {
                final Channel channel = publishChannel();
                final PublishOptions publishOptions = options.optionsOf(message);
                RabbitMessagePublisher.this.publish(channel, publishOptions, message);
                return true;
            } catch (final Exception e) {
                if (!options.shouldContinueOnError(message, e)) {
                    return reschedulePublication(message);
                }
                return true;
            }
        }

        private boolean reschedulePublication(final T message) {
            if (!shouldProcess()) {
                return false;
            }
            try {
                log.warn(String.format("Retrying message: '%s' publication in 1000ms", message));
                Thread.sleep(1000L);
            } catch (final InterruptedException e) {
                log.warn("Publish reschedule delay interrupted", e);
            }
            return tryPublish(message);
        }

        @Override
        public boolean isRunning() {
            return processingLatch.getCount() > 0;
        }

        @Override
        public void close() {
            running.set(false);
            awaitClousure();
        }

        private void awaitClousure() {
            if (processingLatch.getCount() > 0) {
                try {
                    processingLatch.await(2, SECONDS);
                } catch (final InterruptedException e) {
                    log.warn("Queue publisher closing interrupted", e);
                }
                if (processingLatch.getCount() > 0) {
                    log.warn("Timeout while waiting for queue publisher close");
                }
            }
        }
    }
}
