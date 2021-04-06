package de.vinado.spring.secrets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
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
            properties.put("secret.empty", String.format("%s/src/test/resources/secret.empty", CWD));
            properties.put("spring.datasource.username", String.format("%s/src/test/resources/spring.datasource.username", CWD));
            properties.put("spring.datasource.password", String.format("%s/src/test/resources/spring_datasource_password", CWD));
        }

        public DefaultSecretsProcessor(DeferredLogFactory logFactory) {
            super(logFactory.getLog(DefaultSecretsProcessor.class), "defaultSecrets");
        }

        @Override
        protected Map<String, Object> resolveAll(ConfigurableEnvironment environment) {
            Map<String, Object> source = new HashMap<>();

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                String location = entry.getValue();
                resolve(location).ifPresent(add(propertyName, source));
            }

            return source;
        }

        private Optional<Object> resolve(String location) {
            return Optional.ofNullable(getFileContent(Paths.get(location)));
        }

        private static Consumer<Object> add(String systemProperty, Map<String, Object> source) {
            return secretValue -> source.put(systemProperty, secretValue);
        }
    }
}
