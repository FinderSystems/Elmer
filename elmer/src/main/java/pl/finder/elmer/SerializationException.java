package pl.finder.elmer;

@SuppressWarnings("serial")
public final class SerializationException extends RuntimeException {

	public SerializationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
