package de.vinado.boot.secrets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vincent Nadoll
 */
class SecretPropertiesPropertySourceTest {

    private MutablePropertySources sources;
    private Map<String, Object> source;

    @BeforeEach
    void setUp() {
        StandardEnvironment environment = new StandardEnvironment();
        sources = environment.getPropertySources();

        source = Collections.singletonMap("foo", "bar");
    }

    @Test
    void emptySource_shouldNotMerge() {
        HashMap<String, Object> source = new HashMap<>();

        SecretPropertiesPropertySource.merge(source, sources);

        assertFalse(sources.contains(SecretPropertiesPropertySource.NAME));
    }

    @Test
    void sources_shouldContainSecretProperties() {
        assertProperty("foo", "bar");
    }

    @Test
    void property_shouldOverrideStandardEnvironmentProperty() {
        System.setProperty("foo", "baz");

        assertProperty("foo", "bar");
    }

    @Test
    void addThenMerge_shouldOverrideProperty() {
        Map<String, Object> source = Collections.singletonMap("foo", "baz");

        SecretPropertiesPropertySource.merge(source, sources);

        assertProperty("foo", "bar");
    }

    private void assertProperty(String key, String value) {
        SecretPropertiesPropertySource.merge(this.source, sources);
        PropertySource<?> propertySource = sources.get(SecretPropertiesPropertySource.NAME);

        assertNotNull(propertySource);
        assertTrue(propertySource.containsProperty(key));
        assertEquals(value, propertySource.getProperty(key));
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty("foo");
    }
}
