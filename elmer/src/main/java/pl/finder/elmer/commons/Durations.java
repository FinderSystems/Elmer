package pl.finder.elmer.commons;

import java.time.Duration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Extension methods for java.time.Duration class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Durations {

    /**
     * Checks is source duration is greater than another.
     *
     * @param source source duration
     * @param duration compared to
     * @return true/false
     */
    public static boolean isGreaterThan(final Duration source, final Duration duration) {
        return source.compareTo(duration) > 0;
    }

    /**
     * Checks is source duration is greater or equal another.
     *
     * @param source source duration
     * @param duration compared to
     * @return true/false
     */
    public static boolean isGreaterOrEqual(final Duration source, final Duration duration) {
        return source.compareTo(duration) >= 0;
    }

    /**
     * Checks is source duration is lesser than another.
     *
     * @param source source duration
     * @param duration compared to
     * @return true/false
     */
    public static boolean isLesserThan(final Duration source, final Duration duration) {
        return source.compareTo(duration) < 0;
    }

    /**
     * Checks is source duration is lesser or equal another.
     *
     * @param source source duration
     * @param duration compared to
     * @return true/false
     */
    public static boolean isLesserOrEqual(final Duration source, final Duration duration) {
        return source.compareTo(duration) <= 0;
    }
}