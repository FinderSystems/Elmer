package pl.finder.elmer.subscription;

import java.util.function.Consumer;

import pl.finder.elmer.core.AMQPException;
import pl.finder.elmer.core.MessageContext;

public interface SubscriptionManager {

    <T> Subscription subscribe(SubscribeOptions<T> options, Consumer<MessageContext<T>> consumer)
            throws AMQPException;

    default QueueSubscriptionCreator subscribe() {
        return new DefaultSubscriptionCreator(this);
    }

    public static interface QueueSubscriptionCreator {
        SubscriptionCreator toQueue(String queue);
    }

    public static interface SubscriptionCreator {
        SubscriptionCreator withAutoAck(boolean enabled);

        SubscriptionCreator withConsumerTag(String consumerTag);

        SubscriptionCreator exclusive(boolean enabled);

        SubscriptionCreator noLocal(final boolean enabled);

        Subscription create(Consumer<MessageContext<byte[]>> consumer)
                throws AMQPException;

        <T> Subscription create(Class<T> messageType, Consumer<MessageContext<T>> consumer)
                throws AMQPException;

        default SubscriptionCreator withAutoAckEnabled() {
            return withAutoAck(true);
        }

        default SubscriptionCreator withAutoAckDisabled() {
            return withAutoAck(false);
        }

        default SubscriptionCreator exclusive() {
            return exclusive(true);
        }

        default SubscriptionCreator notExclusive() {
            return exclusive(false);
        }
    }
}
