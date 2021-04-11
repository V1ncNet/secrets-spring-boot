package de.vinado.boot.secrets;

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

    @Test
    void initializingNullArguments_shouldThrowException() {
        DeferredLogFactory logFactory = Supplier::get;
        ConfigurableEnvironment environment = new StandardEnvironment();

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
        Map<String, Object> properties = new HashMap<>();
        properties.put("SPRING_DATASOURCE_USERNAME_FILE", "classpath:spring.datasource.username");
        properties.put("SPRING_DATASOURCE_PASSWORD_FILE", "classpath:spring_datasource_password");
        properties.put("HOME", "/home/bob");
        properties.put("PASSWORD_FILE_SECRET", "foo");

        ConfigurableEnvironment environment = spy(new StandardEnvironment());
        EnvironmentPropertyIndexSupplier supplier = new EnvironmentPropertyIndexSupplier(Supplier::get, environment);
        when(environment.getSystemEnvironment()).thenReturn(properties);
        properties.forEach((key, value) -> when(environment.getProperty(key)).thenReturn(String.valueOf(value)));

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(4, index.size());

        assertTrue(index.containsKey("spring.datasource.username.file"));
        assertEquals("classpath:spring.datasource.username", index.get("spring.datasource.username.file"));

        assertTrue(index.containsKey("spring.datasource.password.file"));
        assertEquals("classpath:spring_datasource_password", index.get("spring.datasource.password.file"));

        assertTrue(index.containsKey("home"));
        assertEquals("/home/bob", index.get("home"));

        assertTrue(index.containsKey("password.file.secret"));
        assertEquals("foo", index.get("password.file.secret"));
    }

    @Test
    void fileSuffixEnvironmentProperties_shouldCreateIndex() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("SPRING_DATASOURCE_USERNAME_FILE", "classpath:spring.datasource.username");
        properties.put("SPRING_DATASOURCE_PASSWORD_FILE", "classpath:spring_datasource_password");
        properties.put("HOME", "/home/bob");
        properties.put("PASSWORD_FILE_SECRET", "foo");

        ConfigurableEnvironment environment = spy(new StandardEnvironment());
        EnvironmentPropertyIndexSupplier supplier = new EnvironmentPropertyIndexSupplier(Supplier::get, environment, "_FILE");
        when(environment.getSystemEnvironment()).thenReturn(properties);
        properties.forEach((key, value) -> when(environment.getProperty(key)).thenReturn(String.valueOf(value)));

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(2, index.size());

        assertTrue(index.containsKey("spring.datasource.username"));
        assertEquals("classpath:spring.datasource.username", index.get("spring.datasource.username"));

        assertTrue(index.containsKey("spring.datasource.password"));
        assertEquals("classpath:spring_datasource_password", index.get("spring.datasource.password"));
    }
}
