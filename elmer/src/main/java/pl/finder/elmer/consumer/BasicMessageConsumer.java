package pl.finder.elmer.consumer;

import pl.finder.elmer.model.Message;

/**
 * Basic consumer of received messages.
 *
 * @param <TMessageBody> type of message body
 */
@FunctionalInterface
public interface BasicMessageConsumer<TMessageBody> {

    void onMessage(Message<TMessageBody> message);
}
