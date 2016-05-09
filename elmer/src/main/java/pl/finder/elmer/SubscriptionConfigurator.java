package pl.finder.elmer;

import java.util.function.Consumer;

import pl.finder.elmer.model.ExchangeDefinition;
import pl.finder.elmer.model.QueueDefinition;
import pl.finder.elmer.model.Subscription;

/**
 * Configurator of subscription.
 */
public interface SubscriptionConfigurator {

	/**
	 * Sets source queue of subscription.
	 *
	 * @param queue source queue
	 * @return configurator of queue subscription
	 */
	SubscriptionToConfigurator to(QueueDefinition queue);

	/**
	 * Sets source queue of subscription.
	 *
	 * @param queueName source queue name
	 * @return configurator of queue subscription
	 */
	default SubscriptionToConfigurator to(String queueName) {
		final QueueDefinition queue = QueueDefinition.createDefault(queueName);
		return to(queue);
	}

	/**
	 * Sets source queue of subscription.
	 *
	 * @param queueConfigurator configurator of queue definition
	 * @return configurator of queue subscription
	 */
	default SubscriptionToConfigurator to(
			Consumer<QueueDefinition.Configurator> queueConfigurator) {
		final QueueDefinition.Configurator configurator = QueueDefinition.configurator();
		queueConfigurator.accept(configurator);
		final QueueDefinition queue = configurator.create();
		return to(queue);
	}

	/**
	 * Configurator of queue subscription.
	 */
	public interface SubscriptionToConfigurator {

		/**
		 * Sets exchange binding.
		 *
		 * @param exchange binded exchange definition
		 * @return self
		 */
		SubscriptionToConfigurator bindedTo(ExchangeDefinition exchange);

		/**
		 * Enabled automated message acknowledgments.
		 *
		 * @return self.
		 */
		SubscriptionToConfigurator withAutoAckEnabled();

		/**
		 * Creates subscription.
		 *
		 * @param consumer consumer of received messages.
		 * @return created subscription
		 */
		<TMessageBody> Subscription using(MessageConsumer<TMessageBody> consumer);

		/**
		 * Sets exchange binding.
		 *
		 * @param exchangeName binded exchange name
		 * @return self
		 */
		default SubscriptionToConfigurator bindedTo(String exchangeName) {
			final ExchangeDefinition exchange = ExchangeDefinition.createDefault(exchangeName);
			return bindedTo(exchange);
		}

		/**
		 * Sets exchange binding.
		 *
		 * @param  ExchangeDefinition binded exchange configurator
		 * @return self
		 */
		default SubscriptionToConfigurator bindedTo(
				Consumer<ExchangeDefinition.Configurator> exchangeConfigurator) {
			final ExchangeDefinition.Configurator configurator = ExchangeDefinition.configurator();
			exchangeConfigurator.accept(configurator);
			final ExchangeDefinition exchange = configurator.create();
			return bindedTo(exchange);
		}
	}
}