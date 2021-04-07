package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * @author Vincent Nadoll
 */
class SecretsProcessorTest {

    private static final String CWD = System.getProperty("user.dir");
    private static final ApplicationContextFactory contextFactory = ApplicationContextFactory.DEFAULT;

    private static SpringApplication application;

    private ConfigurableEnvironment environment;
    private DefaultSecretsProcessor processor;

    @BeforeAll
    static void beforeAll() {
        application = mock(SpringApplication.class);
    }

    @BeforeEach
    void setUp() {
        environment = contextFactory.create(WebApplicationType.NONE).getEnvironment();
        processor = new DefaultSecretsProcessor(Supplier::get);
    }

    @Test
    void dotSeparatedSecret_shouldSetUsernameProperty() {
        processor.postProcessEnvironment(environment, application);

        assertEquals("alice", environment.getProperty("spring.datasource.username"));
    }

    @Test
    void underscoreSeparatedSecretFile_shouldSetPasswordProperty() {
        processor.postProcessEnvironment(environment, application);

        assertEquals("password1234", environment.getProperty("spring.datasource.password"));
    }

    @Test
    void emptySecretFile_shouldNotSetProperty() {
        processor.postProcessEnvironment(environment, application);

        assertNull(environment.getProperty("secret.empty"));
    }

    static class DefaultSecretsProcessor extends SecretsProcessor {

        private static final Map<String, String> properties = new HashMap<>();

        static {
            properties.put("secret.empty", String.format("file:%s/src/test/resources/secret.empty", CWD));
            properties.put("spring.datasource.username", String.format("file:%s/src/test/resources/spring.datasource.username", CWD));
            properties.put("spring.datasource.password", String.format("file:%s/src/test/resources/spring_datasource_password", CWD));
        }

        public DefaultSecretsProcessor(DeferredLogFactory logFactory) {
            super(logFactory.getLog(DefaultSecretsProcessor.class), "defaultSecrets");
        }

        @Override
        protected Map<String, String> getSystemProperties(ConfigurableEnvironment environment) {
            return properties;
        }
    }
}
