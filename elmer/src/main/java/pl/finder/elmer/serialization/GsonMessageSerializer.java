package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import pl.finder.elmer.SerializationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GsonMessageSerializer implements MessageSerializer {
    private final Gson gson;
    private final String encoding;

    public static MessageSerializer create(final Gson gson, final String encoding) {
        return new GsonMessageSerializer(gson, encoding);
    }

    public static MessageSerializer create(final Gson gson) {
        return create(gson, "UTF-8");
    }

    public static MessageSerializer createDefault() {
        final Gson gson = new GsonBuilder()
            .create();
        return create(gson);
    }

    @Override
    public <TMessage> byte[] serialize(final TMessage message) {
        checkArgument(message != null, "Message not specified");
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream();
                final Writer writer = new OutputStreamWriter(output, encoding)) {
            gson.toJson(message, writer);
            writer.flush();
            return output.toByteArray();
        } catch (final IOException | JsonIOException e) {
            throw new SerializationException(String.format("Could not serialize message: '%s'",
                    message), e);
        }
    }

    @Override
    public <TMessage> TMessage deserialize(final byte[] message, Class<TMessage> type) {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(message);
                final Reader reader = new InputStreamReader(inputStream, encoding)) {
            return gson.fromJson(reader, type);
        } catch (final IOException | JsonParseException e) {
            throw new SerializationException(String.format("Could not deserialize message: '%s'",
                    new String(message)), e);
        }
    }

    @Override
    public <TMessage> String contentTypeOf(final Class<TMessage> messageType) {
        return "application/json";
    }

    @Override
    public <TMessage> String encodingOf(final Class<TMessage> messageType) {
        return encoding;
    }

}
