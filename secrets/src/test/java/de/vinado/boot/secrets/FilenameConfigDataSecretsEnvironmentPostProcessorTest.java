package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilenameConfigDataSecretsEnvironmentPostProcessorTest extends AbstractSecretsEnvironmentPostProcessorTest {

    @Override
    SecretsEnvironmentPostProcessor createPostProcessor() {
        return new FilenameConfigDataSecretsEnvironmentPostProcessor(Supplier::get);
    }

    @Test
    void applicationConfig_shouldBeProcessed() {
        addApplicationProperty("secrets.file.properties.spring.mail.host", "classpath:spring_mail_host");

        postProcessEnvironment();

        assertEquals("localhost", environment.getProperty("spring.mail.host"));
    }
}
