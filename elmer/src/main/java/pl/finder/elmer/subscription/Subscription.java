package pl.finder.elmer.subscription;

public interface Subscription extends AutoCloseable {

    String consumerTag();

    @Override
    void close();
}
