package pl.finder.elmer;

import java.io.IOException;

import pl.finder.elmer.consumer.MessageConsumer;
import pl.finder.elmer.model.PublishConfig;
import pl.finder.elmer.model.Subscription;
import pl.finder.elmer.model.SubscriptionConfig;

/**
 * Message bus.
 */
public interface MessageBus extends AutoCloseable {

    /**
     * Publishes message with given configuration.
     *
     * @param config configuration of published message
     * @param message message to publish
     * @throws ChannelException
     */
    <TMessage> void publish(PublishConfig config, TMessage message)
            throws ChannelException;

    /**
     * Subscribes consumer with given configuration.
     *
     * @param config configuration of message subscription.
     * @param consumer consumer of received messages.
     * @return created subscription hook
     */
    <TMessage> Subscription subscribe(SubscriptionConfig config, MessageConsumer<TMessage> consumer);

    /**
     * Returns topology configurator.
     *
     * @return topology configurator
     */
    TopologyConfigurator topology();

    /**
     * Closes message bus connection.
     */
    @Override
    public void close() throws IOException;

    /**
     * Fluent API for publishing.
     *
     * @return configurator of published message.
     */
    default PublishConfigurator publish() {
        return new DefaultPublishConfigurator(this);
    }

    /**
     * Fluent API for subscription.
     *
     * @return configurator of subscription
     */
    default SubscriptionConfigurator subscribe() {
        return new DefaultSubscriptionConfigurator(this);
    }
}
