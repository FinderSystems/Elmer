package pl.finder.elmer.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import pl.finder.elmer.model.Message1;

public class GsonMessageSerializerTest {

    @Test
    public void shouldSerializeMessageToJson() {
        // given
        final MessageSerializer serializer = GsonMessageSerializer.createDefault();
        final Message1 message = Message1.of(1, "this is a test");

        // then
        final byte[] serialized = serializer.serialize(message);

        // then
        assertThat(new String(serialized)).isEqualTo("{\"id\":1,\"value\":\"this is a test\"}");
    }

    @Test
    public void shouldDeserializeMessageFromJson() {
        // given
        final MessageSerializer serializer = GsonMessageSerializer.createDefault();
        final byte[] serialized = "{\"id\":1,\"value\":\"this is a test\"}".getBytes();

        // when
        final Message1 message = serializer.deserialize(serialized, Message1.class);

        // then
        assertThat(message).isEqualTo(Message1.of(1, "this is a test"));
    }
}
