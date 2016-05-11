package pl.finder.elmer.model;

import pl.finder.elmer.ChannelException;

public interface ReceivedMessageContext {

    long deliveryTag();

    byte[] body();

    void confirm() throws ChannelException;

    void reject(MessageRejectionOption options) throws ChannelException;

    default void reject() throws ChannelException {
        reject(MessageRejectionOption.createDefault());
    }
}
