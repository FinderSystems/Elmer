package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import pl.finder.elmer.commons.Numbers;

@AllArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
final class NumberSerializer implements MessageSerializer {
    private static final Map<Class<?>, Function<String, Number>> Parsers =
            ImmutableMap.<Class<?>, Function<String, Number>> builder()
            .put(Byte.class, Numbers::parseByte)
            .put(Short.class, Numbers::parseShort)
            .put(Integer.class, Numbers::parseInt)
            .put(Long.class, Numbers::parseLong)
            .put(Float.class, Numbers::parseFloat)
            .put(Double.class, Numbers::parseDouble)
            .build();

    void register(final Map<String, MessageSerializer> registery) {
        Parsers.keySet().stream().forEach(type -> registery.put(type.getCanonicalName(), this));
    }

    @Override
    public Message serialize(final Object message) {
        checkArgument(message instanceof Number,
                String.format("Unsupported message type: %s, expected instance of java.lang.Number",
                        message.getClass().getCanonicalName()));
        final byte[] body = message.toString().getBytes();
        return Message.builder()
                .body(body)
                .type(message.getClass().getCanonicalName())
                .build();
    }

    @Override
    public <T> T deserialize(final Message message) {
        final String type = message.type();
        checkState(!isNullOrEmpty(type), "Unable to determinate messageType");
        final Class<?> messageType = MessageTypes.byName(type);
        return doDeserialize(message, messageType);
    }

    @Override
    public <T> T deserialize(final Message message, final Class<T> messageType) {
        return doDeserialize(message, messageType);
    }

    private static <T> T doDeserialize(final Message message, final Class<?> messageType) {
        final Function<String, Number> handler = Parsers.get(messageType);
        checkArgument(handler != null, String.format("Unsupported message type: %s", messageType.getCanonicalName()));
        final String value = new String(message.body(), message.encoding() != null ?
                Charset.forName(message.encoding()) : Charsets.UTF_8);
        @SuppressWarnings("unchecked")
        final T result = (T) handler.apply(value);
        return result;
    }

}
