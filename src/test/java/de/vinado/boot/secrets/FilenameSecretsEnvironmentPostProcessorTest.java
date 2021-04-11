package de.vinado.boot.secrets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * @author Vincent Nadoll
 */
class FilenameSecretsEnvironmentPostProcessorTest {

    private static SpringApplication application;

    private ConfigurableEnvironment environment;
    private FilenameSecretsEnvironmentPostProcessor processor;

    @BeforeAll
    static void beforeAll() {
        System.setProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY, "${user.dir}/src/test/resources");
        application = mock(SpringApplication.class);
    }

    @BeforeEach
    void setUp() {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, ".");

        environment = new StandardEnvironment();
        processor = new FilenameSecretsEnvironmentPostProcessor(Supplier::get);
    }

    @Test
    void dotSeparatedSecret_shouldSetUsernameProperty() {
        processor.postProcessEnvironment(environment, application);

        assertFileExist("spring.datasource.username");
        assertFileNotExist("spring_datasource_username");
        assertEquals("alice", environment.getProperty("spring.datasource.username"));
    }

    @Test
    void processor_shouldPriorDotSeparatedSecretAndSetPasswordProperty() {
        processor.postProcessEnvironment(environment, application);

        assertFileExist("spring.datasource.password");
        assertFileExist("spring_datasource_password");
        assertEquals("1234password", environment.getProperty("spring.datasource.password"));
    }

    private void assertFileExist(String name) {
        assertFilePresence(name, Assertions::assertTrue);
    }

    @Test
    void underscoreSeparatedSecretFile_shouldSetSmtpHost() {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, "_");

        processor.postProcessEnvironment(environment, application);

        assertFileNotExist("spring.mail.host");
        assertFileExist("spring_mail_host");
        assertEquals("localhost", environment.getProperty("spring.mail.host"));
    }

    private void assertFileNotExist(String name) {
        assertFilePresence(name, Assertions::assertFalse);
    }

    private void assertFilePresence(String name, Consumer<Boolean> exist) {
        String pathname = String.format("%s/src/test/resources/%s", System.getProperty("user.dir"), name);
        File file = new File(pathname);
        exist.accept(file.exists());
    }

    @Test
    void emptySecretFile_shouldNotSetProperty() {
        processor.postProcessEnvironment(environment, application);

        assertNull(environment.getProperty("secret.empty"));
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY);
        System.clearProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY);
    }
}
