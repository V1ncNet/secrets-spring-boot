package de.vinado.boot.secrets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FilenameSecretsEnvironmentPostProcessorTest extends AbstractSecretsEnvironmentPostProcessorTest {

    @Override
    SecretsEnvironmentPostProcessor createPostProcessor() {
        return new FilenameSecretsEnvironmentPostProcessor(Supplier::get);
    }

    @BeforeAll
    static void beforeAll() {
        System.setProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY, "${user.dir}/src/test/resources");
    }

    @Test
    void resourceDirectory_shouldBeProcessed_withDotSeparator() {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, ".");

        postProcessEnvironment();

        assertNotNull(environment.getProperty("application-env-sample.properties"));
        assertNotNull(environment.getProperty("application-file-sample.properties"));
        assertNull(environment.getProperty("secret.empty"));
        assertProperty("1234password", "spring.datasource.password");
        assertProperty("password1234", "spring_datasource_password");
        assertProperty("alice", "spring.datasource.username");
        assertNull(environment.getProperty("spring.mail.host"));
        assertProperty("localhost", "spring_mail_host");
    }

    @Test
    void resourceDirectory_shouldBeProcessed_withUnderscoreSeparator() {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, "_");

        postProcessEnvironment();

        assertNull(environment.getProperty("application-env-sample.properties"));
        assertNull(environment.getProperty("application-file-sample.properties"));
        assertNull(environment.getProperty("secret.empty"));
        assertProperty("password1234", "spring.datasource.password");
        assertNull(environment.getProperty("spring.datasource.username"));
        assertProperty("localhost", "spring.mail.host");
    }

    private void assertProperty(String value, String property) {
        assertEquals(value, environment.getProperty(property));
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY);
        System.clearProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY);
    }
}
