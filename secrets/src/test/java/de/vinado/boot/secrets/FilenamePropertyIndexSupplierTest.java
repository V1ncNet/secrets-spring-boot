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

/**
 * @author Vincent Nadoll
 */
class FilenamePropertyIndexSupplierTest {

    private FilenamePropertyIndexSupplier supplier;

    @BeforeAll
    static void beforeAll() {
        System.setProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY, "${user.dir}/src/test/resources");
    }

    @Test
    void dotSeparator_shouldIndexAllFiles() {
        setUpSupplier(".");

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(7, index.size());

        assertTrue(index.containsKey("secret.empty"));
        assertEquals(fileUriFromClasspath("secret.empty"), index.get("secret.empty"));

        assertTrue(index.containsKey("spring.datasource.password"));
        assertEquals(fileUriFromClasspath("spring.datasource.password"), index.get("spring.datasource.password"));

        assertTrue(index.containsKey("spring.datasource.username"));
        assertEquals(fileUriFromClasspath("spring.datasource.username"), index.get("spring.datasource.username"));

        assertTrue(index.containsKey("spring_mail_host"));
        assertEquals(fileUriFromClasspath("spring_mail_host"), index.get("spring_mail_host"));

        assertTrue(index.containsKey("application-file-sample.properties"));
        assertEquals(fileUriFromClasspath("application-file-sample.properties"), index.get("application-file-sample.properties"));

        assertTrue(index.containsKey("application-env-sample.properties"));
        assertEquals(fileUriFromClasspath("application-env-sample.properties"), index.get("application-env-sample.properties"));
    }

    @Test
    void underscoreSeparators_shouldIndexUnderscoreSeparatedFiles() {
        setUpSupplier("_");

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(2, index.size());

        assertTrue(index.containsKey("spring.datasource.password"));
        assertEquals(fileUriFromClasspath("spring_datasource_password"), index.get("spring.datasource.password"));

        assertTrue(index.containsKey("spring.mail.host"));
        assertEquals(fileUriFromClasspath("spring_mail_host"), index.get("spring.mail.host"));
    }

    @Test
    void illegalSeparator_shouldThrowException() {
        setUpSupplier("/");

        assertThrows(IllegalArgumentException.class, () -> supplier.get());
    }

    private void setUpSupplier(String separator) {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, separator);
        ConfigurableEnvironment environment = new StandardEnvironment();
        this.supplier = new FilenamePropertyIndexSupplier(Supplier::get, environment);
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty(FilenamePropertyIndexSupplier.BASE_DIR_PROPERTY);
        System.clearProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY);
    }
}
