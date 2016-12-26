package pl.finder.elmer;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.Channel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.finder.elmer.core.AMQPException;
import pl.finder.elmer.core.ChannelException;
import pl.finder.elmer.core.MessageContext;
import pl.finder.elmer.core.SubscriptionException;
import pl.finder.elmer.serialization.MessageSerializer;
import pl.finder.elmer.subscription.SubscribeOptions;
import pl.finder.elmer.subscription.Subscription;
import pl.finder.elmer.subscription.SubscriptionManager;

@Slf4j
final class RabbitSubscriptionManager implements SubscriptionManager {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Set<Subscription> subscriptions = new HashSet<>();

    private final MessageSerializer serializer;
    private final Supplier<Channel> channelFactory;

    RabbitSubscriptionManager(final MessageSerializer serializer, final Supplier<Channel> channelFactory) {
        this.serializer = serializer;
        this.channelFactory = channelFactory;
    }

    @Override
    public <T> Subscription subscribe(final SubscribeOptions<T> options, final Consumer<MessageContext<T>> consumer) {
        checkState(!closed.get(), "MessageBus has been closed");
        final Map<String, Object> arguments = ImmutableMap.of();
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(String.format("%s-ConsumptionThread", options.queue()) + "-%s")
                .build();
        final Executor executor = Executors.newFixedThreadPool(options.concurrentThreadsLimit(), threadFactory);
        final Channel channel = channelFactory.get();
        try {
            final InternalConsumer<T> callback = new InternalConsumer<>(
                    channel, executor, options, serializer, consumer);
            final String consumerTag = channel.basicConsume(options.queue(), options.autoAckEnabled(),
                    options.consumerTag(), options.noLocal(), options.exclusive(), arguments, callback);
            final Subscription subscription =  new DefaultSubscription(consumerTag,
                    callback::getConsumerTag, callback::getChannel, this::removeSubscription);
            addSubscription(subscription);
            return subscription;
        } catch (final IOException e) {
            tryClose(channel);
            throw SubscriptionException.create(options, e);
        }
    }

    void close() {
        closed.set(true);
        closeAllSubscriptions();
    }

    private void addSubscription(final Subscription subscription) {
        synchronized (subscriptions) {
            subscriptions.add(subscription);
        }
    }

    private void removeSubscription(final Subscription subscription) {
        synchronized (subscriptions) {
            subscriptions.remove(subscription);
        }
    }

    private void closeAllSubscriptions() {
        final Set<Subscription> toClose;
        synchronized (subscriptions) {
            toClose = ImmutableSet.copyOf(subscriptions);
        }
        toClose.forEach(RabbitSubscriptionManager::tryCloseSubscription);
        synchronized (subscriptions) {
            subscriptions.removeAll(toClose);
        }
    }

    private static void tryCloseSubscription(final Subscription subscription) {
        try {
            subscription.close();
        } catch (final AMQPException e) {
            log.error(String.format("Could not close subscription: '%s'", subscription.consumerTag()), e);
        }
    }

    private static void tryClose(final Channel channel) {
        try {
            channel.close();
        } catch (IOException | TimeoutException e) {
            log.error("Error while closing channel", e);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DefaultSubscription implements Subscription {
        private final String initialConsumerTag;
        private final Supplier<String> consumerTag;
        private final Supplier<Channel> channel;
        private final Consumer<Subscription> onClose;

        @Override
        public String consumerTag() {
            final String tag = consumerTag.get();
            return tag != null ? tag : initialConsumerTag;
        }

        @Override
        public void close() {
            final Channel subscriptionChannel = channel.get();
            try {
                if (subscriptionChannel != null && subscriptionChannel.isOpen()) {
                    subscriptionChannel.close();
                }
                onClose.accept(this);
            } catch (IOException | TimeoutException e) {
                throw new ChannelException(
                        String.format("Error while closing consumer: '%s' channel", consumerTag()), e);
            }
        }

    }
}
