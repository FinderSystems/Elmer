package pl.finder.elmer;

import pl.finder.elmer.model.ExchangeDefinition;
import pl.finder.elmer.model.QueueDefinition;
import pl.finder.elmer.model.Subscription;
import pl.finder.elmer.model.SubscriptionConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

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
		private boolean autoAckEnabled;

		@Override
		public SubscriptionToConfigurator bindedTo(final ExchangeDefinition exchange) {
			bindedExchange = exchange;
			return this;
		}

		@Override
		public SubscriptionToConfigurator withAutoAckEnabled() {
			autoAckEnabled = true;
			return this;
		}

		@Override
		public <TMessageBody> Subscription using(
				final MessageConsumer<TMessageBody> consumer) {
			final SubscriptionConfig config = SubscriptionConfig.builder()
					.queue(queue)
					.autoAckEnabled(autoAckEnabled)
					.bindedExchange(bindedExchange)
					.build();
			final Subscription subscription = bus.subscribe(config, consumer);
			return subscription;
		}
	}
}
