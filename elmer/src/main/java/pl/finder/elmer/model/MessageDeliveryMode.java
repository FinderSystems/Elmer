package pl.finder.elmer.model;

public enum MessageDeliveryMode {
    NonPersistent(1),
    Persistent(2);

    private final int value;

    private MessageDeliveryMode(final int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
