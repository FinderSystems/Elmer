package pl.finder.elmer.core;

@SuppressWarnings("serial")
public abstract class AMQPException extends RuntimeException {

    protected AMQPException(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected AMQPException(final String message) {
        super(message);
    }
}
