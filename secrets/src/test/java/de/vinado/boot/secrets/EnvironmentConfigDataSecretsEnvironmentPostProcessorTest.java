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
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Vincent Nadoll
 */
class EnvironmentConfigDataSecretsEnvironmentPostProcessorTest {

    private SpringApplication application;
    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        application = new SpringApplication(new DefaultResourceLoader(), getClass());
        environment = spy(new StandardEnvironment());
    }

    @Test
    void missingDefaultProperties_shouldNotPostProcessEnvironment() {
        setUpProcessor();

        assertNull(environment.getProperty("secret.empty"));
        assertNull(environment.getProperty("spring.mail.host"));
    }

    @Test
    void applicationConfig_shouldPostProcessEnvironment() {
        setProperty("SMTP_USER_FILE", "classpath:spring_mail_host");
        Map<String, Object> applicationProperties =
            Collections.singletonMap("secrets.env.properties.spring.mail.host", "SMTP_USER_FILE");
        DefaultPropertiesPropertySource.addOrMerge(applicationProperties, environment.getPropertySources());

        setUpProcessor();

        assertNull(environment.getProperty("secret.empty"));
        assertEquals("localhost", environment.getProperty("spring.mail.host"));
    }

    private void setProperty(String key, String value) {
        when(environment.getSystemEnvironment()).thenReturn(Collections.singletonMap(key, value));
        when(environment.getProperty(key)).thenReturn(value);
    }

    private void setUpProcessor() {
        EnvironmentConfigDataSecretsEnvironmentPostProcessor processor =
            new EnvironmentConfigDataSecretsEnvironmentPostProcessor(Supplier::get);
        processor.postProcessEnvironment(environment, application);
    }
}
