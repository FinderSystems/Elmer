package pl.finder.elmer.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import com.google.common.collect.ImmutableMap;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class PublishConfig {
    private final String exchange;
    private final String queue;
    private final String replyTo;
    private final String correlationId;
    private final String routingKey;
    private final boolean mandatory;
    private final boolean immediate;
    private final MessageDeliveryMode deliveryMode;
    private final Map<String, Object> headers;

    public static PublishConfig.Builder builder() {
        return new PublishConfig.Builder();
    }

    public PublishConfig.Builder with() {
        return builder()
                .exchange(exchange)
                .queue(queue)
                .replyTo(replyTo)
                .correlationId(correlationId)
                .routingKey(routingKey)
                .mandatory(mandatory)
                .immediate(immediate)
                .headersRaw(headers)
                .deliveryMode(deliveryMode);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Builder {
        private String exchange;
        private String queue;
        private String replyTo;
        private String correlationId;
        private String routingKey;
        private boolean mandatory;
        private boolean immediate;
        private MessageDeliveryMode deliveryMode = MessageDeliveryMode.NonPersistent;
        private Map<String, Object> headers = new HashMap<>();

        private PublishConfig.Builder headersRaw(final Map<String, Object> headers) {
            this.headers = headers;
            return this;
        }

        public PublishConfig.Builder headers(final Map<String, String> headers) {
            this.headers = headers.entrySet().stream()
                    .collect(toMap(entry -> entry.getKey(), entry -> entry.getValue()));
            return this;
        }

        public PublishConfig.Builder withHeader(final String name, final String value) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(name, value);
            return this;
        }

        public PublishConfig build() {
            checkArgument(!isNullOrEmpty(exchange) || !isNullOrEmpty(queue),
                    "Publish target was not specified: setup exchange or queue.");
            checkState(!isNullOrEmpty(exchange) != !isNullOrEmpty(queue),
                    "Ambiguous publish taget: select exchange or queue");
            final Map<String, Object> publishHeaders = headers != null ?
                    ImmutableMap.copyOf(headers) : ImmutableMap.of();
            return new PublishConfig(exchange, queue, replyTo, correlationId,
                    routingKey, mandatory, immediate, deliveryMode, publishHeaders);
        }
    }
}
