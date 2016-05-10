package pl.finder.elmer;

import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

/**
 * Configurator of published message.
 */
public interface PublishConfigurator {

    /**
     * Sets target exchange for published message.
     *
     * @param exchange name of target exchange
     * @return configurator of message published to an exchange
     */
    PublishToConfigurator toExchange(String exchange);

    /**
     * Sets targe queue for published message.
     *
     * @param queue name of target queue
     * @return configurator of message published to queue
     */
    PublishToConfigurator toQueue(String queue);


    /**
     * Configurator of message published to given target.
     */
    public interface PublishToConfigurator {

        /**
         * Sets correlation id of published message.
         *
         * @param correlationId correlation id
         * @return self
         */
        PublishToConfigurator correlatedBy(String correlationId);

        /**
         * Sets reply queue of published message.
         *
         * @param replyTo name of reply queue
         * @return self
         */
        PublishToConfigurator replyTo(String replyTo);

        /***
         * Appends headers of published message.
         *
         * @param headers headers
         * @return self
         */
        PublishToConfigurator withHeaders(Map<String, String> headers);

        /**
         * Sets routing key of published message.
         *
         * @param routingKey routing key
         * @return self
         */
        PublishToConfigurator withRoutingKey(String routingKey);

        /**
         * Publishes message.
         *
         * @param message published message body
         * @throws ChannelException
         */
        <TMessage> void message(TMessage message)
                throws ChannelException;

        /**
         * Publishes messages mapped by routing key.
         *
         * @param messages messages with routing key as key
         * @throws ChannelException
         */
        void messagesByRoutingKey(Multimap<String, Object> messages) throws ChannelException;

        /**
         * Appends header of published message.
         *
         * @param name header name
         * @param value header value
         * @return self
         */
        default PublishToConfigurator withHeader(String name, String value) {
            final Map<String, String> headers = ImmutableMap.of(name, value);
            return withHeaders(headers);
        }

        /***
         * Appends headers of published message.
         *
         * @param headers headers
         * @return self
         */
        default PublishToConfigurator withHeaders(Consumer<ImmutableMap.Builder<String, String>> headers) {
            final ImmutableMap.Builder<String, String> headersBuilder = ImmutableMap.builder();
            headers.accept(headersBuilder);
            return withHeaders(headersBuilder.build());
        }

        /**
         * Publishes given messages.
         *
         * @param messages collection of messages to publish
         * @throws ChannelException
         */
        default <TMessage> void messages(Iterable<TMessage> messages) throws ChannelException {
            for (TMessage message : messages) {
                message(message);
            }
        }
    }
}