package pl.finder.elmer.publication;

import static java.util.Arrays.asList;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

public interface MessagePublisher extends AutoCloseable {

    <T> CompletionStage<MessagePublisher> publish(PublishOptions options, T message);

    <T> CompletionStage<MessagePublisher> publishAll(MultiPublishOptions<T> options, Collection<T> messages);

    default <T> CompletionStage<MessagePublisher> publishAll(final MultiPublishOptions<T> options,
            @SuppressWarnings("unchecked") final T... messages) {
        final List<T> messagesList = asList(messages);
        return publishAll(options, messagesList);
    }

    <T> QueuePublisher publishFrom(MultiPublishOptions<T> options, BlockingQueue<T> messageQueue);

    @Override
    void close();

    default FluentMessagePublisher publish() {
        return exchange -> new DefaultMessagePublishConfigurator(this, exchange);
    }

    @FunctionalInterface
    public static interface FluentMessagePublisher {

        MessagePublishConfigurator toExchange(String exchange);
    }

    public static interface MessagePublishConfigurator {

        MessagePublishConfigurator withRoutingKey(String routingKey);

        <T> TypedMessagePublishConfigurator<T> withRoutingKey(Function<T, String> routingKey);

        <T> TypedMessagePublishConfigurator<T> withCorrelationId(Function<T, String> correlationId);

        MessagePublishConfigurator withCorrelationId(String correlationId);

        MessagePublishConfigurator withReplyTo(String replyTo);

        MessagePublishConfigurator withExpirationTime(Duration expirationTime);

        MessagePublishConfigurator withDeliveryMode(DeliveryMode deliveryMode);

        MessagePublishConfigurator withMandatoryPublish(boolean enabled);

        MessagePublishConfigurator withImmidiatePublish(boolean enabled);

        MessagePublishConfigurator withHeader(String name, Object value);

        MessagePublishConfigurator withHeaders(Consumer<ImmutableMap.Builder<String, Object>> headers);

        <T> CompletionStage<MessagePublisher> message(T message);

        <T> CompletionStage<MessagePublisher> messages(Collection<T> messages);

        default MessagePublishConfigurator withHeaders(final Map<String, Object> headers) {
            return withHeaders(headersBuilder -> headersBuilder.putAll(headers));
        }

        default MessagePublishConfigurator withPersitentDeliveryMode() {
            return withDeliveryMode(DeliveryMode.Persistent);
        }

        default MessagePublishConfigurator withNonPersitentDeliveryMode() {
            return withDeliveryMode(DeliveryMode.NonPersistent);
        }

        default MessagePublishConfigurator withMandatoryPublishEnabled() {
            return withMandatoryPublish(true);
        }

        default MessagePublishConfigurator withMandatoryPublishDisabled() {
            return withMandatoryPublish(false);
        }

        default MessagePublishConfigurator withImmidiatePublishEnabled() {
            return withImmidiatePublish(true);
        }

        default MessagePublishConfigurator withImmidiatePublishDisabled() {
            return withImmidiatePublish(false);
        }
    }

    public static interface TypedMessagePublishConfigurator<T> {
        TypedMessagePublishConfigurator<T> withRoutingKey(Function<T, String> routingKey);

        TypedMessagePublishConfigurator<T> withCorrelationId(Function<T, String> correlationId);

        TypedMessagePublishConfigurator<T> withReplyTo(String replyTo);

        TypedMessagePublishConfigurator<T> withExpirationTime(Duration expirationTime);

        TypedMessagePublishConfigurator<T> withDeliveryMode(DeliveryMode deliveryMode);

        TypedMessagePublishConfigurator<T> withMandatoryPublish(boolean enabled);

        TypedMessagePublishConfigurator<T> withImmidiatePublish(boolean enabled);

        TypedMessagePublishConfigurator<T> withHeader(String name, Object value);

        TypedMessagePublishConfigurator<T> withHeaders(Consumer<ImmutableMap.Builder<String, Object>> headers);

        CompletionStage<MessagePublisher> message(T message);

        CompletionStage<MessagePublisher> messages(Collection<T> messages);

        default TypedMessagePublishConfigurator<T> withRoutingKey(final String routingKey) {
            return withRoutingKey(message -> routingKey);
        }

        default TypedMessagePublishConfigurator<T> withCorrelationId(final String correlationId) {
            return withCorrelationId(message -> correlationId);
        }

        default TypedMessagePublishConfigurator<T> withHeaders(final Map<String, Object> headers) {
            return withHeaders(headersBuilder -> headersBuilder.putAll(headers));
        }

        default TypedMessagePublishConfigurator<T> withPersitentDeliveryMode() {
            return withDeliveryMode(DeliveryMode.Persistent);
        }

        default TypedMessagePublishConfigurator<T> withNonPersitentDeliveryMode() {
            return withDeliveryMode(DeliveryMode.NonPersistent);
        }

        default TypedMessagePublishConfigurator<T> withMandatoryPublishEnabled() {
            return withMandatoryPublish(true);
        }

        default TypedMessagePublishConfigurator<T> withMandatoryPublishDisabled() {
            return withMandatoryPublish(false);
        }

        default TypedMessagePublishConfigurator<T> withImmidiatePublishEnabled() {
            return withImmidiatePublish(true);
        }

        default TypedMessagePublishConfigurator<T> withImmidiatePublishDisabled() {
            return withImmidiatePublish(false);
        }
    }
}
