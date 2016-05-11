package pl.finder.elmer.consumer;

import pl.finder.elmer.model.Message;
import pl.finder.elmer.model.ReceivedMessageContext;

/**
 * Basic consumer of received messages.
 *
 * @param <TMessageBody> type of message body
 */
@FunctionalInterface
public interface MessageConsumer<TMessage> {

    void onMessage(Message<TMessage> message, ReceivedMessageContext context);
}
