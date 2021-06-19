package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Vincent Nadoll
 */
class CompositePropertyIndexSupplierTest {

    private ConfigurableEnvironment environment;
    private Map<String, String> first;
    private Map<String, String> second;

    @BeforeEach
    void setUp() {
        environment = spy(new StandardEnvironment());
        first = Collections.singletonMap("foo", "bar");
        second = Collections.singletonMap("foo", "baz");
    }

    @Test
    void initializeBuilderWithNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> CompositePropertyIndexSupplier.using(null));
    }

    @Test
    void throwingStrategy_shouldTrowException() {
        CompositePropertyIndexSupplier supplier = CompositePropertyIndexSupplier.builder()
            .add(first)
            .add(second)
            .build();

        assertThrows(IllegalStateException.class, supplier::get);
    }

    @Test
    void firstComeFirstServeStrategy_shouldKeepInitialValue() {
        CompositePropertyIndexSupplier supplier = CompositePropertyIndexSupplier.keeping()
            .add(first)
            .add(second)
            .build();

        Map<String, String> result = supplier.get();

        assertEquals(first, result);
    }

    @Test
    void overridingStrategy_shouldOverrideExistingValue() {
        CompositePropertyIndexSupplier supplier = CompositePropertyIndexSupplier.overriding()
            .add(first)
            .add(second)
            .build();

        Map<String, String> result = supplier.get();

        assertEquals(second, result);
    }

    @Test
    void addNullToBuilder_shouldThrowException() {
        CompositePropertyIndexSupplier.Builder builder = CompositePropertyIndexSupplier.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.add((PropertyIndexSupplier) null));
        assertThrows(IllegalArgumentException.class, () -> builder.add((Map<String, String>) null));
        assertThrows(IllegalArgumentException.class, () -> builder.addAll(null));
    }

    @Test
    void buildThenSubstitute_shouldEqualBuildAndSubstituteResult() {
        setProperty("DATABASE_USER_FILE", "/run/secrets/db_username");
        Map<String, String> first = Collections.singletonMap("spring.datasource.username", "DATABASE_USER_FILE");
        Map<String, String> second = Collections.singletonMap("spring.datasource.password", "/run/secrets/db_password");

        CompositePropertyIndexSupplier.Builder builder = CompositePropertyIndexSupplier.builder()
            .add(PropertyIndexSupplier.from(first))
            .add(PropertyIndexSupplier.from(second));

        Map<String, String> buildThenSubstitute = builder.build().substituteValues(environment).get();
        Map<String, String> BuildAndSubstitute = builder.buildAndSubstitute(environment).get();

        assertEquals(buildThenSubstitute, BuildAndSubstitute);
    }

    @Test
    void missingPropertyResolver_shouldNotSubstitute() {
        setProperty("DATABASE_USER_FILE", "/run/secrets/db_username");
        Map<String, String> first = Collections.singletonMap("spring.datasource.username", "DATABASE_USER_FILE");

        CompositePropertyIndexSupplier.Builder builder = CompositePropertyIndexSupplier.builder()
            .add(PropertyIndexSupplier.from(first));

        Map<String, String> result = builder.build().get();

        assertEquals(1, result.size());
        assertTrue(result.containsKey("spring.datasource.username"));
        assertEquals("DATABASE_USER_FILE", result.get("spring.datasource.username"));
    }

    void setProperty(String key, String value) {
        when(environment.getSystemEnvironment()).thenReturn(Collections.singletonMap(key, value));
        when(environment.getProperty(key)).thenReturn(value);
        when(environment.getProperty(key, key)).thenReturn(value);
    }
}
