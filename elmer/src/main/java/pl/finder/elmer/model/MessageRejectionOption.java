package pl.finder.elmer.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class MessageRejectionOption {
    private final boolean requeue;

    public static MessageRejectionOption createDefault() {
        return builder()
                .build();
    }

    public static MessageRejectionOption.Builder builder() {
        return new Builder();
    }

    public MessageRejectionOption.Builder with() {
        return builder()
                .requeue(requeue);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Builder {
        private boolean requeue;

        public MessageRejectionOption build() {
            return new MessageRejectionOption(requeue);
        }
    }
}
