package pl.finder.elmer.publication;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap.Builder;

import pl.finder.elmer.publication.MessagePublisher.TypedMessagePublishConfigurator;

final class DefaultTypedMessagePublishConfigurator<T> implements TypedMessagePublishConfigurator<T> {
    private final MessagePublisher publisher;
    private final MultiPublishOptions.Builder<T> options;

    DefaultTypedMessagePublishConfigurator(final MessagePublisher publisher, final PublishOptions options) {
        this.publisher = publisher;
        this.options = MultiPublishOptions.<T> of(options)
                .with();
    }

    @Override
    public TypedMessagePublishConfigurator<T> withRoutingKey(final Function<T, String> routingKey) {
        options.routingKey(routingKey);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withCorrelationId(final Function<T, String> correlationId) {
        options.correlationId(correlationId);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withReplyTo(final String replyTo) {
        options.replyTo(replyTo);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withExpirationTime(final Duration expirationTime) {
        options.expirationTime(expirationTime);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withDeliveryMode(final DeliveryMode deliveryMode) {
        options.deliveryMode(deliveryMode);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withMandatoryPublish(final boolean enabled) {
        options.mandatory(enabled);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withImmidiatePublish(final boolean enabled) {
        options.immediate(enabled);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withHeader(final String name, final Object value) {
        options.withHeader(name, value);
        return this;
    }

    @Override
    public TypedMessagePublishConfigurator<T> withHeaders(final Consumer<Builder<String, Object>> headers) {
        options.headers(headers);
        return this;
    }

    @Override
    public CompletionStage<MessagePublisher> message(final T message) {
        final PublishOptions publishOptions = options.build().optionsOf(message);
        return publisher.publish(publishOptions, message);
    }

    @Override
    public CompletionStage<MessagePublisher> messages(final Collection<T> messages) {
        final MultiPublishOptions<T> publishOptions = options.build();
        return publisher.publishAll(publishOptions, messages);
    }

}
