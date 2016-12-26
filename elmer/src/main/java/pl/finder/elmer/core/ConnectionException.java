package pl.finder.elmer.core;

@SuppressWarnings("serial")
public final class ConnectionException extends AMQPException {

    public ConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
