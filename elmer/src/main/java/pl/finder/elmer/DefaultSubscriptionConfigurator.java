package pl.finder.elmer;

import static com.google.common.base.Preconditions.checkState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import pl.finder.elmer.consumer.BasicMessageConsumer;
import pl.finder.elmer.consumer.MessageConsumer;
import pl.finder.elmer.consumer.MessageConsumers;
import pl.finder.elmer.model.ExchangeDefinition;
import pl.finder.elmer.model.QueueDefinition;
import pl.finder.elmer.model.Subscription;
import pl.finder.elmer.model.SubscriptionConfig;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultSubscriptionConfigurator implements SubscriptionConfigurator {
    private final MessageBus bus;

    @Override
    public SubscriptionToConfigurator to(final QueueDefinition queue) {
        return new DefaultSubscriptionToConfigurator(bus, queue);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DefaultSubscriptionToConfigurator implements SubscriptionToConfigurator {
        private final MessageBus bus;
        private final QueueDefinition queue;
        private ExchangeDefinition bindedExchange;
        private boolean autoAckEnabled = true;

        @Override
        public SubscriptionToConfigurator bindedTo(final ExchangeDefinition exchange) {
            bindedExchange = exchange;
            return this;
        }

        @Override
        public SubscriptionToConfigurator withAutoAckDisabled() {
            autoAckEnabled = false;
            return this;
        }

        @Override
        public <TMessageBody> Subscription using(final MessageConsumer<TMessageBody> consumer) {
            final SubscriptionConfig config = SubscriptionConfig.builder()
                    .queue(queue)
                    .autoAckEnabled(autoAckEnabled)
                    .bindedExchange(bindedExchange)
                    .build();
            final Subscription subscription = bus.subscribe(config, consumer);
            return subscription;
        }

        @Override
        public <TMessageBody> Subscription using(
                final BasicMessageConsumer<TMessageBody> basicConsumer) {
            final SubscriptionConfig config = SubscriptionConfig.builder()
                    .queue(queue)
                    .autoAckEnabled(autoAckEnabled)
                    .bindedExchange(bindedExchange)
                    .build();
            checkState(!config.autoAckEnabled(),
                    "Could not create subscription to BasicMessageConsumer when auto ACK is disabled");
            final MessageConsumer<TMessageBody> consumer = MessageConsumers.from(basicConsumer);
            final Subscription subscription = bus.<TMessageBody> subscribe(config, consumer);
            return subscription;
        }
    }
}
