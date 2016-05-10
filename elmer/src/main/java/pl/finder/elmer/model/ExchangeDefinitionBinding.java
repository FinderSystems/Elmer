package pl.finder.elmer.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class ExchangeDefinitionBinding {
    private final String source;
    private final String destination;
    private final String routingKey;

    public static ExchangeDefinitionBinding.Configurator configurator() {
        return new Configurator();
    }

    public ExchangeDefinitionBinding.Configurator reconfigure() {
        return configurator()
                .source(source)
                .destination(destination)
                .routingKey(routingKey);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Configurator {
        private String source;
        private String destination;
        private String routingKey;

        public ExchangeDefinitionBinding create() {
            checkArgument(!isNullOrEmpty(source), "Source exchange not specified");
            checkArgument(!isNullOrEmpty(destination), "Destination exchange not specified");
            return new ExchangeDefinitionBinding(source, destination, routingKey);
        }
    }
}
