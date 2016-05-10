package pl.finder.elmer.extensions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.Test;
import org.slf4j.Logger;

public class LoggerExtensionsTest {

    @Test
    public void shouldWriteDebugMessage() {
        // given
        final Logger log = mock(Logger.class);
        final String mesage = "this is debug message";
        final boolean debugEnabled = true;
        @SuppressWarnings("unchecked")
        final Supplier<String> messageSupplier = mock(Supplier.class);
        when(log.isDebugEnabled()).thenReturn(debugEnabled);
        when(messageSupplier.get()).thenReturn(mesage);

        // when
        LoggerExtensions.debug(log, messageSupplier);

        // then
        verify(log, times(1)).debug(mesage);
        verify(messageSupplier, times(1)).get();
    }

    @Test
    public void shouldWriteTraceMessage() {
        // given
        final Logger log = mock(Logger.class);
        final String mesage = "this is debug message";
        final boolean traceEnabled = true;
        @SuppressWarnings("unchecked")
        final Supplier<String> messageSupplier = mock(Supplier.class);
        when(log.isTraceEnabled()).thenReturn(traceEnabled);
        when(messageSupplier.get()).thenReturn(mesage);

        // when
        LoggerExtensions.trace(log, messageSupplier);

        // then
        verify(log, times(1)).trace(mesage);
        verify(messageSupplier, times(1)).get();
    }

    @Test
    public void shouldNotWriteDebugMessageAndNotInvokeMessageSupplierWhenDebugLevelIsDisabled() {
        // given
        final Logger log = mock(Logger.class);
        final String mesage = "this is debug message";
        final boolean debugEnabled = false;
        @SuppressWarnings("unchecked")
        final Supplier<String> messageSupplier = mock(Supplier.class);
        when(log.isDebugEnabled()).thenReturn(debugEnabled);
        when(messageSupplier.get()).thenReturn(mesage);

        // when
        LoggerExtensions.debug(log, messageSupplier);

        // then
        verify(log, never()).debug(mesage);
        verify(messageSupplier, never()).get();
    }

    @Test
    public void shouldNotWriteTraceMessageAndNotInvokeMessageSupplierWhenTraceLevelIsDisabled() {
        // given
        final Logger log = mock(Logger.class);
        final String mesage = "this is debug message";
        final boolean traceEnabled = false;
        @SuppressWarnings("unchecked")
        final Supplier<String> messageSupplier = mock(Supplier.class);
        when(log.isTraceEnabled()).thenReturn(traceEnabled);
        when(messageSupplier.get()).thenReturn(mesage);

        // when
        LoggerExtensions.debug(log, messageSupplier);

        // then
        verify(log, never()).debug(mesage);
        verify(messageSupplier, never()).get();
    }
}
