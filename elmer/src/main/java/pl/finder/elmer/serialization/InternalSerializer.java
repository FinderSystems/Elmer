package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.net.MediaType.OCTET_STREAM;

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
            .put(Byte[].class, message -> toObjectArray(message))
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
    private final String encoding;

    public static MessageSerializer decorate(final MessageSerializer serializer) {
        return decorate(serializer, "UTF-8");
    }

    public static MessageSerializer decorate(final MessageSerializer serializer, final String encoding) {
        return new InternalSerializer(serializer, encoding);
    }

    @Override
    public <TMessage> byte[] serialize(final TMessage message) throws SerializationException {
        checkArgument(message != null, "Message not specified");
        final Class<?> type = message.getClass();
        if (serializer.supports(type)) {
            return serializer.serialize(message);
        }
        if (type == byte[].class) {
            return (byte[]) message;
        } else if (type == Byte[].class) {
            return toPrimitiveArray((Byte[]) message);
        }
        return message.toString().getBytes();
    }

    @Override
    public <TMessage> TMessage deserialize(final byte[] message, Class<TMessage> type) throws SerializationException {
        if (serializer.supports(type)) {
            return serializer.deserialize(message, type);
        }
        final Function<byte[], Object> internalDeserializer = basicDeserializers.get(type);
        if (internalDeserializer != null) {
            return deserializeBasic(message, internalDeserializer);
        } else if (type.isEnum()) {
            return deserializeEnum(message, type);
        }
        throw new IllegalArgumentException(String.format("Deserializer not found for type: '%s'", type));
    }

    @SuppressWarnings("unchecked")
    private <TMessage> TMessage deserializeEnum(final byte[] message, Class<TMessage> type) {
        @SuppressWarnings("rawtypes")
        final Object deserialized = Enum.valueOf((Class) type, new String(message));
        return (TMessage) deserialized;
    }

    @SuppressWarnings("unchecked")
    private <TMessage> TMessage deserializeBasic(final byte[] message,
            final Function<byte[], Object> internalDeserializer) {
        try {
            final Object deserialized = internalDeserializer.apply(message);
            return (TMessage) deserialized;
        } catch (final NumberFormatException e) {
            throw new SerializationException(
                    String.format("Message: '%s' has invalid format - expected number", new String(message)), e);
        }
    }

    @Override
    public <TMessage> String contentTypeOf(final Class<TMessage> messageType) {
        if (serializer.supports(messageType)) {
            return serializer.contentTypeOf(messageType);
        } else if (messageType == byte[].class) {
            return OCTET_STREAM.type();
        }
        return "text/plain";
    }

    @Override
    public <TMessage> String encodingOf(final Class<TMessage> messageType) {
        if (serializer.supports(messageType)) {
            return serializer.encodingOf(messageType);
        } else if (messageType == byte[].class || messageType == Byte[].class) {
            return null;
        }
        return encoding;
    }

    @Override
    public boolean supports(final Class<?> type) {
        return true;
    }

    private static byte[] toPrimitiveArray(final Byte[] bytes) {
        final byte[] results = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            results[i] = bytes[i] != null ? bytes[i] : 0;
        }
        return results;
    }

    private static Byte[] toObjectArray(final byte[] bytes) {
        final Byte[] results = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            results[i] = bytes[i];
        }
        return results;
    }
}
