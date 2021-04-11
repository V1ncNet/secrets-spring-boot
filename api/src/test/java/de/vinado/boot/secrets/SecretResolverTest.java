package de.vinado.boot.secrets;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vincent Nadoll
 */
class SecretResolverTest {

    @Test
    void noopResolver_shouldResolveUri() {
        SecretResolver resolver = Optional::of;

        URI location = URI.create("file:/run/secrets/foo");
        Optional<String> content = resolver.loadContent(location);

        assertTrue(content.isPresent());
        assertEquals("file:/run/secrets/foo", content.get());
    }
}
