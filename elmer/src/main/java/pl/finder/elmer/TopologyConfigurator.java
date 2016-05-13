package pl.finder.elmer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import pl.finder.elmer.model.DeleteExchangeOptions;
import pl.finder.elmer.model.DeleteQueueOptions;
import pl.finder.elmer.model.ExchangeDefinition;
import pl.finder.elmer.model.ExchangeDefinitionBinding;
import pl.finder.elmer.model.QueueBindingDefinition;
import pl.finder.elmer.model.QueueDefinition;

/**
 * Configures topology.
 */
public interface TopologyConfigurator {
    // TODO change API to async

    Future<Void> delareExchangeAsync(ExchangeDefinition exchange);

    /**
     * Creates exchange if not exists.
     *
     * @param exchange exchange to create
     * @return self
     */
    default TopologyConfigurator declareExchange(final ExchangeDefinition exchange)
        throws ChannelException {
        Future<Void> promise = delareExchangeAsync(exchange);
        try {
            promise.get();
            return this;
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof ChannelException) {
                throw (ChannelException) e.getCause();
            }
            throw new IllegalStateException("Error while declaring exchange: " + exchange, e);
        } catch (final InterruptedException e) {
            throw new IllegalStateException(String.format("Exchange %s declartion interrupted", exchange));
        }
    }

    /**
     * Deletes exchange.
     *
     * @param exchange name of exchange to delete
     * @param option deletion option
     * @return self
     */
    TopologyConfigurator deleteExchange(String exchange, DeleteExchangeOptions options)
            throws ChannelException;

    /**
     * Creates binding between two exchanges.
     *
     * @param binding exchange-to-exchange binding definition.
     * @return self
     */
    TopologyConfigurator bindExchange(ExchangeDefinitionBinding binding);

    /**
     * Remotes binding between two exchanges.
     *
     * @param binding exchange-to-exchange binding definition.
     * @return self
     */
    TopologyConfigurator unbindExchange(ExchangeDefinitionBinding binding);

    /**
     * Creates queue if not exists.
     *
     * @param queue queue to create
     * @return Self
     */
    TopologyConfigurator declareQueue(QueueDefinition queue)
            throws ChannelException;

    /**
     * Deletes queue.
     *
     * @param queue name of queue to felete
     * @param options deletion option
     * @return Self
     */
    TopologyConfigurator deleteQueue(String queue, DeleteQueueOptions options)
        throws ChannelException;

    /**
     * Creates queue binding.
     *
     * @param binding definition of queue binding
     * @return self
     */
    TopologyConfigurator bindQueue(QueueBindingDefinition binding)
        throws ChannelException;

    /**
     * Removes queue binding to an exchange.
     *
     *  @param binding definition of queue binding
     * @rewturn Self
     */
    TopologyConfigurator unbindQueue(QueueBindingDefinition binding);

    /**
     * Returns configurator of exchange topology.
     *
     * @param exchange name of exchange
     * @return configurator of exchange topology
     */
    default ExchangeTopologyConfigurator exchange(final String exchange) {
        checkArgument(!isNullOrEmpty(exchange), "Exchange not specified");
        return new DefaultExchangeTopologyConfigurator(this, exchange);
    }

    /**
     * Creates exchange if not exists.
     *
     * @param exchangeConfigurator exchange configurator
     * @return self
     */
    default TopologyConfigurator declareExchange(final Consumer<ExchangeDefinition.Configurator> exchangeConfigurator)
        throws ChannelException {
        final ExchangeDefinition.Configurator configurator = ExchangeDefinition.configurator();
        exchangeConfigurator.accept(configurator);
        final ExchangeDefinition exchange = configurator.create();
        return declareExchange(exchange);
    }

    /**
     * Deletes exchange.
     *
     * @param exchange name of exchange to delete
     * @return self
     */
    default TopologyConfigurator deleteExchange(String exchange)
            throws ChannelException {
        return deleteExchange(exchange, DeleteExchangeOptions.createDefault());
    }

    /**
     * Creates binding between to exchanges.
     *
     * @param bindingConfigurator exchange-to-exchange binding configurator.
     * @return self
     */
    default TopologyConfigurator bindExchange(final Consumer<ExchangeDefinitionBinding.Configurator> bindingConfigurator)
        throws ChannelException {
        final ExchangeDefinitionBinding.Configurator toConfigure = ExchangeDefinitionBinding.configurator();
        bindingConfigurator.accept(toConfigure);
        final ExchangeDefinitionBinding binding = toConfigure.create();
        return bindExchange(binding);
    }

    /**
     * Creates queue if not exists.
     *
     * @param queueConfigurator queue configurator
     * @return Self
     */
    default TopologyConfigurator declareQueue(final Consumer<QueueDefinition.Configurator> queueConfigurator)
        throws ChannelException {
        final QueueDefinition.Configurator configurator = QueueDefinition.configurator();
        queueConfigurator.accept(configurator);
        final QueueDefinition queue = configurator.create();
        return declareQueue(queue);
    }

    /**
     * Deletes queue.
    *
    * @param queue name of queue to felete
    * @return Self
    */
    default TopologyConfigurator deleteQueue(final String queue)
       throws ChannelException {
        return deleteQueue(queue, DeleteQueueOptions.createDefault());
    }

    /**
     * Creates queue binding.
     *
     * @param bindingConfigurator queue binding configurator
     * @return self
     */
    default TopologyConfigurator bindQueue(final Consumer<QueueBindingDefinition.Configurator> bindingConfigurator)
        throws ChannelException {
        final QueueBindingDefinition.Configurator toConfigure = QueueBindingDefinition.configurator();
        bindingConfigurator.accept(toConfigure);
        final QueueBindingDefinition binding = toConfigure.create();
        return bindQueue(binding);
    }
}
