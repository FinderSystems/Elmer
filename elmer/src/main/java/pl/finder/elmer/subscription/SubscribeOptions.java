package pl.finder.elmer.subscription;

import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import java.util.function.BiConsumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Options for created subscription.
 */
@EqualsAndHashCode
@ToString
@Getter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubscribeOptions<T> {
    /**
     * Type of consumed message.
     */
    private final Class<T> messageType;
    /**
     * Name of the queue .
     */
    private final String queue;
    /**
     * Is automatic message acknowledging is enabled.
     */
    private final boolean autoAckEnabled;

    private final boolean noLocal;

    private final boolean exclusive;

    private final String consumerTag;

    private final int concurrentThreadsLimit;

    private final BiConsumer<SubscribeOptions<?>, Exception> errorHandler;

    /**
     * Returns builder of SubscribeOptions.
     *
     * @return SubscribeOptionsBuilder
     */
    public static <T> SubscribeOptions.Builder<T> builder() {
        return new Builder<>();
    }

    public SubscribeOptions.Builder<T> with() {
        return SubscribeOptions.<T> builder()
                .messageType(messageType)
                .queue(queue)
                .autoAckEnabled(autoAckEnabled)
                .noLocal(noLocal)
                .exclusive(exclusive)
                .consumerTag(consumerTag)
                .concurrentThreadsLimit(concurrentThreadsLimit)
                .errorHandler(errorHandler);
    }

    public void onError(final Exception e) {
        errorHandler.accept(this, e);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    @Setter
    public static final class Builder<T> {
        private static final BiConsumer<SubscribeOptions<?>, Exception> DefaltErrorHandler = ErrorHandler.create();

        private Class<T> messageType;
        /**
         * Sets name of the queue.
         *
         * @param queue queue name
         * @returns self
         */
        private String queue;

        /**
         * Sets automatic message acknowledging settings.
         *
         * @param autoAckEnabled true - enabled, false - disabled
         * @returns self
         */
        private boolean autoAckEnabled = true;

        private boolean noLocal = false;

        private boolean exclusive = false;

        private String consumerTag;

        private int concurrentThreadsLimit = 1;

        private BiConsumer<SubscribeOptions<?>, Exception> errorHandler = DefaltErrorHandler;
        /**
         * Builds SubscribeOptions.
         *
         * @return SubscribeOptions
         */
        public SubscribeOptions<T> build() {
            checkState(!isNullOrEmpty(queue), "Queue name was not specfied");
            checkState(concurrentThreadsLimit > 0,
                    String.format("Invalid concurrentThreadsLimit: %d, expected > 0", concurrentThreadsLimit));
            return new SubscribeOptions<>(messageType, queue, autoAckEnabled, noLocal,
                    exclusive, consumerTag, concurrentThreadsLimit,
                    errorHandler != null ? errorHandler : DefaltErrorHandler);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE, staticName = "create")
    @Slf4j
    private static class ErrorHandler implements BiConsumer<SubscribeOptions<?>, Exception> {

        @Override
        public void accept(final SubscribeOptions<?> options, final Exception e) {
            log.error(String.format("Error while consuming message from queue: '%s'", options.queue()), e);
        }
    }
}
