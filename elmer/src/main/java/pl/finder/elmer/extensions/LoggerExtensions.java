package pl.finder.elmer.extensions;

import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;

/**
 * Extensions for Logger class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggerExtensions {

    public static void debug(final Logger log, final Supplier<String> message) {
        if (log.isDebugEnabled()) {
            log.debug(message.get());
        }
    }

    public static void trace(final Logger log, final Supplier<String> message) {
        if (log.isTraceEnabled()) {
            log.trace(message.get());
        }
    }
}
