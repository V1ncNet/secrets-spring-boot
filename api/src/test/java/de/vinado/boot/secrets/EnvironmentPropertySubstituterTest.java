package de.vinado.boot.secrets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vincent Nadoll
 */
class EnvironmentPropertySubstituterTest {

    private static final String FOO_TEST_ENV_SUB = "TEST_ENV_SUB";
    private static final String BAR_TEST_ENV_SUB = "BAR_TEST_ENV_SUB";
    private static final String EMPTY_TEST_ENV_SUB = "EMPTY_TEST_ENV_SUB";

    private EnvironmentPropertySubstituter substituter;

    @BeforeAll
    static void beforeAll() {
        System.setProperty(FOO_TEST_ENV_SUB, "foo");
        System.setProperty(BAR_TEST_ENV_SUB, " bar   \n");
        System.setProperty(EMPTY_TEST_ENV_SUB, "");
    }

    @BeforeEach
    void setUp() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        substituter = new EnvironmentPropertySubstituter(environment);
    }

    @Test
    void validProperty_shouldSubstitute() {
        Optional<String> substitute = substituter.substitute(FOO_TEST_ENV_SUB);

        assertTrue(substitute.isPresent());
        assertEquals("foo", substitute.get());
    }

    @Test
    void unnecessarySpaces_shouldTrimAndSubstitute() {
        Optional<String> substitute = substituter.substitute(BAR_TEST_ENV_SUB);

        assertTrue(substitute.isPresent());
        assertEquals("bar", substitute.get());
    }

    @Test
    void emptyProperty_shouldNotSubstitute() {
        Optional<String> substitute = substituter.substitute(EMPTY_TEST_ENV_SUB);

        assertFalse(substitute.isPresent());
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty(FOO_TEST_ENV_SUB);
        System.clearProperty(BAR_TEST_ENV_SUB);
        System.clearProperty(EMPTY_TEST_ENV_SUB);
    }
}
