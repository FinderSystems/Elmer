package pl.finder.elmer.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
public final class SubscriptionConfig {
	private final QueueDefinition queue;
	private final ExchangeDefinition bindedExchange;
	private final boolean autoAckEnabled;
//	private final int threadPoolLimit;

	public static SubscriptionConfig.Builder builder() {
		return new Builder();
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@Setter
	public static final class Builder {
		private QueueDefinition queue;
		private ExchangeDefinition bindedExchange;
		private boolean autoAckEnabled;

		public SubscriptionConfig.Builder queue(final QueueDefinition queue) {
			this.queue = queue;
			return this;
		}

		public SubscriptionConfig.Builder queue(
				final Consumer<QueueDefinition.Configurator> queueConfigurator) {
			final QueueDefinition.Configurator configurator = QueueDefinition.configurator();
			queueConfigurator.accept(configurator);
			return queue(configurator.create());
		}

		public SubscriptionConfig.Builder bindedExchange(final ExchangeDefinition bindedExchange) {
			this.bindedExchange = bindedExchange;
			return this;
		}

		public SubscriptionConfig.Builder bindedExchange(
				final Consumer<ExchangeDefinition.Configurator> exchangeConfigurator) {
			final ExchangeDefinition.Configurator configurator = ExchangeDefinition.configurator();
			exchangeConfigurator.accept(configurator);
			return bindedExchange(configurator.create());
		}

		public SubscriptionConfig build() {
			checkArgument(queue != null, "Queue definition not specified");
			return new SubscriptionConfig(queue, bindedExchange, autoAckEnabled);
		}
	}
}
