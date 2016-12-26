package pl.finder.elmer.subscription;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.finder.elmer.core.MessageContext;
import pl.finder.elmer.subscription.SubscriptionManager.QueueSubscriptionCreator;
import pl.finder.elmer.subscription.SubscriptionManager.SubscriptionCreator;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultSubscriptionCreator implements QueueSubscriptionCreator, SubscriptionCreator {
    private final SubscriptionManager subscriptionManager;

    private String queue;
    private boolean autoAckEnabled = true;
    private boolean exclusive;
    private boolean noLocal;
    private String consumerTag;

    @Override
    public SubscriptionCreator withAutoAck(final boolean enabled) {
        autoAckEnabled = enabled;
        return this;
    }

    @Override
    public SubscriptionCreator exclusive(final boolean enabled) {
        exclusive = enabled;
        return this;
    }

    @Override
    public SubscriptionCreator noLocal(final boolean enabled) {
        noLocal = enabled;
        return this;
    }

    @Override
    public SubscriptionCreator withConsumerTag(final String consumerTag) {
        this.consumerTag = consumerTag;
        return this;
    }

    @Override
    public SubscriptionCreator toQueue(final String queue) {
        this.queue = queue;
        return this;
    }

    @Override
    public Subscription create(final Consumer<MessageContext<byte[]>> consumer) {
        final SubscribeOptions<byte[]> options = createOptions(byte[].class);
        return subscriptionManager.subscribe(options, consumer);
    }

    @Override
    public <T> Subscription create(final Class<T> messageType, final Consumer<MessageContext<T>> consumer) {
        final SubscribeOptions<T> options = createOptions(messageType);
        return subscriptionManager.subscribe(options, consumer);
    }

    private <T> SubscribeOptions<T> createOptions(final Class<T> messageType) {
        return SubscribeOptions.<T> builder()
                .messageType(messageType)
                .autoAckEnabled(autoAckEnabled)
                .queue(queue)
                .exclusive(exclusive)
                .noLocal(noLocal)
                .consumerTag(consumerTag)
                .build();
    }
}
