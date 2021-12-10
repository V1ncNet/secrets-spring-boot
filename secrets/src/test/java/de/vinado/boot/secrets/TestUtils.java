package de.vinado.boot.secrets;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Vincent Nadoll
 */
final class TestUtils {

    public static String fileUriFromClasspath(String filename) {
        String pathname = System.getProperty("user.dir") + "/src/test/resources/" + filename;
        Path path = Paths.get(pathname);
        return path.toUri().toString();
    }
}
