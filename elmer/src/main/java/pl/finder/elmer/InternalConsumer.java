package pl.finder.elmer;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import lombok.extern.slf4j.Slf4j;
import pl.finder.elmer.core.MessageContext;
import pl.finder.elmer.serialization.Message;
import pl.finder.elmer.serialization.MessageSerializer;
import pl.finder.elmer.subscription.SubscribeOptions;

@Slf4j
final class InternalConsumer<T> extends DefaultConsumer {
    private final Executor executor;
    private final SubscribeOptions<T> options;
    private final MessageSerializer serializer;
    private final Consumer<MessageContext<T>> consumer;

    InternalConsumer(final Channel channel, final Executor executor,
            final SubscribeOptions<T> options,
            final MessageSerializer serializer,
            final Consumer<MessageContext<T>> consumer) {
        super(channel);
        this.executor = executor;
        this.options = options;
        this.serializer = serializer;
        this.consumer = consumer;
    }

    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope,
            final BasicProperties properties, final byte[] body)
                    throws IOException {
        executor.execute(() -> {
            try {
                consume(consumerTag, envelope, properties, body);
            } catch (final Exception e) {
                tryNotifyConsumeError(e);
            }
        });
    }

    private void tryNotifyConsumeError(final Exception e) {
        try {
            options.onError(e);
        } catch (final Exception ex) {
            log.error("Fatal error: could not handle consume error", ex);
        }
    }

    private void consume(final String consumerTag, final Envelope envelope, final BasicProperties properties,
            final byte[] body) {
        final Message rawMessage = Message.builder()
                .body(body)
                .contentType(properties.getContentType())
                .encoding(properties.getContentEncoding())
                .type(properties.getType())
                .build();
        final T message = deserialize(rawMessage);
        final Consumer<Void> onAck = ackAction(envelope.getDeliveryTag());
        final MessageContext<T> messageContext = new RabbitMessageContext<>(
                message, consumerTag, envelope.getExchange(), envelope.getRoutingKey(),
                properties, onAck);
        logDelivery(messageContext);
        consumer.accept(messageContext);
    }

    private static void logDelivery(final MessageContext<?> messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("Received: " + messageContext);
        }
    }

    private T deserialize(final Message message) {
        if (options.messageType() != null) {
            return serializer.deserialize(message, options.messageType());
        } else {
            return serializer.deserialize(message);
        }
    }

    private Consumer<Void> ackAction(final long deliveryTag) {
        if (options.autoAckEnabled()) {
            return it -> {};
        }
        return it -> {
            final Channel channel = getChannel();
            try {
                channel.basicAck(deliveryTag, false);
            } catch (final IOException e) {
                throw new IllegalStateException(String.format("Could not deliver ack for: %d", deliveryTag), e);
            }
        };
    }
}
