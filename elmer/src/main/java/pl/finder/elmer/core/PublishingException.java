package pl.finder.elmer.core;

import lombok.Getter;
import lombok.experimental.Accessors;
import pl.finder.elmer.publication.PublishOptions;

@SuppressWarnings("serial")
@Accessors(fluent = true)
public final class PublishingException extends AMQPException {
    @Getter
    private final PublishOptions options;

    public PublishingException(final String message, final Throwable cause, final PublishOptions options) {
        super(message, cause);
        this.options = options;
    }

    public static PublishingException create(final PublishOptions options, final Throwable cause) {
        return new PublishingException(
                String.format("Could not publish message to: '%s' reason: '%s'", options.exchange()),
                cause, options);
    }
}
