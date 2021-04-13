package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultPropertiesPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Vincent Nadoll
 */
public abstract class AbstractSecretsEnvironmentPostProcessorTest {

    private SpringApplication application;
    protected ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        beforeSetUp();

        application = new SpringApplication(new DefaultResourceLoader(), getClass());
        environment = spy(new StandardEnvironment());
    }

    void beforeSetUp() {
    }

    @Test
    void unconfiguredProperties_shouldNotBeProcessed() {
        postProcessEnvironment();

        assertNull(environment.getProperty("secret.empty"));
        assertNull(environment.getProperty("spring.mail.host"));
    }

    void setProperty(String key, String value) {
        when(environment.getSystemEnvironment()).thenReturn(Collections.singletonMap(key, value));
        when(environment.getProperty(key)).thenReturn(value);
    }

    void addApplicationProperty(String key, String value) {
        Map<String, Object> applicationProperties = Collections.singletonMap(key, value);
        DefaultPropertiesPropertySource.addOrMerge(applicationProperties, environment.getPropertySources());
    }

    void postProcessEnvironment() {
        SecretsEnvironmentPostProcessor processor = createPostProcessor();
        processor.postProcessEnvironment(environment, application);
    }

    abstract SecretsEnvironmentPostProcessor createPostProcessor();
}
