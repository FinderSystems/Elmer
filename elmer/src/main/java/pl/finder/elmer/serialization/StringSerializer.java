package pl.finder.elmer.serialization;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class StringSerializer implements MessageSerializer {
    private final Charset charset;

    static MessageSerializer create(final Charset charset) {
        return new StringSerializer(charset);
    }

    static MessageSerializer create(final String charset) {
        return create(Charset.forName(charset));
    }

    static MessageSerializer create() {
        return create(Charsets.UTF_8);
    }

    @Override
    public Message serialize(final Object message) {
        final byte[] body = message.toString().getBytes(charset);
        return Message.builder()
                .body(body)
                .encoding(charset.name())
                .build();
    }

    @Override
    public <T> T deserialize(final Message message) {
        @SuppressWarnings("unchecked")
        final T result = (T) deserialize(message, String.class);
        return result;
    }

    @Override
    public <T> T deserialize(final Message message, final Class<T> messageType) {
        checkArgument(messageType == String.class,
                String.format("Unsupported message type: %s, expected java.lang.String",
                        messageType.getCanonicalName()));
        @SuppressWarnings("unchecked")
        final T result = (T) new String(message.body(), message.encoding() != null ?
                Charset.forName(message.encoding()) : charset);
        return result;
    }

}
