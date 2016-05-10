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
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
public final class DeleteQueueOptions {
    private final boolean onlyWhenNotUsed;
    private final boolean onlyWhenNotEmpty;

    public static DeleteQueueOptions empty() {
        return builder()
                .build();
    }

    public static DeleteQueueOptions.Builder builder() {
        return new Builder();
    }

    public DeleteQueueOptions.Builder with() {
        return builder()
                .onlyWhenNotUsed(onlyWhenNotUsed)
                .onlyWhenNotEmpty(onlyWhenNotEmpty);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Builder {
        private boolean onlyWhenNotUsed;
        private boolean onlyWhenNotEmpty;

        public DeleteQueueOptions build() {
            return new DeleteQueueOptions(onlyWhenNotUsed, onlyWhenNotEmpty);
        }
    }
}
