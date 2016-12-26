package pl.finder.elmer.core;

import pl.finder.elmer.subscription.SubscribeOptions;

@SuppressWarnings("serial")
public final class SubscriptionException extends AMQPException {

    public SubscriptionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static SubscriptionException create(final SubscribeOptions<?> options, final Throwable cause) {
        return new SubscriptionException(String.format("Could not subscribe to: '%s'", options.queue()), cause);
    }
}
