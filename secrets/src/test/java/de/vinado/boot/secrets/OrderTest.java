package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderTest {

    // CHECKSTYLE.OFF: LineLength
    @Test
    void testOrder() {
        assertTrue(ConfigDataEnvironmentPostProcessor.ORDER < FilenameSecretsEnvironmentPostProcessor.ORDER);
        assertTrue(FilenameSecretsEnvironmentPostProcessor.ORDER < FilenameConfigDataSecretsEnvironmentPostProcessor.ORDER);
        assertTrue(FilenameConfigDataSecretsEnvironmentPostProcessor.ORDER < EnvironmentConfigDataSecretsEnvironmentPostProcessor.ORDER);
        assertTrue(EnvironmentConfigDataSecretsEnvironmentPostProcessor.ORDER < EnvironmentSecretsPropertyEnvironmentPostProcessor.ORDER);
    }
    // CHECKSTYLE.ON: LineLength
}
