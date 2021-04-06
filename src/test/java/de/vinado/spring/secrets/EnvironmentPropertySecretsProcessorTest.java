package de.vinado.spring.secrets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class EnvironmentPropertySecretsProcessorTest {

    private static final String CWD = System.getProperty("user.dir");
    private static final ApplicationContextFactory contextFactory = ApplicationContextFactory.DEFAULT;

    private static SpringApplication application;

    private ConfigurableEnvironment environment;
    private EnvironmentPropertySecretsProcessor processor;

    @BeforeAll
    static void beforeAll() {
        application = mock(SpringApplication.class);
    }

    @BeforeEach
    void setUp() {
        environment = spy(contextFactory.create(WebApplicationType.NONE).getEnvironment());
        processor = new DockerSecretProcessor(Supplier::get);
    }

    @Test
    void classpathUri_shouldSetUsernameProperty() {
        setProperty("DATABASE_USER", "classpath:spring.datasource.username");

        processor.postProcessEnvironment(environment, application);

        assertEquals("alice", environment.getProperty("spring.datasource.username"));
    }

    @Test
    void fileUri_shouldSetPasswordProperty() {
        setProperty("DATABASE_PASSWORD", String.format("file:%s/src/test/resources/spring_datasource_password", CWD));

        processor.postProcessEnvironment(environment, application);

        assertEquals("password1234", environment.getProperty("spring.datasource.password"));
    }

    private void setProperty(String key, String value) {
        when(environment.getSystemEnvironment()).thenReturn(Collections.singletonMap(key, value));
        when(environment.getProperty(key)).thenReturn(value);
    }

    static class DockerSecretProcessor extends EnvironmentPropertySecretsProcessor {

        private static final Map<String, String> properties = new HashMap<>();

        static {
            properties.put("spring.datasource.username", "DATABASE_USER");
            properties.put("spring.datasource.password", "DATABASE_PASSWORD");
        }

        public DockerSecretProcessor(DeferredLogFactory logFactory) {
            super(logFactory.getLog(DockerSecretProcessor.class), "dockerSecrets");
        }

        @Override
        protected Map<String, String> getSystemProperties(ConfigurableEnvironment environment) {
            return properties;
        }
    }
}
