package pl.finder.elmer;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.Channel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import pl.finder.elmer.RabbitConnectionProvider.DefaultSslConfigurator;
import pl.finder.elmer.configuration.BusConfigurator;
import pl.finder.elmer.configuration.RabbitMqConfig;
import pl.finder.elmer.core.MessageBus;
import pl.finder.elmer.core.MessageContext;
import pl.finder.elmer.io.ConnectionProvider;
import pl.finder.elmer.io.PooledConnectionProvider;
import pl.finder.elmer.io.SslConfigurator;
import pl.finder.elmer.publication.MessagePublisher;
import pl.finder.elmer.publication.MultiPublishOptions;
import pl.finder.elmer.publication.PublishOptions;
import pl.finder.elmer.publication.QueuePublisher;
import pl.finder.elmer.serialization.DelegatingMessageSerializer;
import pl.finder.elmer.serialization.MessageSerializer;
import pl.finder.elmer.serialization.MessageSerializer.Configurator;
import pl.finder.elmer.subscription.SubscribeOptions;
import pl.finder.elmer.subscription.Subscription;
import pl.finder.elmer.topology.TopologyManager;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RabbitMessageBus implements MessageBus {
    private final ConnectionProvider connectionProvider;
    private final MessagePublisher publisher;
    private final RabbitSubscriptionManager subscriptionManager;
    private final RabbitTopologyManager topologyManager;
    private final Supplier<MessagePublisher> publisherFactory;

    static MessageBus create(final RabbitMqConfig config, final Consumer<BusConfigurator> options) {
        final DefaultBusConfigurator configurator = DefaultBusConfigurator.create();
        options.accept(configurator);

        final MessageSerializer serializer = configurator.createSerializer();
        final ConnectionProvider connectionProvider = configurator.createConnectionProvider(config);
        final ConnectionProvider connectionPool = new PooledConnectionProvider(connectionProvider,
                config.connectionPoolSize());
        final Supplier<Channel> channelFactory = RabbitChannelFactory.create(connectionPool);
        final MessagePublisher publisher = createMessagePublisher(config, serializer, channelFactory);
        final RabbitSubscriptionManager subscriptionManager = new RabbitSubscriptionManager(serializer, channelFactory);
        final RabbitTopologyManager topologyManager = new RabbitTopologyManager();

        return new RabbitMessageBus(connectionProvider, publisher, subscriptionManager,
                topologyManager, () -> createMessagePublisher(config, serializer, channelFactory));
    }

    private static MessagePublisher createMessagePublisher(final RabbitMqConfig config,
            final MessageSerializer serializer,
            final Supplier<Channel> channelFactory) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("PublishingThread-%d")
                .build();
        final Executor publishingExecutor = Executors.newSingleThreadExecutor(threadFactory);
        return new RabbitMessagePublisher(config, publishingExecutor, serializer, channelFactory);
    }

    @Override
    public <T> CompletionStage<MessagePublisher> publish(final PublishOptions options, final T message) {
        return publisher.publish(options, message);
    }

    @Override
    public <T> CompletionStage<MessagePublisher> publishAll(final MultiPublishOptions<T> options,
            final Collection<T> messages) {
        return publisher.publishAll(options, messages);
    }

    @Override
    public <T> QueuePublisher publishFrom(final MultiPublishOptions<T> options, final BlockingQueue<T> messageQueue) {
        return publisher.publishFrom(options, messageQueue);
    }

    @Override
    public <T> Subscription subscribe(final SubscribeOptions<T> options, final Consumer<MessageContext<T>> consumer) {
        return subscriptionManager.subscribe(options, consumer);
    }

    @Override
    public MessagePublisher createPublisher() {
        return publisherFactory.get();
    }

    @Override
    public TopologyManager topology() {
        return topologyManager;
    }

    @Override
    public void close() {
        try {
            publisher.close();
            subscriptionManager.close();
            topologyManager.close();
        } finally {
            connectionProvider.close();
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE, staticName = "create")
    private static class DefaultBusConfigurator implements BusConfigurator {
        private MessageSerializer serializer;
        private final DefaultSslConfigurator sslConfigurator = DefaultSslConfigurator.create();

        @Override
        public BusConfigurator configureSerializer(final Consumer<Configurator> configure) {
            serializer = DelegatingMessageSerializer.create(configure);
            return this;
        }

        @Override
        public BusConfigurator configureSsl(final Consumer<SslConfigurator> configure) {
            configure.accept(sslConfigurator);
            return this;
        }

        private MessageSerializer createSerializer() {
            if (serializer != null) {
                return serializer;
            }
            return DelegatingMessageSerializer.create(options -> {});
        }

        private ConnectionProvider createConnectionProvider(final RabbitMqConfig config) {
            return RabbitConnectionProvider.create(config, sslConfigurator);
        }
    }
}
