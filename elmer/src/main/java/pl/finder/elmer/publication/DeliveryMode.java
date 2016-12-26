package pl.finder.elmer.publication;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Enumeration with delivery mode types.
 */
public enum DeliveryMode {

    Default(null),

    /**
     * Non persistent delivery mode: 1.
     */
    NonPersistent(1),

    /**
     * Persistent delivery mode: 2.
     */
    Persistent(2);

    private static final Map<Integer, DeliveryMode> ValuesById = ImmutableMap.copyOf(stream(values())
            .filter(it -> it.value() != null)
            .collect(toMap(DeliveryMode::value, it -> it)));

    private final Integer value;

    private DeliveryMode(final Integer value) {
        this.value = value;
    }

    /**
     * Returns delivery mode integer value.
     *
     * @return integer value of delivery mode.
     */
    public Integer value() {
        return value;
    }

    public static DeliveryMode valueOf(final Integer value) {
        if (value == null) {
            return Default;
        }
        checkArgument(ValuesById.containsKey(value),
                String.format("Unsupported deliveryMode: %s", value));
        return ValuesById.get(value);
    }
}
