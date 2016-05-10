package pl.finder.elmer.rabbit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.Duration.ZERO;
import static pl.finder.elmer.extensions.DurationExtensions.isGreaterThan;
import static pl.finder.elmer.extensions.LoggerExtensions.debug;
import static pl.finder.elmer.model.AmqpProperties.DelayedType;
import static pl.finder.elmer.model.AmqpProperties.DeletedMessage;
import static pl.finder.elmer.model.AmqpProperties.Expires;
import static pl.finder.elmer.model.AmqpProperties.MaxLength;
import static pl.finder.elmer.model.AmqpProperties.MaxLengthBytes;
import static pl.finder.elmer.model.AmqpProperties.MessageTimeToLive;

import java.io.IOException;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.finder.elmer.ChannelException;
import pl.finder.elmer.TopologyConfigurator;
import pl.finder.elmer.model.DeleteExchangeOptions;
import pl.finder.elmer.model.DeleteQueueOptions;
import pl.finder.elmer.model.ExchangeDefinition;
import pl.finder.elmer.model.ExchangeDefinitionBinding;
import pl.finder.elmer.model.QueueBindingDefinition;
import pl.finder.elmer.model.QueueDefinition;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.Channel;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
final class RabbitTopologyConfigurator implements TopologyConfigurator {
    private final Channel channel;

    @Override
    public TopologyConfigurator declareExchange(final ExchangeDefinition exchange) throws ChannelException {
        checkArgument(exchange != null, "Exchange definition not specified");
        try {
            debug(log, () -> String.format("Declaring: %s", exchange));
            channel.exchangeDeclare(exchange.name(), exchange.type().name(), exchange.durable(),
                    exchange.autoDeletable(), exchange.internal(), argumentsOf(exchange));
            return this;
        } catch (final IOException e) {
            throw new ChannelException("Error while creating: " + exchange, e);
        }
    }

    @Override
    public TopologyConfigurator deleteExchange(final String exchange, final DeleteExchangeOptions options)
            throws ChannelException {
        checkArgument(!isNullOrEmpty(exchange), "Exchange name not specified");
        try {
            debug(log, () -> String.format("Deleting: '%s' with %s", exchange, options));
            channel.exchangeDelete(exchange, options.onlyWhenNotUsed());
            return this;
        } catch (final IOException e) {
            throw new ChannelException(String.format("Error while deleting: '%s' exhange", exchange), e);
        }
    }

    @Override
    public TopologyConfigurator bindExchange(final ExchangeDefinitionBinding binding) throws ChannelException {
        checkArgument(binding != null, "Exchange binding not specified");
        try {
            debug(log, () -> String.format("Binding exchange: %s", binding));
            channel.exchangeBind(binding.destination(), binding.source(), binding.routingKey(), argumentsOf(binding));
            return this;
        } catch (final IOException e) {
            throw new ChannelException("Error while binding: " + binding, e);
        }
    }

    @Override
    public TopologyConfigurator unbindExchange(final ExchangeDefinitionBinding binding) throws ChannelException {
        checkArgument(binding != null, "Exchange binding not specified");
        try {
            debug(log, () -> String.format("Unbinding exchange: %s", binding));
            channel.exchangeUnbind(binding.destination(), binding.source(), binding.routingKey());
            return this;
        } catch (final IOException e) {
            throw new ChannelException("Error while unbinding: " + binding, e);
        }
    }

    @Override
    public TopologyConfigurator declareQueue(final QueueDefinition queue) {
        checkArgument(queue != null, "Queue definition not specified");
        try {
            debug(log, () -> String.format("Declaring: %s", queue));
            channel.queueDeclare(queue.name(), queue.durable(), queue.exclusive(),
                    queue.autoDeletable(), argumentsOf(queue));
            return this;
        } catch (final IOException e) {
            throw new ChannelException("Error while creating: " + queue, e);
        }
    }

    @Override
    public TopologyConfigurator deleteQueue(final String queue, final DeleteQueueOptions options)
            throws ChannelException {
        checkArgument(!isNullOrEmpty(queue), "Queue name not specified");
        try {
            debug(log, () -> String.format("Deleting: '%s' with %s", queue, options));
            channel.queueDelete(queue, options.onlyWhenNotUsed(), options.onlyWhenNotEmpty());
            return this;
        } catch (final IOException e) {
            throw new ChannelException(String.format("Error while deleting: '%s' queue", queue), e);
        }
    }

    @Override
    public TopologyConfigurator bindQueue(final QueueBindingDefinition binding) throws ChannelException {
        checkArgument(binding != null, "Queue binding not specified");
        try {
            debug(log, () -> String.format("Binding queue: %s", binding));
            channel.queueBind(binding.queue(), binding.exchange(), binding.routingKey(), argumentsOf(binding));
            return this;
        } catch (final IOException e) {
            throw new ChannelException("Error while binding: " + binding, e);
        }
    }

    @Override
    public TopologyConfigurator unbindQueue(final QueueBindingDefinition binding) {
        checkArgument(binding != null, "Queue binding not specified");
        try {
            debug(log, () -> String.format("Unbinding queue: %s", binding));
            channel.queueUnbind(binding.queue(), binding.exchange(), binding.routingKey(), argumentsOf(binding));
            return this;
        } catch (final IOException e) {
            throw new ChannelException("Error while unbinding: " + binding, e);
        }
    }

    private static Map<String, Object> argumentsOf(final ExchangeDefinition exchange) {
        final ImmutableMap.Builder<String, Object> arguments = ImmutableMap.builder();
        if (exchange.delayed()) {
            arguments.put(DelayedType, DeletedMessage);
        }
        return arguments.build();
    }

    private static Map<String, Object> argumentsOf(
            @SuppressWarnings("unused") final ExchangeDefinitionBinding binding) {
        final ImmutableMap.Builder<String, Object> arguments = ImmutableMap.builder();
        return arguments.build();
    }

    private static Map<String, Object> argumentsOf(final QueueDefinition queue) {
        final ImmutableMap.Builder<String, Object> arguments = ImmutableMap.builder();
        if (isGreaterThan(queue.messageExpirationTime(), ZERO)) {
            arguments.put(MessageTimeToLive, (int) queue.messageExpirationTime().toMillis());
        }
        if (isGreaterThan(queue.expirationTime(), ZERO)) {
            arguments.put(Expires, (int) queue.expirationTime().toMillis());
        }
        if (queue.maximumCapacity() > 0) {
            arguments.put(MaxLength, queue.maximumCapacity());
        }
        if (queue.maximumCapacityBytes() > 0) {
            arguments.put(MaxLengthBytes, queue.maximumCapacityBytes());
        }
        return arguments.build();
    }

    private static Map<String, Object> argumentsOf(
            @SuppressWarnings("unused") final QueueBindingDefinition binding) {
        final ImmutableMap.Builder<String, Object> arguments = ImmutableMap.builder();
        return arguments.build();
    }

}
