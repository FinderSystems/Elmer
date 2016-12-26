package pl.finder.elmer;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.function.Consumer;

import com.rabbitmq.client.BasicProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import pl.finder.elmer.core.MessageContext;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(fluent = true)
@EqualsAndHashCode
final class RabbitMessageContext<T> implements MessageContext<T> {
    @Getter
    private final T body;
    @Getter
    private final String consumerTag;
    @Getter
    private final String exchange;
    @Getter
    private final String routingKey;
    @Getter
    private final BasicProperties properties;
    private transient final Consumer<Void> onAck;

    @Override
    public void ack() {
        onAck.accept(null);
    }

    @Override
    public String toString() {
        return toStringHelper("MessageContext")
                .add("consumerTag", consumerTag)
                .add("routingKey",routingKey)
                .add("body", body)
                .toString();

    }
}
