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

/**
 * @author Vincent Nadoll
 */
class FilenameConfigDataSecretsEnvironmentPostProcessorTest {

    private SpringApplication application;
    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        application = new SpringApplication(new DefaultResourceLoader(), getClass());
        environment = new StandardEnvironment();
    }

    @Test
    void missingDefaultProperties_shouldNotPostProcessEnvironment() {
        setUpProcessor();

        assertNull(environment.getProperty("secret.empty"));
        assertNull(environment.getProperty("spring.mail.host"));
    }

    @Test
    void applicationConfig_shouldPostProcessEnvironment() {
        Map<String, Object> applicationProperties =
            Collections.singletonMap("secrets.file.properties.spring.mail.host", "classpath:spring_mail_host");
        DefaultPropertiesPropertySource.addOrMerge(applicationProperties, environment.getPropertySources());

        setUpProcessor();

        assertNull(environment.getProperty("secret.empty"));
        assertEquals("localhost", environment.getProperty("spring.mail.host"));
    }

    private void setUpProcessor() {
        FilenameConfigDataSecretsEnvironmentPostProcessor processor =
            new FilenameConfigDataSecretsEnvironmentPostProcessor(Supplier::get);
        processor.postProcessEnvironment(environment, application);
    }
}
