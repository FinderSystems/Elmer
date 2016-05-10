package pl.finder.elmer.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AmqpProperties {
    public static String DelayedType = "x-delayed-type";
    public static String DeletedMessage = "x-delayed-message";
    public static String MessageTimeToLive = "x-message-ttl";
    public static String Expires = "x-expires";
    public static String MaxPriority = "x-max-priority";
    public static String DeadLetterExchange = "x-dead-letter-exchange";
    public static String DeadLetterRoutingKey = "x-dead-letter-routing-key";
    public static String MaxLength = "x-max-length";
    public static String MaxLengthBytes = "x-max-length-bytes";
}
