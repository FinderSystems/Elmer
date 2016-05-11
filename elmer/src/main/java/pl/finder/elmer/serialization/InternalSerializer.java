package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import pl.finder.elmer.SerializationException;

import com.google.common.collect.ImmutableMap;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class InternalSerializer implements MessageSerializer {
    private static final Map<Type, Function<byte[], Object>> basicDeserializers =
            ImmutableMap.<Type, Function<byte[], Object>> builder()
            .put(byte[].class, message -> message)
            .put(String.class, message -> new String(message))
            .put(boolean.class, message -> Boolean.parseBoolean(new String(message)))
            .put(Boolean.class, message -> Boolean.parseBoolean(new String(message)))
            .put(byte.class, message -> Byte.parseByte(new String(message)))
            .put(Byte.class, message -> Byte.parseByte(new String(message)))
            .put(short.class, message -> Short.parseShort(new String(message)))
            .put(Short.class, message -> Short.parseShort(new String(message)))
            .put(int.class, message -> Integer.parseInt(new String(message)))
            .put(Integer.class, message -> Integer.parseInt(new String(message)))
            .put(long.class, message -> Long.parseLong(new String(message)))
            .put(Long.class, message -> Long.parseLong(new String(message)))
            .put(float.class, message -> Float.parseFloat(new String(message)))
            .put(Float.class, message -> Float.parseFloat(new String(message)))
            .put(double.class, message -> Double.parseDouble(new String(message)))
            .put(Double.class, message -> Double.parseDouble(new String(message)))
            .build();

    private final MessageSerializer serializer;

    public static MessageSerializer decorate(final MessageSerializer serializer) {
        return new InternalSerializer(serializer);
    }

    @Override
    public <TMessage> byte[] serialize(final TMessage message) throws SerializationException {
        checkArgument(message != null, "Message not specified");
        final Class<?> type = message.getClass();
        if (type == byte[].class) {
            return (byte[]) message;
        } else if (shouldDeserializeToString(type)) {
            return message.toString().getBytes();
        }
        return serializer.serialize(message);
    }

    private boolean shouldDeserializeToString(final Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                type == Boolean.class ||
                Number.class.isAssignableFrom(type);
    }

    @Override
    public <TMessage> TMessage deserialize(final byte[] message, Type type) throws SerializationException {
        final Function<byte[], Object> internalDeserializer = basicDeserializers.get(type);
        if (internalDeserializer != null) {
            return deserializeBasic(message, internalDeserializer);
        }
        return serializer.deserialize(message, type);
    }

    @SuppressWarnings("unchecked")
    private <TMessage>  TMessage deserializeBasic(final byte[] message,
            final Function<byte[], Object> internalDeserializer) {
        try {
            final Object deserialized = internalDeserializer.apply(message);
            return (TMessage) deserialized;
        } catch (final NumberFormatException e) {
            throw new SerializationException(
                    String.format("Message: '%s' has invalid format - expected number", new String(message)), e);
        }
    }
}
