package de.vinado.boot.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static de.vinado.boot.secrets.TestUtils.fileUriFromClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigDataPropertyIndexSupplierTest {

    private ConfigurableEnvironment environment;
    private ConfigDataPropertyIndexSupplier supplier;

    @BeforeEach
    void setUp() {
        environment = new StandardEnvironment();
    }

    @Test
    void otherConfigData_shouldNotBeIndexed() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("spring.mail.host", "foo");
        applicationProperties.put("spring.datasource.username", "bob");
        setUpSupplier(applicationProperties);

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(0, index.size());
    }

    @Test
    void malformedConfigData_shouldNotBeIndexed() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("secrets.file.properties", "classpath:spring_mail_host");
        setUpSupplier(applicationProperties);

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(0, index.size());
    }

    @Test
    void prefixedConfigData_shouldBeIndexed() {
        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("secrets.file.properties.spring.mail.host", "classpath:spring_mail_host");
        applicationProperties.put("secrets.file.properties.secret.empty", fileUriFromClasspath("secret.empty"));
        setUpSupplier(applicationProperties);

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(2, index.size());

        assertTrue(index.containsKey("spring.mail.host"));
        assertEquals("classpath:spring_mail_host", index.get("spring.mail.host"));

        assertTrue(index.containsKey("secret.empty"));
        assertEquals(fileUriFromClasspath("secret.empty"), index.get("secret.empty"));
    }

    private void setUpSupplier(Map<String, Object> applicationProperties) {
        DefaultPropertiesPropertySource.addOrMerge(applicationProperties, environment.getPropertySources());

        supplier = new ConfigDataPropertyIndexSupplier(Supplier::get, environment, "file");
    }
}
