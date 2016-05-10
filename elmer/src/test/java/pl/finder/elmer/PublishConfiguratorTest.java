package pl.finder.elmer;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import pl.finder.elmer.model.Message1;
import pl.finder.elmer.model.Message2;
import pl.finder.elmer.model.PublishConfig;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class PublishConfiguratorTest {

    @Test
    public void shouldPublishToExchange() {
        // given
        @SuppressWarnings("resource")
        final MessageBus bus = mock(MessageBus.class);
        final String message = "test";
        final String exchange = "test-exchange";
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final String routingKey = "1.2.message";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);

        // when
        publish
            .toExchange(exchange)
            .correlatedBy(correlationId)
            .replyTo(replyTo)
            .withHeaders(headers -> headers
                    .put("X-Header1", "value1")
                    .put("X-Header2", "value2"))
            .withRoutingKey(routingKey)
            .message(message);

        // then
        final PublishConfig expectedConfig = PublishConfig.builder()
                .exchange(exchange)
                .correlationId(correlationId)
                .replyTo(replyTo)
                .routingKey(routingKey)
                .withHeader("X-Header1", "value1")
                .withHeader("X-Header2", "value2")
                .build();
        verify(bus, times(1)).publish(expectedConfig, message);
    }

    @Test
    public void shouldPublishToQueue() {
        // given
        @SuppressWarnings("resource")
        final MessageBus bus = mock(MessageBus.class);
        final Object message = new Object();
        final String queue = "queue";
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);
        final Map<String, String> headers = ImmutableMap.of("X-Header", "value");

        // when
        publish
            .toQueue(queue)
            .correlatedBy(correlationId)
            .replyTo(replyTo)
            .withHeaders(headers)
            .message(message);

        // then
        final PublishConfig expectedConfig = PublishConfig.builder()
                .queue(queue)
                .correlationId(correlationId)
                .replyTo(replyTo)
                .headers(headers)
                .build();
        verify(bus, times(1)).publish(expectedConfig, message);
    }

    @SuppressWarnings("resource")
    @Test
    public void shouldPublishMultipleMessagesToExchange() {
        // given
        final MessageBus bus = mock(MessageBus.class);
        final Iterable<String> messages = asList(
                "A", "B", "C", "D", "E");
        final Multimap<PublishConfig, String> publishedMessages = ArrayListMultimap.create();
        final String exchange = "test-exchange";
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final String routingKey = "1.2.message";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);
        doAnswer(answer -> {
            final PublishConfig config = answer.getArgumentAt(0, PublishConfig.class);
            final String message = answer.getArgumentAt(1, String.class);
            publishedMessages.put(config, message);
            return null;
        }).when(bus).publish(any(PublishConfig.class), anyString());

        // when
        publish
            .toExchange(exchange)
            .correlatedBy(correlationId)
            .replyTo(replyTo)
            .withHeaders(headers -> headers
                    .put("X-Header1", "value1")
                    .put("X-Header2", "value2"))
            .withRoutingKey(routingKey)
            .messages(messages);

        // then
        final PublishConfig expectedConfig = PublishConfig.builder()
                .exchange(exchange)
                .correlationId(correlationId)
                .replyTo(replyTo)
                .routingKey(routingKey)
                .withHeader("X-Header1", "value1")
                .withHeader("X-Header2", "value2")
                .build();
        assertThat(publishedMessages).isEqualTo(ImmutableMultimap.of(
                expectedConfig, "A",
                expectedConfig, "B",
                expectedConfig, "C",
                expectedConfig, "D",
                expectedConfig, "E"));
    }

    @SuppressWarnings("resource")
    @Test
    public void shouldPublishMultipleMessagesToQueue() {
        // given
        final MessageBus bus = mock(MessageBus.class);
        final Iterable<String> messages = asList(
                "A", "B", "C", "D", "E");
        final Multimap<PublishConfig, String> publishedMessages = ArrayListMultimap.create();
        final String queue = "test-queue";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);
        doAnswer(answer -> {
            final PublishConfig config = answer.getArgumentAt(0, PublishConfig.class);
            final String message = answer.getArgumentAt(1, String.class);
            publishedMessages.put(config, message);
            return null;
        }).when(bus).publish(any(PublishConfig.class), anyString());

        // when
        publish
            .toQueue(queue)
            .messages(messages);

        // then
        final PublishConfig expectedConfig = PublishConfig.builder()
                .queue(queue)
                .build();
        assertThat(publishedMessages).isEqualTo(ImmutableMultimap.of(
                expectedConfig, "A",
                expectedConfig, "B",
                expectedConfig, "C",
                expectedConfig, "D",
                expectedConfig, "E"));
    }

    @SuppressWarnings("resource")
    @Test
    public void shouldPublishMultipleMessagesByRoutingKeyToExchange() {
        // given
        final MessageBus bus = mock(MessageBus.class);
        final Multimap<String, Object> messages = ImmutableMultimap.of(
                "1.msg1", Message1.of(1, "A"),
                "1.msg2", Message2.of(1, 5),
                "2.msg1", Message1.of(2, "C"),
                "3.msg2", Message2.of(4, 7));
        final Multimap<PublishConfig, Object> publishedMessages = ArrayListMultimap.create();
        final String exchange = "test-exchange";
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final String routingKey = "not to be used";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);
        doAnswer(answer -> {
            final PublishConfig config = answer.getArgumentAt(0, PublishConfig.class);
            final Object message = answer.getArguments()[1];
            publishedMessages.put(config, message);
            return null;
        }).when(bus).publish(any(PublishConfig.class), anyString());

        // when
        publish
            .toExchange(exchange)
            .correlatedBy(correlationId)
            .replyTo(replyTo)
            .withHeaders(headers -> headers
                    .put("X-Header1", "value1")
                    .put("X-Header2", "value2"))
            .withRoutingKey(routingKey)
            .messagesByRoutingKey(messages);

        // when
        final PublishConfig expectedConfigBase = PublishConfig.builder()
                .exchange(exchange)
                .correlationId(correlationId)
                .replyTo(replyTo)
                .withHeader("X-Header1", "value1")
                .withHeader("X-Header2", "value2")
                .build();
        assertThat(publishedMessages).isEqualTo(ImmutableMultimap.of(
                expectedConfigBase.with().routingKey("1.msg1").build(), Message1.of(1, "A"),
                expectedConfigBase.with().routingKey("1.msg2").build(), Message2.of(1, 5),
                expectedConfigBase.with().routingKey("2.msg1").build(), Message1.of(2, "C"),
                expectedConfigBase.with().routingKey("3.msg2").build(), Message2.of(4, 7)));
    }

    @SuppressWarnings("resource")
    @Test
    public void shouldPublishMultipleMessagesByRoutingKeyToQueue() {
        // given
        final MessageBus bus = mock(MessageBus.class);
        final Multimap<String, Object> messages = ImmutableMultimap.of(
                "1.msg1", Message1.of(1, "A"),
                "1.msg2", Message2.of(1, 5),
                "2.msg1", Message1.of(2, "C"),
                "3.msg2", Message2.of(4, 7));
        final Multimap<PublishConfig, Object> publishedMessages = ArrayListMultimap.create();
        final String queue = "test-queue";
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final String routingKey = "not to be used";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);
        doAnswer(answer -> {
            final PublishConfig config = answer.getArgumentAt(0, PublishConfig.class);
            final Object message = answer.getArguments()[1];
            publishedMessages.put(config, message);
            return null;
        }).when(bus).publish(any(PublishConfig.class), anyString());

        // when
        publish
            .toQueue(queue)
            .correlatedBy(correlationId)
            .replyTo(replyTo)
            .withHeaders(headers -> headers
                    .put("X-Header1", "value1")
                    .put("X-Header2", "value2"))
            .withRoutingKey(routingKey)
            .messagesByRoutingKey(messages);

        // when
        final PublishConfig expectedConfigBase = PublishConfig.builder()
                .queue(queue)
                .correlationId(correlationId)
                .replyTo(replyTo)
                .withHeader("X-Header1", "value1")
                .withHeader("X-Header2", "value2")
                .build();
        assertThat(publishedMessages).isEqualTo(ImmutableMultimap.of(
                expectedConfigBase.with().routingKey("1.msg1").build(), Message1.of(1, "A"),
                expectedConfigBase.with().routingKey("1.msg2").build(), Message2.of(1, 5),
                expectedConfigBase.with().routingKey("2.msg1").build(), Message1.of(2, "C"),
                expectedConfigBase.with().routingKey("3.msg2").build(), Message2.of(4, 7)));
    }

    @Test
    public void shouldThrowArgumentExceptionWhenTargetExchangeIsNotSpecified() {
        @SuppressWarnings("resource")
        final MessageBus bus = mock(MessageBus.class);
        final Object message = new Object();
        final String exchange = null;
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);

        // when
        final Throwable caughtException = Assertions.catchThrowable(() -> publish
                .toExchange(exchange)
                .correlatedBy(correlationId)
                .replyTo(replyTo)
                .message(message));

        // then
        assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target exchange was not specified");
    }

    @Test
    public void shouldThrowArgumentExceptionWhenTargetExchangeIsEmpty() {
        @SuppressWarnings("resource")
        final MessageBus bus = mock(MessageBus.class);
        final Object message = new Object();
        final String exchange = "";
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);

        // when
        final Throwable caughtException = Assertions.catchThrowable(() -> publish
                .toExchange(exchange)
                .correlatedBy(correlationId)
                .replyTo(replyTo)
                .message(message));

        // then
        assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target exchange was not specified");
    }

    @Test
    public void shouldThrowArgumentExceptionWhenTargetQueueIsNotSpecified() {
        @SuppressWarnings("resource")
        final MessageBus bus = mock(MessageBus.class);
        final Object message = new Object();
        final String queue = null;
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);

        // when
        final Throwable caughtException = Assertions.catchThrowable(() -> publish
                .toQueue(queue)
                .correlatedBy(correlationId)
                .replyTo(replyTo)
                .message(message));

        // then
        assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target queue was not specified");
    }

    @Test
    public void shouldThrowArgumentExceptionWhenTargetQueueIsEmpty() {
        @SuppressWarnings("resource")
        final MessageBus bus = mock(MessageBus.class);
        final Object message = new Object();
        final String queue = "";
        final String correlationId = "123456789";
        final String replyTo = "reply-queue";
        final PublishConfigurator publish = new DefaultPublishConfigurator(bus);

        // when
        final Throwable caughtException = Assertions.catchThrowable(() -> publish
                .toQueue(queue)
                .correlatedBy(correlationId)
                .replyTo(replyTo)
                .message(message));

        // then
        assertThat(caughtException)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target queue was not specified");
    }
}
