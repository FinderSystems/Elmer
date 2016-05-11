package pl.finder.elmer.rabbit;

import static pl.finder.elmer.extensions.LoggerExtensions.debug;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.finder.elmer.ChannelException;
import pl.finder.elmer.MessageBus;
import pl.finder.elmer.TopologyConfigurator;
import pl.finder.elmer.consumer.MessageConsumer;
import pl.finder.elmer.model.PublishConfig;
import pl.finder.elmer.model.Subscription;
import pl.finder.elmer.model.SubscriptionConfig;
import pl.finder.elmer.serialization.MessageSerializer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RabbitMessageBus implements MessageBus {
    private final TopologyConfigurator topology;
    private final MessageSerializer serializer;
    private final Connection connection;
    private final Channel channel;

    @Override
    public <TMessage> void publish(PublishConfig config, TMessage message) {
        final byte[] body = serializer.serialize(message);
        try {
            debug(log, () -> String.format("Publishing: '%s' with %s", message, config));
            channel.basicPublish(config.exchange(), config.routingKey(), config.mandatory(), config.immediate(),
                    propertiesOf(message.getClass(), config), body);
        } catch (IOException e) {
            throw new ChannelException("Could not publish: " + message, e);
        }
    }

    @Override
    public <TMessage> Subscription subscribe(final SubscriptionConfig config, MessageConsumer<TMessage> consumer) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopologyConfigurator topology() {
        return topology;
    }

    @Override
    public void close() throws IOException {
        try {
            closeChannel();
        } catch (final TimeoutException e) {
            throw new IOException("Timeout while closing channel", e);
        } finally {
            closeConnection();
        }
    }


    private void closeChannel() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    private void closeConnection() throws IOException {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }

    private BasicProperties propertiesOf(final Class<?> messageType, final PublishConfig config) {
        final BasicProperties properties = new BasicProperties.Builder()
            .contentEncoding(serializer.encodingOf(messageType))
            .contentType(serializer.contentTypeOf(messageType))
            .correlationId(config.correlationId())
            .headers(config.headers())
            .replyTo(config.replyTo())
            .type(messageType.getCanonicalName())
            .deliveryMode(config.deliveryMode() != null ? config.deliveryMode().value() : null)
            .build();
        return properties;
    }
}
