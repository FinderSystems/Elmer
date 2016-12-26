package pl.finder.elmer;

import java.io.IOException;
import java.util.function.Supplier;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import pl.finder.elmer.core.ChannelException;
import pl.finder.elmer.io.ConnectionProvider;

@AllArgsConstructor(access = AccessLevel.PACKAGE, staticName = "create")
final class RabbitChannelFactory implements Supplier<Channel> {
    private final ConnectionProvider connectionProvider;

    @Override
    public Channel get() {
        final Connection connection = connectionProvider.get();
        try {
            return connection.createChannel();
        } catch (final IOException e) {
            throw new ChannelException("Could not create channel", e);
        }
    }

}
