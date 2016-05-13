package pl.finder.elmer.consumer;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import pl.finder.elmer.model.Message;
import pl.finder.elmer.model.ReceivedMessageContext;

/**
 * Utilities for MessageConsumer creation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageConsumers {

    /**
     * Creates MessageConsumer from BasicMessageConsumer
     *
     * @param consumer basic message consumer
     * @return MessageConsumer
     */
    public static <TMessageBody> MessageConsumer<TMessageBody> from(final BasicMessageConsumer<TMessageBody> consumer) {
        final Consumer<Message<TMessageBody>> onMessage = received -> consumer.onMessage(received);
        return from(onMessage, consumer.messageType());
    }

    /**
     * Creates MessageConsumer from function.
     *
     * @param onMessage message consume function
     * @param messageType type of consumed message
     * @return MessageConsumer
     */
    public static <TMessageBody> MessageConsumer<TMessageBody> from(final Consumer<Message<TMessageBody>> onMessage,
            final Class<TMessageBody> messageType) {
        return new BasicMessageConsumerWrapper<>(onMessage, messageType);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class BasicMessageConsumerWrapper<T> implements MessageConsumer<T> {
        private final Consumer<Message<T>> onMessage;
        private final Class<T> messageType;

        @Override
        public void onMessage(final Message<T> message, final ReceivedMessageContext context) {
            onMessage.accept(message);
        }

        @Override
        public Class<T> messageType() {
            return messageType;
        }

    }
}
