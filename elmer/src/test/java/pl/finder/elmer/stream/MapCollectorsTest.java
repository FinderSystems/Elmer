package pl.finder.elmer.stream;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

public class MapCollectorsTest {

    @Test
    public void shouldCollectListElementsByKeyToMap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(2, "B"),
                Data.create(3, "C"));
        final Function<Data, Integer> keyMapper = data -> data.id();

        // when
        final Map<Integer, Data> map = list.stream().collect(MapCollectors.toMap(keyMapper));

        // then
        final Map<Integer, Data> expected = ImmutableMap.of(
                1, Data.create(1, "A"),
                2, Data.create(2, "B"),
                3, Data.create(3, "C"));
        assertThat(map).isEqualTo(expected);
    }

    @Test
    public void shouldCollectListElementsByKeyToImmutableMap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(2, "B"),
                Data.create(3, "C"));
        final Function<Data, Integer> keyMapper = data -> data.id();

        // when
        final Map<Integer, Data> map = list.stream().collect(MapCollectors.toImmutableMap(keyMapper));

        // then
        final Map<Integer, Data> expected = ImmutableMap.of(
                1, Data.create(1, "A"),
                2, Data.create(2, "B"),
                3, Data.create(3, "C"));
        assertThat(map).isEqualTo(expected);
        assertThat((Object) map).isInstanceOf(ImmutableMap.class);
    }

    @Test
    public void shouldCollectListToImmutableMap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(2, "B"),
                Data.create(3, "C"));
        final Function<Data, Integer> keyMapper = data -> data.id();
        final Function<Data, String> valueMapper = data -> data.value();

        // when
        final Map<Integer, String> map = list.stream().collect(MapCollectors.toImmutableMap(keyMapper, valueMapper));

        // then
        final Map<Integer, String> expected = ImmutableMap.of(
                1, "A",
                2, "B",
                3, "C");
        assertThat(map).isEqualTo(expected);
        assertThat((Object) map).isInstanceOf(ImmutableMap.class);
    }

    @Test
    public void shouldCollectListToMultimap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(1, "B"),
                Data.create(2, "A"),
                Data.create(3, "C"));
        final Function<Data, Integer> keyMapper = data -> data.id();
        final Function<Data, String> valueMapper = data -> data.value();

        // when
        final Multimap<Integer, String> multimap = list.stream().collect(
                MapCollectors.toMultimap(keyMapper, valueMapper));

        // then
        final Multimap<Integer, String> expected = ImmutableMultimap.of(
                1, "A",
                1, "B",
                2, "A",
                3, "C");
        assertThat(multimap).isEqualTo(expected);
        assertThat(multimap).isInstanceOf(ArrayListMultimap.class);
    }

    @Test
    public void shouldCollectListElementsByKeyToMultimap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(1, "B"),
                Data.create(2, "A"),
                Data.create(3, "C"));
        final Function<Data, Integer> keyMapper = data -> data.id();

        // when
        final Multimap<Integer, Data> multimap = list.stream().collect(
                MapCollectors.toMultimap(keyMapper));

        // then
        final Multimap<Integer, Data> expected = ImmutableMultimap.of(
                1, Data.create(1, "A"),
                1, Data.create(1, "B"),
                2, Data.create(2, "A"),
                3, Data.create(3, "C"));
        assertThat(multimap).isEqualTo(expected);
        assertThat(multimap).isInstanceOf(ArrayListMultimap.class);
    }

    @Test
    public void shouldCollectListToSetMultimap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(1, "A"),
                Data.create(2, "B"),
                Data.create(2, "B"));
        final Function<Data, Integer> keyMapper = data -> data.id();
        final Function<Data, String> valueMapper = data -> data.value();

        // when
        final Multimap<Integer, String> multimap = list.stream().collect(
                MapCollectors.toSetMultimap(keyMapper, valueMapper));

        // then
        final Multimap<Integer, String> expected = ImmutableSetMultimap.of(
                1, "A",
                2, "B");
        assertThat(multimap).isEqualTo(expected);
        assertThat(multimap).isInstanceOf(SetMultimap.class);
    }

    @Test
    public void shouldCollectListElementsByKeyToSetMultimap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(1, "A"),
                Data.create(2, "B"),
                Data.create(2, "B"));
        final Function<Data, Integer> keyMapper = data -> data.id();

        // when
        final Multimap<Integer, Data> multimap = list.stream().collect(
                MapCollectors.toSetMultimap(keyMapper));

        // then
        final Multimap<Integer, Data> expected = ImmutableSetMultimap.of(
                1, Data.create(1, "A"),
                2, Data.create(2, "B"));
        assertThat(multimap).isEqualTo(expected);
        assertThat(multimap).isInstanceOf(SetMultimap.class);
    }

    @Test
    public void shouldCollectListToImmutableMultimap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(1, "B"),
                Data.create(2, "A"),
                Data.create(3, "C"));
        final Function<Data, Integer> keyMapper = data -> data.id();
        final Function<Data, String> valueMapper = data -> data.value();

        // when
        final Multimap<Integer, String> multimap = list.stream().collect(
                MapCollectors.toImmutableMultimap(keyMapper, valueMapper));

        // then
        final Multimap<Integer, String> expected = ImmutableMultimap.of(
                1, "A",
                1, "B",
                2, "A",
                3, "C");
        assertThat(multimap).isEqualTo(expected);
        assertThat(multimap).isInstanceOf(ImmutableMultimap.class);
    }

    @Test
    public void shouldCollectListElementsByKeyToImmutableMultimap() {
        // given
        final List<Data> list = Arrays.asList(
                Data.create(1, "A"),
                Data.create(1, "B"),
                Data.create(2, "A"),
                Data.create(3, "C"));
        final Function<Data, Integer> keyMapper = data -> data.id();

        // when
        final Multimap<Integer, Data> multimap = list.stream().collect(
                MapCollectors.toImmutableMultimap(keyMapper));

        // then
        final Multimap<Integer, Data> expected = ImmutableMultimap.of(
                1, Data.create(1, "A"),
                1, Data.create(1, "B"),
                2, Data.create(2, "A"),
                3, Data.create(3, "C"));
        assertThat(multimap).isEqualTo(expected);
        assertThat(multimap).isInstanceOf(ImmutableMultimap.class);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "create")
    @Getter
    @Accessors(fluent = true)
    @EqualsAndHashCode
    @ToString
    private static class Data {
        private final int id;
        private final String value;
    }
}
