package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vincent Nadoll
 */
class NoopSubstituterTest {

    @Test
    void nonNullOriginal_shouldNotAlterOriginal() {
        String original = "foo";

        Optional<String> substitute = Substituter.noop().substitute(original);

        assertTrue(substitute.isPresent());
        assertEquals(original, substitute.get());
    }

    @Test
    void nullValue_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> Substituter.noop().substitute(null));
    }
}
