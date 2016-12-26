package pl.finder.elmer.configuration;

public enum SslProtocol {

    TLSv1_2("TLSv1.2"),
    TLSv1("TLSv1");

    private final String value;

    private SslProtocol(final String value) {
        this.value = value;
    }

    public String protocolName() {
        return value;
    }
}
