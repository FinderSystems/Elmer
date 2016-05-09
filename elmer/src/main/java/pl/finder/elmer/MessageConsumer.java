package pl.finder.elmer;

import pl.finder.elmer.model.Message;

/**
 * Consumer of received messages.
 *
 * @param <TMessageBody> type of message body
 */
@FunctionalInterface
public interface MessageConsumer<TMessageBody> {

	void onMessage(Message<TMessageBody> message);
}
