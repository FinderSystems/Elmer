package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
final class JavaMessageSerializer implements MessageSerializer {

    @Override
    public Message serialize(final Object message) {
        checkNotNull(message, "Message not specified");
        checkArgument(message instanceof Serializable, String.format("Message: %s does not implements %s interface",
                message.getClass().getCanonicalName(), Serializable.class.getCanonicalName()));
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream();
                final ObjectOutputStream objectStream = new ObjectOutputStream(output)) {
            objectStream.writeObject(message);
            objectStream.flush();
            return Message.builder()
                    .body(output.toByteArray())
                    .contentType(MessageContentType.JAVA.value())
                    .build();
        } catch (final IOException e) {
            throw new IllegalStateException("Could not serialize message", e);
        }
    }

    @Override
    public <T> T deserialize(final Message message) {
        try (final InputStream input = message.openStream();
                final ObjectInputStream objectStream = new ObjectInputStream(input)) {
            @SuppressWarnings("unchecked")
            final T object = (T) objectStream.readObject();
            return object;
        } catch (final IOException e) {
            throw new IllegalStateException("Could not serialize message", e);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Could not find message class", e);
        }
    }

    @Override
    public <T> T deserialize(final Message message, final Class<T> messageType) {
        return deserialize(message);
    }

}
