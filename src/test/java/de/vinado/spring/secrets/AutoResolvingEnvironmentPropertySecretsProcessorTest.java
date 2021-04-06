package de.vinado.spring.secrets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Vincent Nadoll
 */
class AutoResolvingEnvironmentPropertySecretsProcessorTest {

    private static final String CWD = System.getProperty("user.dir");
    private static final ApplicationContextFactory contextFactory = ApplicationContextFactory.DEFAULT;

    private static SpringApplication application;

    private ConfigurableEnvironment environment;
    private AutoResolvingEnvironmentPropertySecretsProcessor processor;

    @BeforeAll
    static void beforeAll() {
        application = mock(SpringApplication.class);
    }

    @BeforeEach
    void setUp() {
        environment = spy(contextFactory.create(WebApplicationType.NONE).getEnvironment());
        processor = new AutoResolvingEnvironmentPropertySecretsProcessor(Supplier::get);
    }

    @Test
    void classpathUri_shouldSetUsernameProperty() {
        setProperty("SPRING_DATASOURCE_USERNAME_FILE", "classpath:spring.datasource.username");

        processor.postProcessEnvironment(environment, application);

        assertEquals("alice", environment.getProperty("spring.datasource.username"));
    }

    @Test
    void fileUri_shouldSetPasswordProperty() {
        setProperty("SPRING_DATASOURCE_PASSWORD_FILE", String.format("file:%s/src/test/resources/spring_datasource_password", CWD));

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

    private void setProperty(String key, String value) {
        when(environment.getSystemEnvironment()).thenReturn(Collections.singletonMap(key, value));
        when(environment.getProperty(key)).thenReturn(value);
    }
}
