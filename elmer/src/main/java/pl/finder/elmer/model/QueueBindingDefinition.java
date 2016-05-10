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
public final class QueueBindingDefinition {
    private final String queue;
    private final String exchange;
    private final String routingKey;

    public static QueueBindingDefinition.Configurator configurator() {
        return new Configurator();
    }

    public QueueBindingDefinition.Configurator reconfigure() {
        return configurator()
                .queue(queue)
                .exchange(exchange)
                .routingKey(routingKey);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Configurator {
        private String queue;
        private String exchange;
        private String routingKey;

        public QueueBindingDefinition create() {
            checkArgument(!isNullOrEmpty(queue), "Queue not specified");
            checkArgument(!isNullOrEmpty(exchange), "Exchange not specified");
            return new QueueBindingDefinition(queue, exchange, routingKey);
        }
    }
}
