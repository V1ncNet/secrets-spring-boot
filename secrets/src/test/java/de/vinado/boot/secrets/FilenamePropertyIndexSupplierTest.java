package de.vinado.boot.secrets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;
import java.util.function.Supplier;

import static de.vinado.boot.secrets.TestUtils.fileUriFromClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilenamePropertyIndexSupplierTest {

    private Map<String, String> index;

    @BeforeAll
    static void beforeAll() {
        System.setProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY, "${user.dir}/src/test/resources");
    }

    @Test
    void resourceDirectory_shouldBeIndexed_withDotSeparator() {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, ".");

        createIndex();

        assertNotNull(index);
        assertEquals(7, index.size());

        assertEntry("application-env-sample.properties", fileUriFromClasspath("application-env-sample.properties"));
        assertEntry("application-file-sample.properties", fileUriFromClasspath("application-file-sample.properties"));
        assertEntry("secret.empty", fileUriFromClasspath("secret.empty"));
        assertEntry("spring.datasource.password", fileUriFromClasspath("spring.datasource.password"));
        assertEntry("spring.datasource.username", fileUriFromClasspath("spring.datasource.username"));
        assertEntry("spring_datasource_password", fileUriFromClasspath("spring_datasource_password"));
        assertEntry("spring_mail_host", fileUriFromClasspath("spring_mail_host"));
    }

    @Test
    void resourceDirectory_shouldBeIndexed_withUnderscoreSeparator() {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, "_");

        createIndex();

        assertNotNull(index);
        assertEquals(2, index.size());

        assertEntry("spring.datasource.password", fileUriFromClasspath("spring_datasource_password"));
        assertEntry("spring.mail.host", fileUriFromClasspath("spring_mail_host"));
    }

    @Test
    void illegalSeparator_shouldThrowException() {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, "/");
        ConfigurableEnvironment environment = new StandardEnvironment();

        FilenamePropertyIndexSupplier supplier = new FilenamePropertyIndexSupplier(Supplier::get, environment);

        assertThrows(IllegalArgumentException.class, supplier::get);
    }

    private void createIndex() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        FilenamePropertyIndexSupplier supplier = new FilenamePropertyIndexSupplier(Supplier::get, environment);
        index = supplier.get();
    }

    private void assertEntry(String key, String value) {
        assertTrue(index.containsKey(key));
        assertEquals(value, index.get(key));
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY);
        System.clearProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY);
    }
}
