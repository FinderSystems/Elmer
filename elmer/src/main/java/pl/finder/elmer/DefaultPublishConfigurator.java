package pl.finder.elmer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.finder.elmer.model.PublishConfig;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultPublishConfigurator implements PublishConfigurator {
	private final MessageBus bus;

	@Override
	public PublishToConfigurator toExchange(final String exchange) {
		return DefaultPublishToConfigurator.ofExchange(bus, exchange);
	}

	@Override
	public PublishToConfigurator toQueue(final String queue) {
		return DefaultPublishToConfigurator.ofQueue(bus, queue);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static class DefaultPublishToConfigurator implements PublishToConfigurator {
		private final MessageBus bus;
		private final Consumer<PublishConfig.Builder> setupTarget;
		private final Map<String, String> headers = new HashMap<>();
		private String correlationId;
		private String replyTo;
		private String routingKey;

		private static PublishToConfigurator ofExchange(final MessageBus bus,
				final String exchange) {
			final Consumer<PublishConfig.Builder> setupTarget = config -> config.exchange(exchange);
			return new DefaultPublishToConfigurator(bus, setupTarget);
		}

		private static PublishToConfigurator ofQueue(final MessageBus bus,
				final String queue) {
			final Consumer<PublishConfig.Builder> setupTarget = config -> config.queue(queue);
			return new DefaultPublishToConfigurator(bus, setupTarget);
		}

		@Override
		public PublishToConfigurator correlatedBy(final String correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		@Override
		public PublishToConfigurator replyTo(final String replyTo) {
			this.replyTo = replyTo;
			return this;
		}

		@Override
		public PublishToConfigurator withRoutingKey(final String routingKey) {
			this.routingKey = routingKey;
			return this;
		}

		@Override
		public PublishToConfigurator withHeaders(final Map<String, String> headers) {
			this.headers.putAll(headers);
			return this;
		}

		@Override
		public <TMessage> void message(final TMessage message) {
			final PublishConfig config = publishConfigBuilder().build();
			bus.publish(config, message);
		}

		@Override
		public void messagesByRoutingKey(final Multimap<String, Object> messages)
				throws MessagingException {
			final PublishConfig.Builder configBuilder = publishConfigBuilder();
			for (final String routingKey : messages.keySet()) {
				for (final Object message : messages.get(routingKey)) {
					final PublishConfig config = configBuilder
							.routingKey(routingKey)
							.build();
					bus.publish(config, message);
				}
			}
		}

		private PublishConfig.Builder publishConfigBuilder() {
			final PublishConfig.Builder configBuilder = PublishConfig.builder()
					.headers(ImmutableMap.copyOf(headers))
					.correlationId(correlationId)
					.replyTo(replyTo)
					.routingKey(routingKey);
			setupTarget.accept(configBuilder);
			return configBuilder;
		}
	}
}
