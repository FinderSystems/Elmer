package pl.finder.elmer;

import java.util.function.Consumer;

import pl.finder.elmer.model.DeleteExchangeOptions;
import pl.finder.elmer.model.ExchangeDefinition;

public interface ExchangeTopologyConfigurator {

    /**
     * Declares exchange from given definition.
     *
     * @param configurator of exchange definition.
     * @throws ChannelException
     */
    void declare(Consumer<ExchangeDefinition.Configurator> exchange)
        throws ChannelException;

    /**
     * Deletes exchange.
     *
     * @param options delete exchange options
     * @throws ChannelException
     */
    void delete(DeleteExchangeOptions options)
        throws ChannelException;

    /**
     * Handler of exchange binding.
     */
    ExchangeBindingConfigurator bind();

    /**
     * Handler of exchange unbinding.
     */
    ExchangeUnbindingConfigurator unbind()
            throws ChannelException;

    /**
     * Deletes exchange.
     *
     * @throws ChannelException
     */
    default void delete() throws ChannelException {
        delete(DeleteExchangeOptions.empty());
    }

    /**
     * Configurator of exchange binding.
     */
    public interface ExchangeBindingConfigurator {

        /**
         * Sets binding routing key.
         *
         * @param routingKey routing key
         * @return self
         */
        ExchangeBindingConfigurator withRoutingKey(String routingKey);

        /**
         * Creates binding to given exchange.
         *
         * @param exchange name of exchange to bind to
         * @throws ChannelException
         */
        void toExchange(String exchange)
            throws ChannelException;
    }

    /**
     * Configurator of exchange unbinding.
     */
    public interface ExchangeUnbindingConfigurator {

        /**
         * Sets binding routing key.
         *
         * @param routingKey routing key
         * @return self
         */
        ExchangeUnbindingConfigurator withRoutingKey(String routingKey);

        /**
         * Removes binding from given exchange.
         *
         * @param exchange name of exchange to unbind from
         * @throws ChannelException
         */
        void fromExchange(String exchange)
            throws ChannelException;
    }
}
