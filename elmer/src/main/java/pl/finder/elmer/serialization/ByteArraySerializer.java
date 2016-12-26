package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
final class ByteArraySerializer implements MessageSerializer {

    @Override
    public Message serialize(final Object message) {
        checkArgument(message instanceof byte[],
                String.format("Unsupported message type: %s, expected byte[]", message.getClass()));
        return Message.raw((byte[]) message);
    }

    @Override
    public <T> T deserialize(final Message message) {
        @SuppressWarnings("unchecked")
        final T result = (T) message.body();
        return result;
    }

    @Override
    public <T> T deserialize(final Message message, final Class<T> messageType) {
        checkArgument(messageType == byte[].class,
                String.format("Unsupported message type: %s, expected byte[]", messageType));
        return deserialize(message);
    }

}
