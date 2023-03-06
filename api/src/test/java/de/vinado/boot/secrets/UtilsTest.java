package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.log.LogMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UtilsTest {

    private Log log;

    @BeforeEach
    void setUp() {
        DeferredLogFactory logFactory = Supplier::get;
        log = spy(logFactory.getLog(getClass()));
    }

    @Test
    void acceptAndLog() {
        List<Integer> list = new ArrayList<>();
        Utils.<Integer>acceptAndLog(list::add, log::info, "Added value: %d", Function.identity()).accept(0);

        assertEquals(1, list.size());
        assertEquals(0, list.get(0));

        verify(log, times(1)).info(any(LogMessage.class));
    }

    @Test
    void testAndLogFailure_shouldLog() {
        Utils.<Integer>testAndLogFailure(integer -> integer == 1, log::info, "1 != %d", Function.identity()).test(0);

        verify(log, times(1)).info(any(LogMessage.class));
    }

    @Test
    void testAndLogFailure_shouldNotLog() {
        Utils.<Integer>testAndLogFailure(integer -> integer == 0, log::info, "1 != %d", Function.identity()).test(0);

        verify(log, never()).info(any(LogMessage.class));
    }

    @Test
    void log() {
        Integer a = 2;
        Integer b = 2;
        Utils.log(a, log::info, "%d + %d = %d", Function.identity(), integer -> b, integer -> integer + b);

        verify(log, times(1)).info(any(LogMessage.class));
    }

    @Test
    void top_shouldNegate() {
        boolean negation = Utils.not(integer -> true).test(0);

        assertFalse(negation);
    }

    @Test
    void startsWith() {
        boolean startsWith = Utils.startsWith("secrets.file.properties")
            .test("secrets.file.properties.spring.datasource.username");

        assertTrue(startsWith);
    }

    @Test
    void substring() {
        String substring = Utils.substring(24).apply("secrets.file.properties.spring.datasource.username");

        assertEquals("spring.datasource.username", substring);
    }

    @Test
    void endsWith() {
        boolean startsWith = Utils.endsWith("_FILE").test("SPRING_DATASOURCE_USERNAME_FILE");

        assertTrue(startsWith);
    }
}
