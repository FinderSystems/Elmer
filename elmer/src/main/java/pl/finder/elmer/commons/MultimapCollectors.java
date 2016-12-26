package pl.finder.elmer.commons;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultimapCollectors {

    public static <T, K, V> MultimapCollector<T, K, V> toMultimap(final Function<T, K> keyExtractor,
            final Function<T, V> valueExtractor) {
        return new MultimapCollector<>(keyExtractor, valueExtractor);
    }

    public static <T, K, V> MultimapCollector<T, K, T> toMultimap(final Function<T, K> keyExtractor) {
        return new MultimapCollector<>(keyExtractor, v -> v);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class MultimapCollector<T, K, V> implements Collector<T, Multimap<K, V>, Multimap<K, V>> {
        private final Function<T, K> keyExtractor;
        private final Function<T, V> valueExtractor;

        @Override
        public Supplier<Multimap<K, V>> supplier() {
            return ArrayListMultimap::create;
        }

        @Override
        public BiConsumer<Multimap<K, V>, T> accumulator() {
            return (multimap, element) -> multimap.put(keyExtractor.apply(element), valueExtractor.apply(element));
        }

        @Override
        public BinaryOperator<Multimap<K, V>> combiner() {
            return (multimap1, mulitmap2) -> {
                final Multimap<K, V> combined = ArrayListMultimap.create();
                combined.putAll(multimap1);
                combined.putAll(mulitmap2);
                return combined;
            };
        }

        @Override
        public Function<Multimap<K, V>, Multimap<K, V>> finisher() {
            return multimap -> multimap;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
        }
    }
}
