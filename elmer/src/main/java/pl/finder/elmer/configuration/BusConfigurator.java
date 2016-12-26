package pl.finder.elmer.configuration;

import java.util.function.Consumer;

import pl.finder.elmer.io.SslConfigurator;
import pl.finder.elmer.serialization.MessageSerializer;

public interface BusConfigurator {

    BusConfigurator configureSerializer(Consumer<MessageSerializer.Configurator> configure);

    BusConfigurator configureSsl(Consumer<SslConfigurator> configure);
}
