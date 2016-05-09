package pl.finder.elmer;

import java.util.function.Consumer;

import pl.finder.elmer.model.ExchangeDefinition;
import pl.finder.elmer.model.QueueDefinition;

/**
 * Configures topology.
 */
public interface TopologyConfigurator {

	/**
	 * Creates exchange if not exists.
	 *
	 * @param exchange exchange to create
	 * @return self
	 */
	TopologyConfigurator declareExchange(ExchangeDefinition exchange);

	/**
	 * Creates queue if not exists.
	 *
	 * @param queue queue to create
	 * @return Self
	 */
	TopologyConfigurator declareQueue(QueueDefinition queue);

	/**
	 * Creates exchange if not exists.
	 *
	 * @param exchangeConfigurator exchange configurator
	 * @return self
	 */
	default TopologyConfigurator declareExchange(Consumer<ExchangeDefinition.Configurator> exchangeConfigurator) {
		final ExchangeDefinition.Configurator configurator = ExchangeDefinition.configurator();
		exchangeConfigurator.accept(configurator);
		final ExchangeDefinition exchange = configurator.create();
		return declareExchange(exchange);
	}

	/**
	 * Creates queue if not exists.
	 *
	 * @param queueConfigurator queue configurator
	 * @return Self
	 */
	default TopologyConfigurator declareQueue(Consumer<QueueDefinition.Configurator> queueConfigurator) {
		final QueueDefinition.Configurator configurator = QueueDefinition.configurator();
		queueConfigurator.accept(configurator);
		final QueueDefinition queue = configurator.create();
		return declareQueue(queue);
	}
}
