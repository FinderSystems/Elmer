package pl.finder.elmer.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor(staticName = "of")
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class Message2 {
    private final int id;
    private final Integer value;
}
