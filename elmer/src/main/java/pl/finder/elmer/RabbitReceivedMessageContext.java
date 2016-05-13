package pl.finder.elmer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import pl.finder.elmer.model.MessageRejectionOption;
import pl.finder.elmer.model.ReceivedMessageContext;

import com.rabbitmq.client.Envelope;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString(exclude = { "confirm", "reject" })
@Accessors(fluent = true)
final class RabbitReceivedMessageContext implements ReceivedMessageContext {
    @Getter
    private final long deliveryTag;
    @Getter
    private final String exchange;
    @Getter
    private final String routingKey;
    @Getter
    private final String consumerTag;
    private final transient Consumer<Long> confirm;
    private final transient BiConsumer<Long, MessageRejectionOption> reject;

    @Override
    public void confirm() throws ChannelException {
        confirm.accept(deliveryTag);
    }

    @Override
    public void reject(final MessageRejectionOption options) throws ChannelException {
        reject.accept(deliveryTag, options);
    }

    static RabbitReceivedMessageContext.Builder buildFrom(final Envelope envelope) {
        return Builder.createFrom(envelope);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Builder {
        private final long deliveryTag;
        private final String exchange;
        private final String routingKey;

        private static Builder createFrom(final Envelope envelope) {
            return new Builder(envelope.getDeliveryTag(), envelope.getExchange(), envelope.getRoutingKey());
        }

        Builder.Step1 confirmUsing(final Consumer<Long> confirmAction) {
            return new Step1(deliveryTag, exchange, routingKey, confirmAction);
        }

        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        static final class Step1 {
            private final long deliveryTag;
            private final String exchange;
            private final String routingKey;
            private final Consumer<Long> confirm;

            Builder.Step2 rejectUsing(final BiConsumer<Long, MessageRejectionOption> rejectAction) {
                return new Step2(deliveryTag, exchange, routingKey, confirm, rejectAction);
            }
        }

        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        static final class Step2 {
            private final long deliveryTag;
            private final String exchange;
            private final String routingKey;
            private final Consumer<Long> confirm;
            private final BiConsumer<Long, MessageRejectionOption> reject;

            ReceivedMessageContext build(final String consumerTag) {
                return new RabbitReceivedMessageContext(deliveryTag, exchange, routingKey, consumerTag, confirm, reject);
            }
        }
    }
}
