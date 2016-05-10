package pl.finder.elmer.util;

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

    public DurationAssertionBuilder thatDuration(final Duration duration) {
        return new DurationAssertionBuilder(name, duration);
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
}
