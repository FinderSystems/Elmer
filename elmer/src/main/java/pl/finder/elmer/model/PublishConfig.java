package pl.finder.elmer.model;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.Duration;
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
	private final Map<String, String> headers;
	private final Duration expirationTime;

	public static PublishConfig.Builder builder() {
		return new PublishConfig.Builder();
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
		private Map<String, String> headers = new HashMap<String, String>();
		private Duration expirationTime = Duration.ZERO;

		public PublishConfig.Builder withHeader(final String name, final String value) {
			if (headers != null) {
				headers = new HashMap<>();
			}
			headers.put(name, value);
			return this;
		}

		public PublishConfig build() {
			checkState(!isNullOrEmpty(exchange) || !isNullOrEmpty(queue),
					"Publish target was not specified: setup exchange or queue directly.");
			checkState(!isNullOrEmpty(exchange) != !isNullOrEmpty(queue),
					"Ambiguous publish taget: select exchange or queue");
			final Map<String, String> publishHeaders = headers != null ?
					ImmutableMap.copyOf(headers) : ImmutableMap.of();
		    return new PublishConfig(exchange, queue, replyTo, correlationId,
		    		routingKey, publishHeaders, expirationTime);
		}
	}
}
