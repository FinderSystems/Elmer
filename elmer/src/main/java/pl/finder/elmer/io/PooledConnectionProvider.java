package pl.finder.elmer.io;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Connection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PooledConnectionProvider implements ConnectionProvider {
    private final ConnectionProvider connectionProvider;
    private final int poolSize;
    private final Map<Integer, Connection> connections;

    private boolean closed = false;
    private int connectionNo;

    public PooledConnectionProvider(final ConnectionProvider connectionProvider, final int poolSize) {
        this.connectionProvider = connectionProvider;
        this.poolSize = poolSize;
        this.connections = new HashMap<>(poolSize);
    }

    @Override
    public Connection get() {
        synchronized (connections) {
            checkState(!closed, "ConnectionPool has been closed");
            final int connectionNo = nextConnectionNo();
            Connection connection = connections.get(connectionNo);
            if (connection == null || !connection.isOpen()) {
                connection = connectionProvider.get();
                connections.put(connectionNo, connection);
            }
            return connection;
        }
    }

    @Override
    public void close() {
        log.info("Closing connection pool");
        synchronized (connections) {
            try {
                connections.values().stream().forEach(PooledConnectionProvider::tryClose);
                connections.clear();
            } finally {
                closed = true;
                connectionProvider.close();
            }
        }
    }

    private static void tryClose(final Connection connection) {
        try {
            if (connection.isOpen()) {
                connection.close();
            }
        } catch (final IOException e) {
            log.warn("Could not close connetion", e);
        }
    }

    private int nextConnectionNo() {
        if (poolSize == 1) {
            return 0;
        }
        final int result = connectionNo;
        if (connectionNo >= poolSize - 1) {
            connectionNo = 0;
        } else {
            connectionNo ++;
        }
        return result;
    }
}
