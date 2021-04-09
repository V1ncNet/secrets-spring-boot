package de.vinado.boot.secrets;

import java.net.URI;
import java.util.Optional;

/**
 * An interface for loading the content of a file.
 *
 * @author Vincent Nadoll
 */
@FunctionalInterface
public interface SecretResolver {

    /**
     * Loads the content from the given location.
     *
     * @param location location from which the content is loaded
     * @return secret
     */
    Optional<String> loadContent(String location);

    /**
     * Loads the content from the given URI.
     *
     * @param location location from which the content is loaded
     * @return secret
     */
    default Optional<String> loadContent(URI location) {
        return loadContent(location.toString());
    }
}
