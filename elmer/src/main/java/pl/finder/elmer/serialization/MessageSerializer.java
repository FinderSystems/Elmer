package pl.finder.elmer.serialization;

public interface MessageSerializer {

    Message serialize(Object message);

    <T> T deserialize(Message message);

    <T> T deserialize(Message message, Class<T> messageType);

    /**
     * Configurator of MessageSerializer.
     */
    public static interface Configurator {
        Configurator clearDefaults();

        Configurator setDefault(MessageSerializer serializer);

        Configurator setDefault(MessageContentType contentType);

        Configurator register(String contentType, MessageSerializer serializer);

        Configurator register(Class<?> messageType, MessageSerializer serializer);
    }
}
