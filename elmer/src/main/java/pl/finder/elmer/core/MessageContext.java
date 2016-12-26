package pl.finder.elmer.core;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.BasicProperties;

import pl.finder.elmer.commons.Numbers;
import pl.finder.elmer.publication.DeliveryMode;

public interface MessageContext<T> {

    T body();

    String consumerTag();

    String exchange();

    String routingKey();

    BasicProperties properties();

    void ack();

    default Map<String, Object> headers() {
        final BasicProperties properties = properties();
        final Map<String, Object> headers = properties.getHeaders();
        return headers != null ? ImmutableMap.copyOf(headers) : ImmutableMap.of();
    }

    default Object header(final String name) {
        return headers().get(name);
    }

    default String headerAsString(final String name) {
        final Object value = headers().get(name);
        return value != null ? value.toString() : null;
    }

    default List<String> headerValues(final String name) {
        final Object value = headers().get(name);
        return value != null ?
                stream(value.toString().split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .collect(toList()) : emptyList();
    }

    default Integer headerAsInt(final String name) {
        final Object value = headers().get(name);
        if (value == null) {
            return null;
        } else if (value instanceof Integer) {
            return (Integer) value;
        }
        return Numbers.parseInt(value.toString());
    }

    default Long headerAsLong(final String name) {
        final Object value = headers().get(name);
        if (value == null) {
            return null;
        } else if (value instanceof Long) {
            return (Long) value;
        }
        return Numbers.parseLong(value.toString());
    }

    default String messageId() {
        return properties().getMessageId();
    }

    default String replyTo() {
        return properties().getReplyTo();
    }

    default DeliveryMode deliveryMode() {
        return DeliveryMode.valueOf(properties().getDeliveryMode());
    }

    default Duration expirationTime() {
        final String expiration = properties().getExpiration();
        if (isNullOrEmpty(expiration)) {
            return Duration.ZERO;
        }
        final long expirationMillis = Numbers.parseLong(expiration);
        return Duration.ofMillis(expirationMillis);
    }

    default String encoding() {
        return properties().getContentEncoding();
    }

    default String contentType() {
        return properties().getContentType();
    }

    default String correlationId() {
        return properties().getCorrelationId();
    }

    default String messageType() {
        return properties().getType();
    }

    default Integer priority() {
        return properties().getPriority();
    }

    default Instant timestamp() {
        final Date date = properties().getTimestamp();
        return date != null ? Instant.ofEpochMilli(date.getTime()) : Instant.ofEpochMilli(0L);
    }

    default String appId() {
        return properties().getAppId();
    }

    default String userId() {
        return properties().getUserId();
    }
}
