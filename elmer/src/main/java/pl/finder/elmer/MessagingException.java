package pl.finder.elmer;

/**
 * Exception of message publication.
 */
@SuppressWarnings("serial")
public class MessagingException extends RuntimeException {

    public MessagingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
