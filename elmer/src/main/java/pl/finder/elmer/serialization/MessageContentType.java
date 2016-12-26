package pl.finder.elmer.serialization;

public enum MessageContentType {
    JSON("application/json"),
    XML("application/xml"),
    JAVA("application/java");

    private final String value;

    private MessageContentType(final String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
