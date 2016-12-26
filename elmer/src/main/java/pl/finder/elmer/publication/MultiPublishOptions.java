package pl.finder.elmer.publication;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString(exclude = {"routingKey", "correlationId", "errorHandler"})
public final class MultiPublishOptions<T> {
    @Getter
    private final String exchange;
    @Getter
    private final boolean mandatory;
    @Getter
    private final boolean immediate;
    @Getter
    private final Integer priority;
    @Getter
    private final DeliveryMode deliveryMode;
    @Getter
    private final String replyTo;
    @Getter
    private final Duration expirationTime;
    @Getter
    private final Map<String, Object> headers;
    private final transient Function<T, String> routingKey;
    private final transient Function<T, String> correlationId;
    @Getter
    private final boolean continueOnError;
    private final transient BiConsumer<T, Exception> errorHandler;

    public static <T> MultiPublishOptions.Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> MultiPublishOptions<T> of(final PublishOptions options) {
        return MultiPublishOptions.<T> builder()
                .exchange(options.exchange())
                .routingKey(options.routingKey())
                .mandatory(options.mandatory())
                .immediate(options.immediate())
                .priority(options.priority())
                .deliveryMode(options.deliveryMode())
                .correlationId(options.correlationId())
                .replyTo(options.replyTo())
                .expirationTime(options.expirationTime())
                .headers(options.headers())
                .build();
    }

    public PublishOptions optionsOf(final T message) {
        return PublishOptions.builder()
                .exchange(exchange)
                .routingKey(routingKey.apply(message))
                .mandatory(mandatory)
                .immediate(immediate)
                .priority(priority)
                .deliveryMode(deliveryMode)
                .correlationId(correlationId.apply(message))
                .replyTo(replyTo)
                .expirationTime(expirationTime)
                .headers(headers)
                .build();
    }

    public void closed(final T message) {
        errorHandler.accept(message, new InterruptedException("Publishing interrupted: publisher has been closed"));
    }

    public void interrupted(final T message) {
        errorHandler.accept(message, new InterruptedException("Publishing interrupted by previous error"));
    }

    public boolean shouldContinueOnError(final T message, final Exception error) {
        errorHandler.accept(message, error);
        return continueOnError;
    }

    public MultiPublishOptions.Builder<T> with() {
        return MultiPublishOptions.<T>builder()
                .exchange(exchange)
                .routingKey(routingKey)
                .mandatory(mandatory)
                .immediate(immediate)
                .priority(priority)
                .deliveryMode(deliveryMode)
                .correlationId(correlationId)
                .replyTo(replyTo)
                .expirationTime(expirationTime)
                .headers(headers)
                .continueOnError(continueOnError)
                .errorHandler(errorHandler);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Builder<T> {
        private String exchange;
        private boolean mandatory;
        private boolean immediate;
        private Integer priority;
        private DeliveryMode deliveryMode;
        private String replyTo;
        private Duration expirationTime = Duration.ZERO;
        private Map<String, Object> headers = ImmutableMap.of();
        private Function<T, String> routingKey = message -> null;
        private Function<T, String> correlationId = message -> null;
        private boolean continueOnError;
        private BiConsumer<T, Exception> errorHandler = ErrorHandler.create();

        public Builder<T> headers(final Consumer<ImmutableMap.Builder<String, Object>> headers) {
            final ImmutableMap.Builder<String, Object> headersBuilder = ImmutableMap.builder();
            headers.accept(headersBuilder);
            this.headers = headersBuilder.build();
            return this;
        }

        public Builder<T> headers(final Map<String, Object> headers) {
            return headers(builder -> builder.putAll(headers));
        }

        public Builder<T> withHeader(final String name, final Object value) {
            return headers(builder -> builder
                    .putAll(headers)
                    .put(name, value));
        }

        public Builder<T> routingKey(final Function<T, String> routingKey) {
            this.routingKey = routingKey != null ? routingKey: message -> null;
            return this;
        }

        public Builder<T> routingKey(final String routingKey) {
            return routingKey(message -> routingKey);
        }

        public Builder<T> correlationId(final Function<T, String> correlationId) {
            this.correlationId = correlationId != null ? correlationId : message -> null;
            return this;
        }

        public Builder<T> correlationId(final String correlationId) {
            return correlationId(message -> correlationId);
        }

        public Builder<T> errorHandler(final BiConsumer<T, Exception> errorHandler) {
            this.errorHandler = errorHandler != null ? errorHandler : (message, exception) -> {};
            return this;
        }

        public MultiPublishOptions<T> build() {
            checkState(!isNullOrEmpty(exchange), "Exchange not specified");
            checkState(priority == null || (priority >=0 && priority <= 255),
                    String.format("Invalid priority: %s, expected null or <0 - 255>", priority));
            checkState(expirationTime == null || !expirationTime.isNegative(),
                    String.format("Invalid expirationTime: %dms, expected >=0", expirationTime.toMillis()));

            return new MultiPublishOptions<>(exchange, mandatory, immediate, priority, deliveryMode,
                    replyTo, expirationTime, headers, routingKey, correlationId,
                    continueOnError, errorHandler != null ? errorHandler : ErrorHandler.create());
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE, staticName = "create")
    @Slf4j
    private static class ErrorHandler<T> implements BiConsumer<T, Exception> {
        @Override
        public void accept(final T message, final Exception e) {
            log.error(String.format("Error while publishing: '%s'", message), e);
        }
    }
}
