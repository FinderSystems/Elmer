package pl.finder.elmer;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import pl.finder.elmer.model.DeleteExchangeOptions;
import pl.finder.elmer.model.ExchangeDefinition;
import pl.finder.elmer.model.ExchangeDefinitionBinding;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultExchangeTopologyConfigurator implements ExchangeTopologyConfigurator {
    private final TopologyConfigurator topology;
    private final String exchange;

    @Override
    public void declare(final Consumer<ExchangeDefinition.Configurator> exchangeConfigurator) {
        final ExchangeDefinition.Configurator toConfigure = ExchangeDefinition.configurator()
                .name(exchange);
        exchangeConfigurator.accept(toConfigure);
        final ExchangeDefinition exchangeDefinition = toConfigure.create();
        topology.declareExchange(exchangeDefinition);
    }

    @Override
    public void delete(final DeleteExchangeOptions options) {
        topology.deleteExchange(exchange, options != null ? options : DeleteExchangeOptions.createDefault());
    }

    @Override
    public ExchangeBindingConfigurator bind() {
        return new DefaultExchangeBindingConfigurator(topology, exchange);
    }

    @Override
    public ExchangeUnbindingConfigurator unbind() throws ChannelException {
        return new DefaultExchangeUnbindingConfigurator(topology, exchange);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DefaultExchangeBindingConfigurator implements ExchangeBindingConfigurator {
        private final TopologyConfigurator topology;
        private final String exchange;
        private String routingKey;

        @Override
        public ExchangeBindingConfigurator withRoutingKey(final String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        @Override
        public void toExchange(final String destinationExchange) throws ChannelException {
            final ExchangeDefinitionBinding binding = ExchangeDefinitionBinding.configurator()
                    .source(exchange)
                    .destination(destinationExchange)
                    .routingKey(routingKey)
                    .create();
            topology.bindExchange(binding);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DefaultExchangeUnbindingConfigurator implements ExchangeUnbindingConfigurator {
        private final TopologyConfigurator topology;
        private final String exchange;
        private String routingKey;

        @Override
        public ExchangeUnbindingConfigurator withRoutingKey(final String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        @Override
        public void fromExchange(final String destinationExchange) throws ChannelException {
            final ExchangeDefinitionBinding binding = ExchangeDefinitionBinding.configurator()
                    .source(exchange)
                    .destination(destinationExchange)
                    .routingKey(routingKey)
                    .create();
            topology.unbindExchange(binding);
        }
    }
}
