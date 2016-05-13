package pl.finder.elmer.model;

import java.time.Instant;
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
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
public final class Message<TBody> {
    private final String id;
    private final TBody body;
    private final String replyTo;
    private final String correlationId;
    private final Map<String, String> headers;
    private final Instant timestamp;

    public static <T> Message.Builder<T> builder() {
        return new Builder<>();
    }

    public String header(final String name) {
        return headers.get(name);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Builder<T> {
        private String id;
        private T body;
        private String replyTo;
        private String correlationId;
        private Map<String, String> headers;
        private Instant timestamp;

        public Message<T> build() {
            final Map<String, String> messageHeaders = headers != null ?
                    ImmutableMap.copyOf(headers) : ImmutableMap.of();
            return new Message<>(id, body, replyTo, correlationId, messageHeaders, timestamp);
        }
    }
}
