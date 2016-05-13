package pl.finder.elmer.consumer;

import pl.finder.elmer.model.Message;
import pl.finder.elmer.model.ReceivedMessageContext;

/**
 * Basic consumer of received messages.
 *
 * @param <TMessageBody> type of message body
 */
public interface MessageConsumer<TMessageBody> {

    void onMessage(Message<TMessageBody> message, ReceivedMessageContext context);

    Class<TMessageBody> messageType();
}
