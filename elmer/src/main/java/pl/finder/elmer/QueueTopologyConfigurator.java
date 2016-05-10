package pl.finder.elmer;

import java.util.function.Consumer;

import pl.finder.elmer.model.DeleteQueueOptions;
import pl.finder.elmer.model.QueueDefinition;

/**
 * Configurator of queue topology.
 */
public interface QueueTopologyConfigurator {

    /**
     * Declares queue from given definition.
     *
     * @param queue configurator of queue definition
     * @throws ChannelException
     */
    void declare(Consumer<QueueDefinition.Configurator> queue)
        throws ChannelException;

    /**
     * Deletes queue.
     *
     * @param options delete queue options
     * @throws ChannelException
     */
    void delete(DeleteQueueOptions options)
            throws ChannelException;

    /**
     * Deletes queue.
     *
     * @throws ChannelException
     */
    default void delete() throws ChannelException {
        delete(DeleteQueueOptions.empty());
    }

    /**
     * Handler of queue binder.
     */
    QueueBindingConfigurator bind();

    /**
     * Handler of queue unbinding.
     */
    QueueUnbindingConfigurator unbind();

    /**
     * Configurator of queue binding.
     */
    public interface QueueBindingConfigurator {

        /**
         * Sets binding routing key.
         *
         * @param routingKey routing key
         * @return self
         */
        QueueBindingConfigurator withRoutingKey(String routingKey);

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
     * Configurator of queue unbinding.
     */
    public interface QueueUnbindingConfigurator {

        /**
         * Sets binding routing key.
         *
         * @param routingKey routing key
         * @return self
         */
        QueueUnbindingConfigurator withRoutingKey(String routingKey);

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
