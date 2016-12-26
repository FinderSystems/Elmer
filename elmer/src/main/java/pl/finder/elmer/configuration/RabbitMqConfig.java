package pl.finder.elmer.configuration;

import java.time.Duration;
import java.util.List;

import com.rabbitmq.client.Address;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import pl.finder.elmer.serialization.MessageContentType;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Getter
@Accessors(fluent = true)
public final class RabbitMqConfig {
    private final List<Address> hosts;
    private final String username;
    private final String password;
    private final String virtualHost;
    private final Duration heartbeat;
    private final Duration shutdownTimeout;
    private final Duration connectionTimeout;
    private final Duration handshakeTimeout;
    private final Duration networkRecoveryInterval;
    private final boolean networkRecoveryEnabled;
    private final boolean topologyRecoveryEnabled;
    private final boolean messagePersitanceEnabled;
    private final int channelLimit;
    private final int frameSizeLimit;
    private final int connectionPoolSize;
    private final SerializationConfig serialization;
    private final boolean nonBlockingIoEnabled;
    private final boolean sslEnabled;
    private final String sslProtocol;

    public static RabbitMqConfig createDefaut() {
        return null; // TODO
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    @ToString
    @Getter
    @Accessors(fluent = true)
    public static final class SerializationConfig {
        private final MessageContentType defaultContentType;
        private final boolean includeContentType;
        private final boolean includeMessageType;
        private final boolean incldeEncoding;
    }
}
