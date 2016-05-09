package pl.finder.elmer;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GsonMessageSerializer implements MessageSerializer {
	private final Gson gson;

	public static MessageSerializer create(final Gson gson) {
		return new GsonMessageSerializer(gson);
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
				final Writer writer = new OutputStreamWriter(output)) {
			gson.toJson(message, writer);
			return output.toByteArray();
		} catch (final IOException | JsonIOException e) {
			throw new SerializationException(String.format("Could not serialize: '%s'",
					message), e);
		}
	}

	@Override
	public <TMessage> TMessage deserialize(final byte[] message, Type type) {
		try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(message);
				final Reader reader = new InputStreamReader(inputStream)) {
			return gson.fromJson(reader, type);
		} catch (final IOException | JsonParseException e) {
			throw new SerializationException(String.format("Could not serialize: '%s'",
					new String(message)), e);
		}
	}


}
