package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Vincent Nadoll
 */
class EnvironmentPropertyIndexSupplierTest {

    private ConfigurableEnvironment environment;
    private Map<String, String> index;

    @BeforeEach
    void setUp() {
        environment = spy(new StandardEnvironment());
    }

    @Test
    void initializingNullArguments_shouldThrowException() {
        DeferredLogFactory logFactory = Supplier::get;

        assertThrows(NullPointerException.class, () -> new EnvironmentPropertyIndexSupplier(logFactory, null));
        assertThrows(NullPointerException.class, () -> new EnvironmentPropertyIndexSupplier(null, environment));
        assertThrows(NullPointerException.class, () -> new EnvironmentPropertyIndexSupplier(null, null));

        assertThrows(NullPointerException.class, () -> new EnvironmentPropertyIndexSupplier(logFactory, null, null));
        assertThrows(NullPointerException.class, () -> new EnvironmentPropertyIndexSupplier(null, environment, null));
        assertThrows(NullPointerException.class, () -> new EnvironmentPropertyIndexSupplier(null, null, "_FILE"));
        assertThrows(NullPointerException.class, () -> new EnvironmentPropertyIndexSupplier(null, null, null));
    }

    @Test
    void environmentProperties_shouldCreateIndex() {
        addProperty("SPRING_DATASOURCE_USERNAME_FILE", "classpath:spring.datasource.username");
        addProperty("SPRING_DATASOURCE_PASSWORD_FILE", "classpath:spring_datasource_password");
        addProperty("HOME", "/home/bob");
        addProperty("PASSWORD_FILE_SECRET", "foo");

        createIndex("");

        assertNotNull(index);
        assertTrue(index.size() >= 4);

        assertEntry("spring.datasource.username.file", "classpath:spring.datasource.username");
        assertEntry("spring.datasource.password.file", "classpath:spring_datasource_password");
        assertEntry("home", "/home/bob");
        assertEntry("password.file.secret", "foo");
    }

    @Test
    void fileSuffixEnvironmentProperties_shouldCreateIndex() {
        addProperty("SPRING_DATASOURCE_USERNAME_FILE", "classpath:spring.datasource.username");
        addProperty("SPRING_DATASOURCE_PASSWORD_FILE", "classpath:spring_datasource_password");
        addProperty("HOME", "/home/bob");
        addProperty("PASSWORD_FILE_SECRET", "foo");

        createIndex("_FILE");

        assertNotNull(index);
        assertEquals(2, index.size());

        assertEntry("spring.datasource.username", "classpath:spring.datasource.username");
        assertEntry("spring.datasource.password", "classpath:spring_datasource_password");
    }

    private void addProperty(String key, String value) {
        Map<String, Object> systemProperties = new HashMap<>(environment.getSystemEnvironment());
        systemProperties.put(key, value);
        when(environment.getSystemEnvironment()).thenReturn(systemProperties);
        when(environment.getProperty(key)).thenReturn(value);
    }

    private void createIndex(String suffix) {
        EnvironmentPropertyIndexSupplier supplier = new EnvironmentPropertyIndexSupplier(Supplier::get, environment, suffix);
        index = supplier.get();
    }

    private void assertEntry(String key, String value) {
        assertTrue(index.containsKey(key));
        assertEquals(value, index.get(key));
    }
}
