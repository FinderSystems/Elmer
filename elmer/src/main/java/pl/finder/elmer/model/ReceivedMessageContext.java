package pl.finder.elmer.model;

import pl.finder.elmer.ChannelException;

public interface ReceivedMessageContext {

    long deliveryTag();

    String consumerTag();

    String exchange();

    String routingKey();

    void confirm() throws ChannelException;

    void reject(MessageRejectionOption options) throws ChannelException;

    default void reject() throws ChannelException {
        reject(MessageRejectionOption.createDefault());
    }
}
