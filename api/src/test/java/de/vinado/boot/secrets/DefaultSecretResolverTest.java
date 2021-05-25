package de.vinado.boot.secrets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vincent Nadoll
 */
class DefaultSecretResolverTest {

    private ResourceLoader resourceLoader;
    private DefaultSecretResolver resolver;

    @BeforeEach
    void setUp() {
        resourceLoader = new DefaultResourceLoader();
        resolver = new DefaultSecretResolver(resourceLoader);
    }

    @Test
    void initializingNullArguments_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultSecretResolver(null));
    }

    @Test
    void classpathResource_shouldLoadResourceContent() {
        Optional<String> resolved = resolver.loadContent("classpath:spring_mail_host");

        assertEquals("localhost", resolved);
    }

    @Test
    void fileResource_shouldLoadResourceContent() {
        Optional<String> resolved = resolver.loadContent(fromFile("spring_mail_host"));

        assertEquals("localhost", resolved);
    }

    @Test
    void nullLocation_shouldNotResolve() {
        Optional<String> resolved = resolver.loadContent((String) null);

        assertFalse(resolved.isPresent());
    }

    @Test
    void text_shouldNotResolve() {
        Optional<String> resolved = resolver.loadContent(UUID.randomUUID().toString());

        assertFalse(resolved.isPresent());
    }

    @Test
    void emptyLocation_shouldNotResolve() {
        Optional<String> resolved = resolver.loadContent("");

        assertFalse(resolved.isPresent());
    }

    @Test
    void emptyResource_shouldNotResolve() {
        Optional<String> resolved = resolver.loadContent("classpath:secret.empty");

        assertFalse(resolved.isPresent());
    }

    @Test
    void nonExistingClasspathResource_shouldNotResolve() {
        Optional<String> resolved = resolver.loadContent("classpath:foo");

        assertFalse(resolved.isPresent());
    }

    @Test
    void nonExistingFileResource_shouldNotResolve() {
        Optional<String> resolved = resolver.loadContent(fromFile("foo"));

        assertFalse(resolved.isPresent());
    }

    private static URI fromFile(String name) {
        String pathname = String.format("%s/src/test/resources/%s", System.getProperty("user.dir"), name);
        File file = new File(pathname);
        return file.toURI();
    }

    private static void assertEquals(String expected, Optional<String> actual) {
        assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());
    }
}
