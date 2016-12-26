package pl.finder.elmer.serialization;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MessageTypes {
    private static final LoadingCache<String, Class<?>> cache = CacheBuilder.newBuilder()
            .build(loadClass());

    static Class<?> byName(final String className) {
        try {
            return cache.get(className);
        } catch (final ExecutionException e) {
            throw new IllegalStateException(String.format("Could not load class: '%s'", className), e);
        }
    }

    private static CacheLoader<String, Class<?>> loadClass() {
        return new CacheLoader<String, Class<?>>() {
            @Override
            public Class<?> load(final String className) throws Exception {
                return Class.forName(className);
            }
        };
    }
}
