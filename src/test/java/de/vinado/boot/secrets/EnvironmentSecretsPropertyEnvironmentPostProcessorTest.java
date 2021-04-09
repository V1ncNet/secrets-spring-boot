package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Vincent Nadoll
 */
class EnvironmentSecretsPropertyEnvironmentPostProcessorTest {

    private static SpringApplication application;

    private ConfigurableEnvironment environment;
    private EnvironmentSecretsPropertyEnvironmentPostProcessor processor;

    @BeforeAll
    static void beforeAll() {
        application = mock(SpringApplication.class);
    }

    @BeforeEach
    void setUp() {
        environment = spy(new StandardEnvironment());
        processor = new EnvironmentSecretsPropertyEnvironmentPostProcessor(Supplier::get);
    }

    @Test
    void classpathUri_shouldSetUsernameProperty() {
        setProperty("SPRING_DATASOURCE_USERNAME_FILE", "classpath:spring.datasource.username");

        processor.postProcessEnvironment(environment, application);

        assertEquals("alice", environment.getProperty("spring.datasource.username"));
    }

    @Test
    void fileUri_shouldSetPasswordProperty() {
        setProperty("SPRING_DATASOURCE_PASSWORD_FILE", String.format("file:%s/src/test/resources/spring_datasource_password", System.getProperty("user.dir")));

        processor.postProcessEnvironment(environment, application);

        assertEquals("password1234", environment.getProperty("spring.datasource.password"));
    }

    @Test
    void invalidLocation_shouldNotSetProperty() {
        setProperty("INVALID_SCHEMA_FILE", "env:spring.datasource.username");

        processor.postProcessEnvironment(environment, application);

        assertNull(environment.getProperty("invalid.schema"));
    }

    @Test
    void nullLocation_shouldNotSetProperty() {
        setProperty("NULL_VARIABLE_FILE", null);

        processor.postProcessEnvironment(environment, application);

        assertNull(environment.getProperty("null.variable"));
    }

    @Test
    void emptyLocation_shouldNotSetProperty() {
        setProperty("EMPTY_VARIABLE_FILE", "");

        processor.postProcessEnvironment(environment, application);

        assertNull(environment.getProperty("empty.variable"));
    }

    @Test
    void emptySecret_shouldNotSetProperty() {
        setProperty("EMPTY_SECRET_FILE", "classpath:secret.empty");

        processor.postProcessEnvironment(environment, application);

        assertNull(environment.getProperty("empty.secret"));
    }

    @Test
    void randomText_shouldNotSetProperty() {
        setProperty("SECRET_UUID_FILE", UUID.randomUUID().toString());

        processor.postProcessEnvironment(environment, application);

        assertNull(environment.getProperty("secret.uuid"));
    }

    private void setProperty(String key, String value) {
        when(environment.getSystemEnvironment()).thenReturn(Collections.singletonMap(key, value));
        when(environment.getProperty(key)).thenReturn(value);
    }
}
