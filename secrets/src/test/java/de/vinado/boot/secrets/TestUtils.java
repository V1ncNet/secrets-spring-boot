package de.vinado.boot.secrets;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Vincent Nadoll
 */
final class TestUtils {

    public static String fileUriFromClasspath(String filename) {
        String pathname = String.format("%s/src/test/resources/%s", System.getProperty("user.dir"), filename);
        Path path = Paths.get(pathname);
        return path.toUri().toString();
    }
}
