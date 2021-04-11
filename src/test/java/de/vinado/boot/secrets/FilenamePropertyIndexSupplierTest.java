package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertEquals(fromFile("secret.empty"), index.get("secret.empty"));

        assertTrue(index.containsKey("spring.datasource.password"));
        assertEquals(fromFile("spring.datasource.password"), index.get("spring.datasource.password"));

        assertTrue(index.containsKey("spring.datasource.username"));
        assertEquals(fromFile("spring.datasource.username"), index.get("spring.datasource.username"));

        assertTrue(index.containsKey("spring_mail_host"));
        assertEquals(fromFile("spring_mail_host"), index.get("spring_mail_host"));

        assertTrue(index.containsKey("application-file-sample.properties"));
        assertEquals(fromFile("application-file-sample.properties"), index.get("application-file-sample.properties"));

        assertTrue(index.containsKey("application-env-sample.properties"));
        assertEquals(fromFile("application-env-sample.properties"), index.get("application-env-sample.properties"));
    }

    @Test
    void underscoreSeparators_shouldIndexUnderscoreSeparatedFiles() {
        setUpSupplier("_");

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(2, index.size());

        assertTrue(index.containsKey("spring.datasource.password"));
        assertEquals(fromFile("spring_datasource_password"), index.get("spring.datasource.password"));

        assertTrue(index.containsKey("spring.mail.host"));
        assertEquals(fromFile("spring_mail_host"), index.get("spring.mail.host"));
    }

    private static String fromFile(String name) {
        String pathname = String.format("%s/src/test/resources/%s", System.getProperty("user.dir"), name);
        Path path = Paths.get(pathname);
        return path.toUri().toString();
    }

    private void setUpSupplier(String separator) {
        System.setProperty(FilenamePropertyIndexSupplier.SEPARATOR_PROPERTY, separator);
        ConfigurableEnvironment environment = new StandardEnvironment();
        this.supplier = new FilenamePropertyIndexSupplier(Supplier::get, environment);
    }
}
