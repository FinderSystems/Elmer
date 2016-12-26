package pl.finder.elmer.publication;


import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Options of published message.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Getter
@Accessors(fluent = true)
public final class PublishOptions {
    private final String exchange;
    private final String routingKey;
    private final boolean mandatory;
    private final boolean immediate;
    private final Integer priority;
    private final DeliveryMode deliveryMode;
    private final String correlationId;
    private final String replyTo;
    private final Duration expirationTime;
    private final Map<String, Object> headers;

    public static PublishOptions.Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    public static final class Builder {
        private String exchange;
        private String routingKey;
        private boolean mandatory;
        private boolean immediate;
        private Integer priority;
        private DeliveryMode deliveryMode;
        private String correlationId;
        private String replyTo;
        private Duration expirationTime = Duration.ZERO;
        private Map<String, Object> headers = ImmutableMap.of();

        public Builder headers(final Map<String, Object> headers) {
            this.headers = ImmutableMap.copyOf(headers);
            return this;
        }

        public Builder headers(final Consumer<ImmutableMap.Builder<String, Object>> headers) {
            final ImmutableMap.Builder<String, Object> headersBuilder = ImmutableMap.builder();
            headers.accept(headersBuilder);
            this.headers = headersBuilder.build();
            return this;
        }

        public Builder withHeader(final String name, final Object value) {
            return headers(builder -> builder
                    .putAll(headers)
                    .put(name, value));
        }

        public PublishOptions build() {
            checkState(!isNullOrEmpty(exchange), "Exchange not specified");
            checkState(priority == null || (priority >=0 && priority <= 255),
                    String.format("Invalid priority: %s, expected null or <0 - 255>", priority));
            checkState(expirationTime == null || !expirationTime.isNegative(),
                    String.format("Invalid expirationTime: %dms, expected >=0", expirationTime.toMillis()));

            return new PublishOptions(exchange, routingKey, mandatory, immediate, priority,
                    deliveryMode, correlationId, replyTo,
                    expirationTime != null ? expirationTime : Duration.ZERO, headers);
        }
    }
}
