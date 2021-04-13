package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Supplier;

import static de.vinado.boot.secrets.TestUtils.fileUriFromClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Vincent Nadoll
 */
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
    void invalidLocation_shouldNotSetProperty() {
        setProperty("INVALID_SCHEMA_FILE", "env:spring.datasource.username");

        postProcessEnvironment();

        assertNull(environment.getProperty("invalid.schema"));
    }

    @Test
    void nullLocation_shouldNotSetProperty() {
        setProperty("NULL_VARIABLE_FILE", null);

        postProcessEnvironment();

        assertNull(environment.getProperty("null.variable"));
    }

    @Test
    void emptyLocation_shouldNotSetProperty() {
        setProperty("EMPTY_VARIABLE_FILE", "");

        postProcessEnvironment();

        assertNull(environment.getProperty("empty.variable"));
    }

    @Test
    void emptySecret_shouldNotSetProperty() {
        setProperty("EMPTY_SECRET_FILE", "classpath:secret.empty");

        postProcessEnvironment();

        assertNull(environment.getProperty("empty.secret"));
    }

    @Test
    void randomText_shouldNotSetProperty() {
        setProperty("SECRET_UUID_FILE", UUID.randomUUID().toString());

        postProcessEnvironment();

        assertNull(environment.getProperty("secret.uuid"));
    }
}
