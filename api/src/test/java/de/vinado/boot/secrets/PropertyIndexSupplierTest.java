package de.vinado.boot.secrets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.StandardEnvironment;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vincent Nadoll
 */
class PropertyIndexSupplierTest {

    @BeforeAll
    static void beforeAll() {
        System.setProperty("SPRING_DATASOURCE_USERNAME", " bob \n");
        System.setProperty("spring.datasource.username", "SPRING_DATASOURCE_USERNAME");
    }

    @Test
    void testSubstitution() {
        PropertyResolver resolver = new StandardEnvironment();

        PropertyIndexSupplier supplier = ((PropertyIndexSupplier) () -> Collections.singletonMap("spring.datasource.username", "SPRING_DATASOURCE_USERNAME"))
            .substituteValues(resolver);

        Map<String, String> index = supplier.get();

        assertNotNull(index);
        assertEquals(1, index.size());
        assertTrue(index.containsKey("spring.datasource.username"));
        assertEquals("bob", index.get("spring.datasource.username"));
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty("SPRING_DATASOURCE_USERNAME");
        System.clearProperty("spring.datasource.username");
    }
}
