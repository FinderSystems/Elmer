package pl.finder.elmer.util;

import java.lang.reflect.Type;
import java.time.Duration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssertionBuilder {
    private final String name;

    public static AssertionBuilder makeAssertion(final String name) {
        return new AssertionBuilder(name);
    }

    public static AssertionBuilder makeAssertion() {
        return new AssertionBuilder("");
    }

    public DurationAssertionBuilder thatDuration(final Duration duration) {
        return new DurationAssertionBuilder(name, duration);
    }

    public MessageAssertionBuilder thatMessage(final String message) {
        return thatMessage(message.getBytes());
    }

    public MessageAssertionBuilder thatMessage(final byte[] message) {
        return new MessageAssertionBuilder(message);
    }

    public ObjectAssertionBuilder thatObject(final Object object) {
        return new ObjectAssertionBuilder(object);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class DurationAssertionBuilder {
        private final String name;
        private final Duration duration;

        public DurationComparationAssertionBuilder comparedTo(final Duration comparedTo) {
           return new DurationComparationAssertionBuilder(name, duration, comparedTo);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class DurationComparationAssertionBuilder {
        private final String name;
        private final Duration first;
        private final Duration second;

        public DurationComparationAssertion shouldReturnTrue() {
            return new DurationComparationAssertion(name, first, second, true);
        }

        public DurationComparationAssertion shouldReturnFalse() {
            return new DurationComparationAssertion(name, first, second, false);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    public static final class DurationComparationAssertion {
        private final String name;
        private final Duration first;
        private final Duration second;
        private final boolean expectedResult;

        @Override
        public String toString() {
            return String.format(name, first, second, expectedResult);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ObjectAssertionBuilder {
        private final Object object;

        public MessageSerializationAssertion onSerializationReturns(final byte[] results) {
            return new MessageSerializationAssertion(object, results);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class MessageAssertionBuilder {
        private final byte[] message;

        public MessageDeserializationAssertionBuilder deserializedTo(final Type type) {
            return new MessageDeserializationAssertionBuilder(message, type);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class MessageDeserializationAssertionBuilder {
        private final byte[] message;
        private final Type type;

        public MessageDeserializationAssertion returns(final Object results) {
            return new MessageDeserializationAssertion(message, type, results);
        }

        public <T extends Throwable> MessageDeserializationErrorAssertion<T> throwsException(
                final Class<T> expectedException, final String expectedMessage) {
            return new MessageDeserializationErrorAssertion<>(message, type, expectedException, expectedMessage);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    public static final class MessageDeserializationAssertion {
        private final byte[] message;
        private final Type type;
        private final Object expectedResult;

        @Override
        public String toString() {
            return String.format("'%s' deserialized to %s returns '%s'", new String(message), type, expectedResult);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    public static final class MessageDeserializationErrorAssertion<T extends Throwable> {
        private final byte[] message;
        private final Type type;
        private final Class<T> expectedException;
        private final String expectedMessage;

        @Override
        public String toString() {
            final Object messageDescription = message != null ? new String(message) : message;
            return String.format("should throw %s with '%s' message when deserializing '%s' to %s",
                    expectedException, expectedMessage, messageDescription, type);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    public static final class MessageSerializationAssertion {
        private final Object message;
        private final byte[] expectedResult;

        @Override
        public String toString() {
            final Object messageDeseription = (message instanceof byte[]) ?
                    new String((byte[]) message) : message;
            final Object expectedResultDeseription = expectedResult != null ? new String(expectedResult) : null;
            return String.format("'%s' on serialization returns '%s'", messageDeseription, expectedResultDeseription);
        }
    }

}
