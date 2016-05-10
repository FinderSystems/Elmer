package pl.finder.elmer.extensions;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.finder.elmer.util.AssertionBuilder.makeAssertion;

import java.time.Duration;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import pl.finder.elmer.util.AssertionBuilder.DurationComparationAssertion;

@RunWith(JUnitParamsRunner.class)
public class DurationExtensionsTest {

    @Test
    @Parameters(method = "greaterThanAssertions")
    public void shouldCompareIsDurationIsGreatherThan(final DurationComparationAssertion assertion) {
        // given
        final Duration first = assertion.first();
        final Duration second = assertion.second();

        // when
        final boolean results = DurationExtensions.isGreaterThan(first, second);

        // then
        assertThat(results).isEqualTo(assertion.expectedResult());
    }

    static DurationComparationAssertion[] greaterThanAssertions() {
        final String message= "%s isGreatherThan %s returns %b";
        return new DurationComparationAssertion[] {
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(1))
                    .shouldReturnTrue(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnFalse(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(1))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnFalse(),
        };
    }

    @Test
    @Parameters(method = "greaterThanOrEqualAssertions")
    public void shouldCompareIsDurationIsGreatheOrEqual(final DurationComparationAssertion assertion) {
        // given
        final Duration first = assertion.first();
        final Duration second = assertion.second();

        // when
        final boolean results = DurationExtensions.isGreaterOrEqual(first, second);

        // then
        assertThat(results).isEqualTo(assertion.expectedResult());
    }

    static DurationComparationAssertion[] greaterThanOrEqualAssertions() {
        final String message= "%s isGreaterOrEqual %s returns %b";
        return new DurationComparationAssertion[] {
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(1))
                    .shouldReturnTrue(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnTrue(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(1))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnFalse(),
        };
    }

    @Test
    @Parameters(method = "lowerThanAssertions")
    public void shouldCompareIsDurationIsLowerThan(final DurationComparationAssertion assertion) {
        // given
        final Duration first = assertion.first();
        final Duration second = assertion.second();

        // when
        final boolean results = DurationExtensions.isLowerThan(first, second);

        // then
        assertThat(results).isEqualTo(assertion.expectedResult());
    }

    static DurationComparationAssertion[] lowerThanAssertions() {
        final String message= "%s isLowerThan %s returns %b";
        return new DurationComparationAssertion[] {
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(1))
                    .shouldReturnFalse(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnFalse(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(1))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnTrue()
        };
    }

    @Test
    @Parameters(method = "lowerThanOrEqualAssertions")
    public void shouldCompareIsDurationIsLowerOrEqual(final DurationComparationAssertion assertion) {
        // given
        final Duration first = assertion.first();
        final Duration second = assertion.second();

        // when
        final boolean results = DurationExtensions.isLowerOrEqual(first, second);

        // then
        assertThat(results).isEqualTo(assertion.expectedResult());
    }

    static DurationComparationAssertion[] lowerThanOrEqualAssertions() {
        final String message= "%s isLowerOrEqual %s returns %b";
        return new DurationComparationAssertion[] {
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(1))
                    .shouldReturnFalse(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(2))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnTrue(),
                makeAssertion(message)
                    .thatDuration(Duration.ofMinutes(1))
                    .comparedTo(Duration.ofMinutes(2))
                    .shouldReturnTrue()
        };
    }
}
