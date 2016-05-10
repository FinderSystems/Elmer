package pl.finder.elmer;

/**
 * Exception of amqp channel.
 */
@SuppressWarnings("serial")
public class ChannelException extends RuntimeException {

    public ChannelException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
