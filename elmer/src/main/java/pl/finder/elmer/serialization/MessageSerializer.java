package pl.finder.elmer.serialization;

import pl.finder.elmer.SerializationException;


public interface MessageSerializer {

    <TMessage> byte[] serialize(TMessage message)
            throws SerializationException;

    <TMessage> TMessage deserialize(byte[] message, Class<TMessage> type)
            throws SerializationException;

    <TMessage> String contentTypeOf(Class<TMessage> messageType);

    <TMessage> String encodingOf(Class<TMessage> messageType);

    default boolean supports(Class<?> type) {
        return type != null &&
                !type.isPrimitive() &&
                !type.isEnum() &&
                type != String.class &&
                type != Boolean.class &&
                type != byte[].class &&
                type != Byte[].class &&
                !Number.class.isAssignableFrom(type);
    }
}
