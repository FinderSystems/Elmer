package pl.finder.elmer.core;

@SuppressWarnings("serial")
public final class ChannelException extends AMQPException {

    public ChannelException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
