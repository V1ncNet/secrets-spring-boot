package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretsEnvironmentTest {

    private ConfigurableEnvironment environment;
    private PropertyIndexSupplier propertyIndexSupplier;
    private SecretResolver resolver;
    private SecretsEnvironment secretsEnvironment;

    @BeforeEach
    void setUp() {
        resolver = Optional::of;
        environment = new StandardEnvironment();
    }

    @Test
    void initializingNullArguments_shouldThrowException() {
        PropertyIndexSupplier indexSupplier = Collections::emptyMap;

        assertThrows(IllegalArgumentException.class, () -> new SecretsEnvironment(Supplier::get, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new SecretsEnvironment(null, environment, null, null));
        assertThrows(IllegalArgumentException.class, () -> new SecretsEnvironment(null, null, resolver, null));
        assertThrows(IllegalArgumentException.class, () -> new SecretsEnvironment(null, null, null, indexSupplier));
        assertThrows(IllegalArgumentException.class, () -> new SecretsEnvironment(null, null, null, null));
    }

    @Test
    void emptyPropertyIndex_shouldAddPropertyToEnvironment() {
        propertyIndexSupplier = Collections::emptyMap;
        secretsEnvironment = new SecretsEnvironment(Supplier::get, environment, resolver, propertyIndexSupplier);

        secretsEnvironment.processAndApply();

        assertFalse(environment.getPropertySources().contains(SecretPropertiesPropertySource.NAME));
    }

    @Test
    void singletonPropertyIndex_shouldAddPropertyToEnvironment() {
        propertyIndexSupplier = () -> Collections.singletonMap("spring.datasource.username", "bob");
        secretsEnvironment = new SecretsEnvironment(Supplier::get, environment, resolver, propertyIndexSupplier);

        secretsEnvironment.processAndApply();
        String property = environment.getProperty("spring.datasource.username");

        assertNotNull(property);
        assertTrue(environment.getPropertySources().contains(SecretPropertiesPropertySource.NAME));
        assertEquals("bob", property);
    }
}
