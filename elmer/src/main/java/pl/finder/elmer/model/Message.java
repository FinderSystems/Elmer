package pl.finder.elmer.model;

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
@EqualsAndHashCode
@ToString
public final class Message<TBody> {
	@Getter
	private final String id;
	@Getter
	private final TBody body;
	@Getter
	private final String replyTo;
	@Getter
	private final String correlationId;
	@Getter
	private final String routingKey;
	@Getter
	private final Map<String, String> headers;

	public static <T> Message.Builder<T> builder() {
		return new Builder<T>();
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
		private String routingKey;
		private Map<String, String> headers;

		public Message<T> build() {
			final Map<String, String> messageHeaders = headers != null ?
					ImmutableMap.copyOf(headers) : ImmutableMap.of();
			return new Message<>(id, body, replyTo, correlationId, routingKey, messageHeaders);
		}
	}
}
