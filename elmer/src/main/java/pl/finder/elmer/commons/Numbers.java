package pl.finder.elmer.commons;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utilities for numbers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Numbers {

    /**
     * Parse string to byte using Byte.parseByte method.
     * NumberFormatException is wrapped to contain parsed string value in message.
     *
     * @param value value to parse
     * @return parsed value
     */
    public static byte parseByte(final String value) {
        return parse(Byte::parseByte, value);
    }

    /**
     * Parse string to short using Short.parseShort method.
     * NumberFormatException is wrapped to contain parsed string value in message.
     *
     * @param value value to parse
     * @return parsed value
     */
    public static short parseShort(final String value) {
        return parse(Short::parseShort, value);
    }

    /**
     * Parse string to integer using Integer.parseInt method.
     * NumberFormatException is wrapped to contain parsed string value in message.
     *
     * @param value value to parse
     * @return parsed value
     */
    public static int parseInt(final String value) {
        return parse(Integer::parseInt, value);
    }

    /**
     * Parse string to long using Long.parseLong method.
     * NumberFormatException is wrapped to contain parsed string value in message.
     *
     * @param value value to parse
     * @return parsed value
     */
    public static long parseLong(final String value) {
        return parse(Long::parseLong, value);
    }

    /**
     * Parse string to float using Float.parseFloat method.
     * NumberFormatException is wrapped to contain parsed string value in message.
     *
     * @param value value to parse
     * @return parsed value
     */
    public static float parseFloat(final String value) {
        return parse(Float::parseFloat, value);
    }

    /**
     * Parse string to double using Double.parseDouble method.
     * NumberFormatException is wrapped to contain parsed string value in message.
     *
     * @param value value to parse
     * @return parsed value
     */
    public static double parseDouble(final String value) {
        return parse(Double::parseDouble, value);
    }

    private static <T extends Number> T parse(final Function<String, T> parseFunction, final String value) {
        try {
            return parseFunction.apply(value);
        } catch (final NumberFormatException e) {
            throw new NumberFormatException(String.format("Could not parse: '%s' reason: %s",
                    value, e.getMessage()));
        }
    }
}
