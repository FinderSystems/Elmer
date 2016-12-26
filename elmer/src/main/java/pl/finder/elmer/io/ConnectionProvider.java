package pl.finder.elmer.io;

import java.util.function.Supplier;

import com.rabbitmq.client.Connection;

import pl.finder.elmer.core.AMQPException;

public interface ConnectionProvider extends Supplier<Connection>, AutoCloseable {

    @Override
    Connection get() throws AMQPException;

    @Override
    void close() throws AMQPException;
}
