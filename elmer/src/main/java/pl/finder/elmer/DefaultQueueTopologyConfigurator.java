package pl.finder.elmer;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import pl.finder.elmer.model.DeleteQueueOptions;
import pl.finder.elmer.model.QueueBindingDefinition;
import pl.finder.elmer.model.QueueDefinition;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultQueueTopologyConfigurator implements QueueTopologyConfigurator {
    private final TopologyConfigurator topology;
    private final String queue;

    @Override
    public void declare(final Consumer<QueueDefinition.Configurator> queueDefinitionConfigurator)
            throws ChannelException {
        final QueueDefinition.Configurator toConfigure = QueueDefinition.configurator()
                .name(queue);
        queueDefinitionConfigurator.accept(toConfigure);
        final QueueDefinition queueDefinition = toConfigure.create();
        topology.declareQueue(queueDefinition);
    }

    @Override
    public void delete(final DeleteQueueOptions options) {
        topology.deleteQueue(queue, options != null ? options : DeleteQueueOptions.empty());
    }

    @Override
    public QueueBindingConfigurator bind() {
        return new DefaultQueueBindingConfigurator(topology, queue);
    }

    @Override
    public QueueUnbindingConfigurator unbind()  {
        return new DefaultQueueUnbindingConfigurator(topology, queue);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DefaultQueueBindingConfigurator implements QueueBindingConfigurator {
        private final TopologyConfigurator topology;
        private final String queue;
        private String routingKey;

        @Override
        public QueueBindingConfigurator withRoutingKey(final String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        @Override
        public void toExchange(final String exchange) throws ChannelException {
            final QueueBindingDefinition binding = QueueBindingDefinition.configurator()
                    .queue(queue)
                    .exchange(exchange)
                    .routingKey(routingKey)
                    .create();
            topology.bindQueue(binding);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DefaultQueueUnbindingConfigurator implements QueueUnbindingConfigurator {
        private final TopologyConfigurator topology;
        private final String queue;
        private String routingKey;

        @Override
        public QueueUnbindingConfigurator withRoutingKey(final String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        @Override
        public void fromExchange(final String exchange) throws ChannelException {
            final QueueBindingDefinition binding = QueueBindingDefinition.configurator()
                    .queue(queue)
                    .exchange(exchange)
                    .routingKey(routingKey)
                    .create();
            topology.unbindQueue(binding);
        }

    }
}
