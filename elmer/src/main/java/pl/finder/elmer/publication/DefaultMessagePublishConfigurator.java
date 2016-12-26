package pl.finder.elmer.publication;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap.Builder;

import pl.finder.elmer.publication.MessagePublisher.MessagePublishConfigurator;
import pl.finder.elmer.publication.MessagePublisher.TypedMessagePublishConfigurator;

final class DefaultMessagePublishConfigurator implements MessagePublishConfigurator {
    private final MessagePublisher publisher;
    private final PublishOptions.Builder options;

    DefaultMessagePublishConfigurator(final MessagePublisher publisher, final String exchange) {
        this.publisher = publisher;
        options = PublishOptions.builder()
                .exchange(exchange);
    }

    @Override
    public MessagePublishConfigurator withRoutingKey(final String routingKey) {
        options.routingKey(routingKey);
        return this;
    }

    @Override
    public MessagePublishConfigurator withCorrelationId(final String correlationId) {
        options.correlationId(correlationId);
        return this;
    }

    @Override
    public MessagePublishConfigurator withReplyTo(final String replyTo) {
        options.replyTo(replyTo);
        return this;
    }

    @Override
    public MessagePublishConfigurator withExpirationTime(final Duration expirationTime) {
        options.expirationTime(expirationTime);
        return this;
    }

    @Override
    public MessagePublishConfigurator withDeliveryMode(final DeliveryMode deliveryMode) {
        options.deliveryMode(deliveryMode);
        return this;
    }

    @Override
    public MessagePublishConfigurator withMandatoryPublish(final boolean enabled) {
        options.mandatory(enabled);
        return this;
    }

    @Override
    public MessagePublishConfigurator withImmidiatePublish(final boolean enabled) {
        options.immediate(enabled);
        return this;
    }

    @Override
    public MessagePublishConfigurator withHeader(final String name, final Object value) {
        options.withHeader(name, value);
        return this;
    }

    @Override
    public MessagePublishConfigurator withHeaders(final Consumer<Builder<String, Object>> headers) {
        options.headers(headers);
        return this;
    }

    @Override
    public <T> TypedMessagePublishConfigurator<T> withRoutingKey(final Function<T, String> routingKey) {
        final PublishOptions publishOptions = options.build();
        return new DefaultTypedMessagePublishConfigurator<T>(publisher, publishOptions)
                .withRoutingKey(routingKey);
    }

    @Override
    public <T> TypedMessagePublishConfigurator<T> withCorrelationId(final Function<T, String> correlationId) {
        final PublishOptions publishOptions = options.build();
        return new DefaultTypedMessagePublishConfigurator<T>(publisher, publishOptions)
                .withCorrelationId(correlationId);
    }

    @Override
    public <T> CompletionStage<MessagePublisher> message(final T message) {
        final PublishOptions publishOptions = options.build();
        return publisher.publish(publishOptions, message);
    }

    @Override
    public <T> CompletionStage<MessagePublisher> messages(final Collection<T> messages) {
        final MultiPublishOptions<T> publishOptions = MultiPublishOptions.of(options.build());
        return publisher.publishAll(publishOptions, messages);
    }

}
