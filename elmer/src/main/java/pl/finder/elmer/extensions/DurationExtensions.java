package pl.finder.elmer.extensions;

import java.time.Duration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Extensions for duration
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DurationExtensions {

    /**
     * Checks is duration is greater than another.
     *
     * @return true greater, false otherwise
     */
    public static boolean isGreaterThan(final Duration source, final Duration target) {
        return source.compareTo(target) > 0;
    }

    /**
     * Checks is duration is greater than or equal to another.
     *
     * @return true greater or equal, false otherwise
     */
    public static boolean isGreaterOrEqual(final Duration source, final Duration target) {
        return source.compareTo(target) >= 0;
    }

    /**
     * Checks is duration is lower than another.
     *
     * @return true lower, false otherwise
     */
    public static boolean isLowerThan(final Duration source, final Duration target) {
        return source.compareTo(target) < 0;
    }

    /**
     * Checks is duration is lower than or equal to another.
     *
     * @return true lower or equal, false otherwise
     */
    public static boolean isLowerOrEqual(final Duration source, final Duration target) {
        return source.compareTo(target) <= 0;
    }
}
