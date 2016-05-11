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
public final class DeleteExchangeOptions {
    private final boolean onlyWhenNotUsed;

    public static DeleteExchangeOptions createDefault() {
        return builder()
                .build();
    }

    public static DeleteExchangeOptions.Builder builder() {
        return new Builder();
    }

    public DeleteExchangeOptions.Builder with() {
        return builder()
                .onlyWhenNotUsed(onlyWhenNotUsed);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Setter
    @Accessors(fluent = true)
    public static final class Builder {
        private boolean onlyWhenNotUsed;

        public DeleteExchangeOptions build() {
            return new DeleteExchangeOptions(onlyWhenNotUsed);
        }
    }
}
