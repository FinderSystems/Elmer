package pl.finder.elmer.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static pl.finder.elmer.util.AssertionBuilder.makeAssertion;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import pl.finder.elmer.SerializationException;
import pl.finder.elmer.model.Message1;
import pl.finder.elmer.util.AssertionBuilder.MessageDeserializationAssertion;
import pl.finder.elmer.util.AssertionBuilder.MessageDeserializationErrorAssertion;
import pl.finder.elmer.util.AssertionBuilder.MessageSerializationAssertion;

@RunWith(JUnitParamsRunner.class)
public class InternalDeserializerTest {

    @Test
    public void shouldThrowArgumentExceptionOnNullMessageSerialization() {
        // given
        final Object message = null;
        final MessageSerializer decorated = mock(MessageSerializer.class);
        final MessageSerializer internalSerializer = InternalSerializer.decorate(decorated);

        // when
        final Throwable caughtException = catchThrowable(() -> internalSerializer.serialize(message));

        // then
        assertThat(caughtException).isNotNull()
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Message not specified");
    }

    @Test
    public void shouldPassSeserializationToDecoratedSerializerOnComplexType() {
        // given
        final MessageSerializer decorated = mock(MessageSerializer.class);
        final MessageSerializer internalSerializer = InternalSerializer.decorate(decorated);
        final Message1 message = Message1.of(1, "test");

        // when
        internalSerializer.serialize(message);

        // then
        verify(decorated, Mockito.times(1)).serialize(message);
    }

    @Test
    public void shouldPassDeserializationToDecoratedSerializerOnComplexType() {
        // given
        final MessageSerializer decorated = mock(MessageSerializer.class);
        final MessageSerializer internalSerializer = InternalSerializer.decorate(decorated);
        final byte[] message = new byte[0];

        // when
        internalSerializer.deserialize(message, Message1.class);

        // then
        verify(decorated, Mockito.times(1)).deserialize(message, Message1.class);
    }

    @Test
    @Parameters(method = "deserializationAssertions")
    public void shouldDeserializeMessage(final MessageDeserializationAssertion assertion) {
        // given
        final MessageSerializer decorated = mock(MessageSerializer.class);
        final MessageSerializer internalSerializer = InternalSerializer.decorate(decorated);

        // when
        final Object result = internalSerializer.deserialize(assertion.message(), assertion.type());

        // then
        assertThat(result).isEqualTo(assertion.expectedResult());
    }

    static MessageDeserializationAssertion[] deserializationAssertions() {
        return new MessageDeserializationAssertion[] {
                makeAssertion()
                    .thatMessage("byte message".getBytes())
                    .deserializedTo(byte[].class)
                    .returns("byte message".getBytes()),
                makeAssertion()
                    .thatMessage("text message")
                    .deserializedTo(String.class)
                    .returns("text message"),
                makeAssertion()
                    .thatMessage("1")
                    .deserializedTo(byte.class)
                    .returns((byte) 1),
                makeAssertion()
                    .thatMessage("1")
                    .deserializedTo(Byte.class)
                    .returns((byte) 1),
                makeAssertion()
                    .thatMessage("10")
                    .deserializedTo(short.class)
                    .returns((short) 10),
                makeAssertion()
                    .thatMessage("10")
                    .deserializedTo(Short.class)
                    .returns((short) 10),
                makeAssertion()
                    .thatMessage("100")
                    .deserializedTo(int.class)
                    .returns(100),
                makeAssertion()
                    .thatMessage("100")
                    .deserializedTo(Integer.class)
                    .returns(100),
                makeAssertion()
                    .thatMessage("1000")
                    .deserializedTo(Long.class)
                    .returns(1000L),
                makeAssertion()
                    .thatMessage("1000")
                    .deserializedTo(Long.class)
                    .returns(1000L),
                makeAssertion()
                    .thatMessage("10000.5")
                    .deserializedTo(float.class)
                    .returns(10000.5F),
                makeAssertion()
                    .thatMessage("10000.5")
                    .deserializedTo(Float.class)
                    .returns(10000.5F),
                makeAssertion()
                    .thatMessage("100000.5")
                    .deserializedTo(double.class)
                    .returns(100000.5D),
                makeAssertion()
                    .thatMessage("100000.5")
                    .deserializedTo(Double.class)
                    .returns(100000.5D),
                makeAssertion()
                    .thatMessage("true")
                    .deserializedTo(boolean.class)
                    .returns(true),
                makeAssertion()
                    .thatMessage("false")
                    .deserializedTo(Boolean.class)
                    .returns(false),
                makeAssertion()
                    .thatMessage(new byte[0])
                    .deserializedTo(Message1.class)
                    .returns(null)
            };
        }

        @Test
        @Parameters(method = "serializationAssertions")
        public void shouldSerializeMessage(final MessageSerializationAssertion assertion) {
            // given
            final MessageSerializer decorated = mock(MessageSerializer.class);
            final MessageSerializer internalSerializer = InternalSerializer.decorate(decorated);

            // when
            final byte[] result = internalSerializer.serialize(assertion.message());

            // then
            assertThat(result).isEqualTo(assertion.expectedResult());
        }

        static MessageSerializationAssertion[] serializationAssertions() {
            return new MessageSerializationAssertion[] {
                    makeAssertion()
                        .thatObject("byte message".getBytes())
                        .onSerializationReturns("byte message".getBytes()),
                    makeAssertion()
                        .thatObject("text message")
                        .onSerializationReturns("text message".getBytes()),
                    makeAssertion()
                        .thatObject((byte) 1)
                        .onSerializationReturns("1".getBytes()),
                    makeAssertion()
                        .thatObject(new Byte((byte) 1))
                        .onSerializationReturns("1".getBytes()),
                    makeAssertion()
                        .thatObject((short) 10)
                        .onSerializationReturns("10".getBytes()),
                    makeAssertion()
                        .thatObject(new Short((short) 10))
                        .onSerializationReturns("10".getBytes()),
                    makeAssertion()
                        .thatObject(100)
                        .onSerializationReturns("100".getBytes()),
                    makeAssertion()
                        .thatObject(new Integer(100))
                        .onSerializationReturns("100".getBytes()),
                    makeAssertion()
                        .thatObject(1000L)
                        .onSerializationReturns("1000".getBytes()),
                    makeAssertion()
                        .thatObject(new Long(1000L))
                        .onSerializationReturns("1000".getBytes()),
                    makeAssertion()
                        .thatObject(10000.5F)
                        .onSerializationReturns("10000.5".getBytes()),
                    makeAssertion()
                        .thatObject(new Float(10000.5F))
                        .onSerializationReturns("10000.5".getBytes()),
                    makeAssertion()
                        .thatObject(100000.5D)
                        .onSerializationReturns("100000.5".getBytes()),
                    makeAssertion()
                        .thatObject(new Double(100000.5D))
                        .onSerializationReturns("100000.5".getBytes()),
                    makeAssertion()
                        .thatObject(true)
                        .onSerializationReturns("true".getBytes()),
                    makeAssertion()
                        .thatObject(new Boolean(false))
                        .onSerializationReturns("false".getBytes()),
                    makeAssertion()
                        .thatObject(Message1.of(1, "test"))
                        .onSerializationReturns(null)
            };
    }

        @Test
        @Parameters(method = "messageDeserializationErrorAssertions")
        public void shouldThrowDetailedExceptionOnDeserializationInvalidInputToNumber(
                final MessageDeserializationErrorAssertion<?> assertion) {
            // given
            final MessageSerializer decorated = mock(MessageSerializer.class);
            final MessageSerializer internalSerializer = InternalSerializer.decorate(decorated);

            // when
            final Throwable caughtException = catchThrowable(() ->
                internalSerializer.deserialize(assertion.message(), assertion.type()));

            // then
            assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(assertion.expectedException())
                .hasMessage(assertion.expectedMessage());
        }

        static MessageDeserializationErrorAssertion<?>[] messageDeserializationErrorAssertions() {
            return new MessageDeserializationErrorAssertion[] {
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(int.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(Integer.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(short.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(Short.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(long.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(Long.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(float.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(Float.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(double.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number"),
                    makeAssertion()
                        .thatMessage("test")
                        .deserializedTo(Double.class)
                        .throwsException(SerializationException.class,
                                "Message: 'test' has invalid format - expected number")
            };
        }
}
