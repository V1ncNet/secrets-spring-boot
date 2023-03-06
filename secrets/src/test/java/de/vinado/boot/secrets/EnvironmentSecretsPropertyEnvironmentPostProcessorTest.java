package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static de.vinado.boot.secrets.TestUtils.fileUriFromClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvironmentSecretsPropertyEnvironmentPostProcessorTest extends AbstractSecretsEnvironmentPostProcessorTest {

    @Override
    protected SecretsEnvironmentPostProcessor createPostProcessor() {
        return new EnvironmentSecretsPropertyEnvironmentPostProcessor(Supplier::get);
    }

    @Test
    void suffixedEnvironmentProperty_shouldAddToEnvironment() {
        setProperty("SPRING_DATASOURCE_PASSWORD_FILE", fileUriFromClasspath("spring_datasource_password"));

        postProcessEnvironment();

        assertEquals("password1234", environment.getProperty("spring.datasource.password"));
    }

    @Test
    void nonSuffixedEnvironmentProperty_shouldAddToEnvironment() {
        setProperty("SPRING_DATASOURCE_USERNAME", "classpath:spring.datasource.username");

        postProcessEnvironment();

        assertNull(environment.getProperty("spring.datasource.username"));
    }
}
