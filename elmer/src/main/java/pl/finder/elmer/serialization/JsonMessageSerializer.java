package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.Gson;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
final class JsonMessageSerializer implements MessageSerializer {
    private final Gson gson;

    static MessageSerializer createDefault() {
        final Gson gson = DefaultGson.create();
        return create(gson);
    }

    @Override
    public Message serialize(final Object message) {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream();
                final Writer writer = new OutputStreamWriter(output)) {
            gson.toJson(message, writer);
            return Message.builder()
                    .body(output.toByteArray())
                    .type(message.getClass().getCanonicalName())
                    .contentType(MessageContentType.JSON.value())
                    .build();
        } catch (final IOException e) {
            throw new IllegalStateException("Could not write message to JSON", e);
        }
    }

    @Override
    public <T> T deserialize(final Message message) {
        final String type = message.type();
        checkState(!isNullOrEmpty(type), "Unable to determinate messageType");
        final Class<?> messageType = MessageTypes.byName(type);
        try (final InputStream input = message.openStream();
                final Reader reader = new InputStreamReader(input)) {
            @SuppressWarnings("unchecked")
            final T result = (T) gson.fromJson(reader, messageType);
            return result;
        } catch (final IOException e) {
            throw new IllegalStateException("Could not read message from JSON", e);
        }
    }

    @Override
    public <T> T deserialize(final Message message, final Class<T> messageType) {
        try (final InputStream input = message.openStream();
                final Reader reader = new InputStreamReader(input)) {
            return gson.fromJson(reader, messageType);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not read message from JSON", e);
        }
    }

}
