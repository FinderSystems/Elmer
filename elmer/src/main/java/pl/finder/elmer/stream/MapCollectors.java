package pl.finder.elmer.stream;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/**
 * Set of collectors for Map and MultiMap creation from streams.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapCollectors {

    /**
     * Returns Map collector of values by key.
     *
     * @param keyMapper function extacting map key from element
     * @return Collector
     */
    public static <T, K> Collector<T, ?, Map<K, T>> toMap(final Function<T, K> keyMapper) {
        final Function<T, T> valueMapper = value -> value;
        return Collectors.toMap(keyMapper, valueMapper);
    }

    /**
     * Returns ImmutableMap collector of values.
     *
     * @param keyMapper function extacting map key from element
     * @param valueMapper function extracting map value from element
     * @return Collector
     */
    public static <T, K, V> Collector<T, ?, Map<K, V>> toImmutableMap(final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        final Supplier<Map<K, V>> supplier = HashMap::new;
        final Function<Map<K, V>, Map<K, V>> finisher = map -> ImmutableMap.copyOf(map);
        final Set<Characteristics> characteristics = ImmutableSet.of(UNORDERED);
        return toMap(keyMapper, valueMapper, finisher, supplier, characteristics);
    }

    /**
     * Returns ImmutableMap collector of values by key.
     *
     * @param keyMapper function extacting map key from element
     * @return Collector
     */
    public static <T, K> Collector<T, ?, Map<K, T>> toImmutableMap(final Function<T, K> keyMapper) {
        final Function<T, T> valueMapper = object -> object;
        return toImmutableMap(keyMapper, valueMapper);
    }

    /**
     * Return Multimap collector of values.
     *
     * @param keyMapper function extacting map key from element
     * @param valueMapper function extracting map value from element
     * @return Collector
     */
    public static <T, K, V> Collector<T, ?, Multimap<K, V>> toMultimap(final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        final Function<Multimap<K, V>, Multimap<K, V>> finisher = multimap -> multimap;
        final Set<Characteristics> characteristics = ImmutableSet.of(IDENTITY_FINISH);
        return toArrayListMultimap(keyMapper, valueMapper, finisher, characteristics);
    }

    /**
     * Return Multimap collector of values by key.
     *
     * @param keyMapper function extacting map key from element
     * @return Collector
     */
    public static <T, K> Collector<T, ?, Multimap<K, T>> toMultimap(final Function<T, K> keyMapper) {
        final Function<T, T> valueMapper = object -> object;
        return toMultimap(keyMapper, valueMapper);
    }

    /**
     * Return SetMultimap collector of values.
     *
     * @param keyMapper function extacting map key from element
     * @param valueMapper function extracting map value from element
     * @return Collector
     */
    public static <T, K, V> Collector<T, ?, SetMultimap<K, V>> toSetMultimap(final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        final Function<SetMultimap<K, V>, SetMultimap<K, V>> finisher = multimap -> multimap;
        final Set<Characteristics> characteristics = ImmutableSet.of(IDENTITY_FINISH);
        return toSetMultimap(keyMapper, valueMapper, finisher, characteristics);
    }

    /**
     * Return SetMultimap collector of values by key.
     *
     * @param keyMapper function extacting map key from element
     * @return Collector
     */
    public static <T, K> Collector<T, ?, SetMultimap<K, T>> toSetMultimap(final Function<T, K> keyMapper) {
        final Function<T, T> valueMapper = object -> object;
        return toSetMultimap(keyMapper, valueMapper);
    }

    /**
     * Return ImmutableMultimap collector of values.
     *
     * @param keyMapper function extacting map key from element
     * @param valueMapper function extracting map value from element
     * @return Collector
     */
    public static <T, K, V> Collector<T, ?, Multimap<K, V>> toImmutableMultimap(final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        final Function<Multimap<K, V>, Multimap<K, V>> finisher = multimap -> ImmutableMultimap.copyOf(multimap);
        final Set<Characteristics> characteristics = ImmutableSet.of(UNORDERED);
        return toArrayListMultimap(keyMapper, valueMapper, finisher, characteristics);
    }

    /**
     * Return ImmutableMultimap collector of values by key.
     *
     * @param keyMapper function extacting map key from element
     * @return Collector
     */
    public static <T, K> Collector<T, ?, Multimap<K, T>> toImmutableMultimap(final Function<T, K> keyMapper) {
        final Function<T, T> valueMapper = object -> object;
        return toImmutableMultimap(keyMapper, valueMapper);
    }

    private static <T, K, V> Collector<T, Multimap<K, V>, Multimap<K, V>> toArrayListMultimap(
            final Function<T, K> keyMapper, final Function<T, V> valueMapper,
            final Function<Multimap<K, V>, Multimap<K, V>> finisher, final Set<Characteristics> characteristics) {
        final Supplier<Multimap<K, V>> supplier = () -> ArrayListMultimap.create();
        return toMultimap(keyMapper, valueMapper, finisher, supplier, characteristics);
    }

    private static <T, K, V> Collector<T, SetMultimap<K, V>, SetMultimap<K, V>> toSetMultimap(
            final Function<T, K> keyMapper, final Function<T, V> valueMapper,
            final Function<SetMultimap<K, V>, SetMultimap<K, V>> finisher, final Set<Characteristics> characteristics) {
        final Supplier<SetMultimap<K, V>> supplier = () -> HashMultimap.create();
        return toMultimap(keyMapper, valueMapper, finisher, supplier, characteristics);
    }

    private static <T, K, V, M extends Multimap<K, V>> Collector<T, M, M> toMultimap(
            final Function<T, K> keyMapper,
            final Function<T, V> valueMapper,
            final Function<M, M> finisher,
            final Supplier<M> supplier,
            final Set<Characteristics> characteristics) {
        final BiConsumer<M, T> accumulator = (multimap, object) -> {
            final K key = keyMapper.apply(object);
            final V value = valueMapper.apply(object);
            multimap.put(key, value);
        };
        final BinaryOperator<M> combiner = multimapCombiner(supplier);
        return CollectorImpl.<T, M, M> builder()
                .accumulator(accumulator)
                .characteristics(characteristics)
                .combiner(combiner)
                .finisher(finisher)
                .supplier(supplier)
                .build();
    }

    private static <T, K, V> Collector<T, Map<K, V>, Map<K, V>> toMap(final Function<T, K> keyMapper,
            final Function<T, V> valueMapper, final Function<Map<K, V>, Map<K, V>> finisher,
            final Supplier<Map<K, V>> supplier,
            final Set<Characteristics> characteristics) {
        final BiConsumer<Map<K, V>, T> accumulator = (map, object) -> {
            final K key = keyMapper.apply(object);
            final V value = valueMapper.apply(object);
            map.put(key, value);
        };
        final BinaryOperator<Map<K, V>> combiner = mapCombiner(supplier);
        return CollectorImpl.<T, Map<K, V>, Map<K, V>> builder()
                .accumulator(accumulator)
                .characteristics(characteristics)
                .combiner(combiner)
                .finisher(finisher)
                .supplier(supplier)
                .build();
    }

    private static <K, V, M extends Multimap<K, V>> BinaryOperator<M> multimapCombiner(
            final Supplier<M> supplier) {
        return (first, second) -> {
            final M combined = supplier.get();
            combined.putAll(first);
            combined.putAll(second);
            return combined;
        };
    }

    private static <K, V> BinaryOperator<Map<K, V>> mapCombiner(final Supplier<Map<K, V>> supplier) {
        return (first, second) -> {
            final Map<K, V> combined = supplier.get();
            combined.putAll(first);
            combined.putAll(second);
            return combined;
        };
    }

    @Getter
    @Accessors(fluent = true)
    @Builder
    private static class CollectorImpl<T, A, V> implements Collector<T, A, V> {
        private final BiConsumer<A, T> accumulator;
        private final Set<Characteristics> characteristics;
        private final BinaryOperator<A> combiner;
        private final Function<A, V> finisher;
        private final Supplier<A> supplier;
    }

}
