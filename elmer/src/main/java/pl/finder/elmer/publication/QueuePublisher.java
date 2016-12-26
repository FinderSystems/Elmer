package pl.finder.elmer.publication;

public interface QueuePublisher extends AutoCloseable {

    boolean isRunning();

    @Override
    void close();
}
