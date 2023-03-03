package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentConfigDataSecretsEnvironmentPostProcessorTest extends AbstractSecretsEnvironmentPostProcessorTest {

    @Override
    SecretsEnvironmentPostProcessor createPostProcessor() {
        return new EnvironmentConfigDataSecretsEnvironmentPostProcessor(Supplier::get);
    }

    @Test
    void applicationConfig_shouldBeProcessed() {
        setProperty("SMTP_USER_FILE", "classpath:spring_mail_host");
        addApplicationProperty("secrets.env.properties.spring.mail.host", "SMTP_USER_FILE");

        postProcessEnvironment();

        assertEquals("localhost", environment.getProperty("spring.mail.host"));
    }
}
