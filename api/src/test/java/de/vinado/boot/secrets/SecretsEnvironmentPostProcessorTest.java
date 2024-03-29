package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretsEnvironmentPostProcessorTest {

    @Test
    void singletonPropertyIndex_shouldAddPropertyToEnvironment() {
        DeferredLogFactory logFactory = Supplier::get;
        ConfigurableEnvironment environment = new StandardEnvironment();
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        SecretsEnvironmentPostProcessor processor = new SecretsEnvironmentPostProcessor(logFactory) {
            @Override
            protected SecretResolver getSecretResolver(ResourceLoader resourceLoader) {
                return Optional::of;
            }

            @Override
            protected PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment) {
                return () -> Collections.singletonMap("spring.datasource.username", "bob");
            }
        };

        processor.postProcessEnvironment(environment, resourceLoader);
        String property = environment.getProperty("spring.datasource.username");

        assertNotNull(property);
        assertTrue(environment.getPropertySources().contains(SecretPropertiesPropertySource.NAME));
        assertEquals("bob", property);
    }
}
