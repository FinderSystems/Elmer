package pl.finder.elmer;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import pl.finder.elmer.configuration.RabbitMqConfig;
import pl.finder.elmer.configuration.SslProtocol;
import pl.finder.elmer.core.AMQPException;
import pl.finder.elmer.core.ConnectionException;
import pl.finder.elmer.io.ConnectionProvider;
import pl.finder.elmer.io.SslConfigurator;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RabbitConnectionProvider implements ConnectionProvider {
    private final RabbitMqConfig config;
    private final ConnectionFactory connectionFactory;

    static ConnectionProvider create(final RabbitMqConfig config, final DefaultSslConfigurator sslConfigurator) {
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(config.username());
        connectionFactory.setPassword(config.password());
        connectionFactory.setVirtualHost(config.virtualHost());
        connectionFactory.setAutomaticRecoveryEnabled(config.networkRecoveryEnabled());
        connectionFactory.setTopologyRecoveryEnabled(config.topologyRecoveryEnabled());
        connectionFactory.setConnectionTimeout((int) config.connectionTimeout().toMillis());
        connectionFactory.setHandshakeTimeout((int) config.handshakeTimeout().toMillis());
        connectionFactory.setShutdownTimeout((int) config.shutdownTimeout().toMillis());
        connectionFactory.setNetworkRecoveryInterval(config.networkRecoveryInterval().toMillis());
        connectionFactory.setRequestedHeartbeat((int) config.heartbeat().getSeconds());
        connectionFactory.setRequestedChannelMax(config.channelLimit());
        connectionFactory.setRequestedFrameMax(config.frameSizeLimit());
        if (config.nonBlockingIoEnabled()) {
            connectionFactory.useNio();
        } else {
            connectionFactory.useBlockingIo();
        }
        if (config.sslEnabled()) {
            sslConfigurator.configure(connectionFactory, config);
        }
        return new RabbitConnectionProvider(config, connectionFactory);
    }

    @Override
    public Connection get() {
        try {
            return connectionFactory.newConnection(config.hosts());
        } catch (IOException | TimeoutException e) {
            throw new ConnectionException("Could not establish connection", e);
        }
    }

    @Override
    public void close() throws AMQPException {
        connectionFactory.clone();
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
    static final class DefaultSslConfigurator implements SslConfigurator {
        private SSLContext context;
        private TrustManager trustManager;

        @Override
        public SslConfigurator sslContext(final SSLContext context) {
            this.context = context;
            return this;
        }

        @Override
        public SslConfigurator trustManager(final TrustManager trustManager) {
            this.trustManager = trustManager;
            return this;
        }

        private void configure(final ConnectionFactory connectionFactory, final RabbitMqConfig config) {
            try {
                apply(connectionFactory, config);
            } catch (final GeneralSecurityException e) {
                throw new IllegalStateException("Could not enable ssl", e);
            }
        }

        private void apply(final ConnectionFactory connectionFactory, final RabbitMqConfig config)
                throws NoSuchAlgorithmException, KeyManagementException {
            if (context != null) {
                connectionFactory.useSslProtocol(context);
            } else if (trustManager != null) {
                final String protocol = !isNullOrEmpty(config.sslProtocol()) ?
                        config.sslProtocol() : SslProtocol.TLSv1_2.protocolName();
                        connectionFactory.useSslProtocol(protocol, trustManager);
            } else if (!isNullOrEmpty(config.sslProtocol())) {
                connectionFactory.useSslProtocol(config.sslProtocol());
            } else {
                connectionFactory.useSslProtocol();
            }
        }
    }
}
