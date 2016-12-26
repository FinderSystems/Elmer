package pl.finder.elmer;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.finder.elmer.configuration.BusConfigurator;
import pl.finder.elmer.configuration.RabbitMqConfig;
import pl.finder.elmer.core.MessageBus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Elmer {

    public static MessageBus huntRabbit() {
        final RabbitMqConfig config = RabbitMqConfig.createDefaut();
        return huntRabbit(config);
    }

    public static MessageBus huntRabbit(final RabbitMqConfig config) {
        return huntRabbit(config, options -> {});
    }

    public static MessageBus huntRabbit(final RabbitMqConfig config, final Consumer<BusConfigurator> options) {
        return RabbitMessageBus.create(config, options);
    }
}
