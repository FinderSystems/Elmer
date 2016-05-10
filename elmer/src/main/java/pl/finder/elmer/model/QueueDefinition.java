package pl.finder.elmer.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.Duration;

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
public final class QueueDefinition {
    private final String name;
    private final boolean passive;
    private final boolean durable;
    private final boolean exclusive;
    private final boolean autoDeletable;
    private final Duration messageExpirationTime;
    private final Duration expirationTime;
    private final int maximumCapacity;
    private final int maximumCapacityBytes;

    public static QueueDefinition createDefault(final String name) {
        return configurator()
                .name(name)
                .create();
    }

    public static QueueDefinition.Configurator configurator() {
        return new Configurator();
    }

    public QueueDefinition.Configurator reconfigure() {
        return configurator()
                .name(name)
                .passive(passive)
                .durable(durable)
                .exclusive(exclusive)
                .autoDeletable(autoDeletable)
                .messageExpirationTime(messageExpirationTime)
                .expirationTime(expirationTime)
                .maximumCapacity(maximumCapacity)
                .maximumCapacityBytes(maximumCapacityBytes);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    public static final class Configurator {
        private String name;
        private boolean passive;
        private boolean durable = true;
        private boolean exclusive;
        private boolean autoDeletable;
        private Duration messageExpirationTime = Duration.ZERO;
        private Duration expirationTime = Duration.ZERO;
        private int maximumCapacity;
        private int maximumCapacityBytes;

        public QueueDefinition create() {
            checkArgument(!isNullOrEmpty(name), "Queue name not specified");
            return new QueueDefinition(name, passive, durable, exclusive,
                    autoDeletable,
                    messageExpirationTime != null ? messageExpirationTime : Duration.ZERO,
                    expirationTime != null ? expirationTime : Duration.ZERO,
                    maximumCapacity, maximumCapacityBytes);
        }
    }
}
