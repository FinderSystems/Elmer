package pl.finder.elmer.serialization;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Getter
@Accessors(fluent = true)
@Builder
public final class Message {
    private final byte[] body;
    private final String type;
    private final String encoding;
    private final String contentType;

    static Message raw(final byte[] body) {
        return new Message(body, null, null, null);
    }

    public InputStream openStream() {
        return new ByteArrayInputStream(body);
    }
}
