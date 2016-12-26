package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toMap;
import static pl.finder.elmer.serialization.MessageContentType.JAVA;
import static pl.finder.elmer.serialization.MessageContentType.JSON;
import static pl.finder.elmer.serialization.MessageContentType.XML;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public final class DelegatingMessageSerializer implements MessageSerializer {
    private final Map<String, MessageSerializer> typeSerializers;
    private final Map<String, MessageSerializer> contentTypeSerializers;
    private final MessageSerializer defaultSerializer;

    private DelegatingMessageSerializer(final DefaultConfigurator configurator) {
        typeSerializers = ImmutableMap.copyOf(configurator.typeSerializers);
        contentTypeSerializers = ImmutableMap.copyOf(configurator.contentTypeSerializers);
        defaultSerializer = configurator.defaultSerializer;
    }

    public static MessageSerializer create(final Consumer<Configurator> options) {
        final DefaultConfigurator configurator = DefaultConfigurator.create();
        options.accept(configurator);
        return new DelegatingMessageSerializer(configurator);
    }

    @Override
    public Message serialize(final Object message) {
        checkNotNull(message, "Cannot serialize null message");
        final MessageSerializer serializer = typeSerializers.getOrDefault(message.getClass(), defaultSerializer);
        return serializer.serialize(message);
    }

    @Override
    public <T> T deserialize(final Message message) {
        checkNotNull(message, "Cannot deserialize null message");
        final MessageSerializer serializer = serializerFor(message);
        return serializer.deserialize(message);
    }

    @Override
    public <T> T deserialize(final Message message, final Class<T> messageType) {
        checkNotNull(message, "Cannot deerialize null message");
        final MessageSerializer serializer = serializerFor(message);
        return serializer.deserialize(message, messageType);
    }

    private MessageSerializer serializerFor(final Message message) {
        return serializerFor(message, null);
    }

    private MessageSerializer serializerFor(final Message message, final Class<?> messageType) {
        if (message.contentType() != null && contentTypeSerializers.containsKey(message.contentType())) {
            return contentTypeSerializers.get(message.contentType());
        } else if (messageType != null && typeSerializers.containsKey(messageType.getCanonicalName())) {
            return typeSerializers.get(messageType.getCanonicalName());
        } else if (message.type() != null && typeSerializers.containsKey(message.type())) {
            return typeSerializers.get(message.type());
        }
        return defaultSerializer;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE, staticName = "create")
    private static class DefaultConfigurator implements MessageSerializer.Configurator {
        private static final Map<MessageContentType, MessageSerializer> DefaultSerializers = ImmutableMap.of(
                JSON, JsonMessageSerializer.createDefault(),
                XML, XmlMessageSerializer.create(),
                JAVA, JavaMessageSerializer.create());

        private final Map<String, MessageSerializer> typeSerializers = createDefaultTypSerializers();
        private final Map<String, MessageSerializer> contentTypeSerializers = DefaultSerializers.entrySet()
                .stream().collect(toMap(entry -> entry.getKey().value(), Entry::getValue));
        private MessageSerializer defaultSerializer = DefaultSerializers.get(JSON);

        @Override
        public Configurator clearDefaults() {
            typeSerializers.clear();
            contentTypeSerializers.clear();
            return this;
        }

        @Override
        public Configurator setDefault(final MessageSerializer serializer) {
            checkNotNull(serializer, "Default serializer cannot be null");
            defaultSerializer = serializer;
            return this;
        }

        @Override
        public Configurator setDefault(final MessageContentType contentType) {
            checkNotNull(contentType, "ContentType not specified");
            checkArgument(DefaultSerializers.containsKey(contentType),
                    String.format("ContentType: '%s' is not supported", contentType));
            return setDefault(DefaultSerializers.get(contentType));
        }

        @Override
        public Configurator register(final String contentType, final MessageSerializer serializer) {
            checkNotNull(contentType, "Cannot register serializer to null contentType");
            checkArgument(!isNullOrEmpty(contentType), "Cannot register serializer to empty content type");
            checkNotNull(serializer, String.format("Cannot register null serializer to: '%s' content type",
                    contentType));
            contentTypeSerializers.put(contentType, serializer);
            return this;
        }

        @Override
        public Configurator register(final Class<?> messageType, final MessageSerializer serializer) {
            checkNotNull(messageType, "Cannot register serializer to null messageType");
            checkNotNull(serializer, String.format("Cannot register null serializer to: '%s' messageType",
                    messageType.getCanonicalName()));
            typeSerializers.put(messageType.getCanonicalName(), serializer);
            return this;
        }

        private static Map<String, MessageSerializer> createDefaultTypSerializers() {
            final Map<String, MessageSerializer> typeSerializers = new HashMap<>();
            final NumberSerializer numberSerializer = NumberSerializer.create();
            typeSerializers.put(byte[].class.getCanonicalName(), ByteArraySerializer.create());
            typeSerializers.put(String.class.getCanonicalName(), StringSerializer.create());
            numberSerializer.register(typeSerializers);
            return typeSerializers;
        }
    }
}
