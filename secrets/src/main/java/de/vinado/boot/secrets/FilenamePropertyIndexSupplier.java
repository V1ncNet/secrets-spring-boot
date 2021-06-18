package de.vinado.boot.secrets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.vinado.boot.secrets.Utils.testAndLogFailure;

/**
 * <p>
 * A supplier for creating a property index over filenames in a configurable directory. The property name is based on
 * this filename. The file 'spring.datasource.username' will add the 'spring.datasource.username' property key and its
 * URI as value of the index. Filenames in snake case will work as well (spring_datasource_username). Other characters
 * won't be altered.
 * </p><p>
 * The supplier will fall back to <em>/run/secrets</em>, Docker's default secretes path, in case
 * <em>secrets.file.base-dir</em> is not set.
 * </p>
 *
 * @author Vincent Nadoll
 */
public class FilenamePropertyIndexSupplier implements PropertyIndexSupplier {

    public static final String BASE_DIR_PROPERTY = "secrets.file.base-dir";
    public static final String SEPARATOR_PROPERTY = "secrets.file.separator";
    public static final Separator DEFAULT_SEPARATOR = Separator.DOT;
    private static final String DEFAULT_BASE_DIR_PROPERTY = "/run/secrets";

    private final Log log;
    private final ConfigurableEnvironment environment;

    public FilenamePropertyIndexSupplier(DeferredLogFactory logFactory, ConfigurableEnvironment environment) {
        this.log = logFactory.getLog(getClass());
        this.environment = environment;
    }

    @Override
    public Map<String, String> get() {
        String baseDir = environment.getProperty(BASE_DIR_PROPERTY, DEFAULT_BASE_DIR_PROPERTY);
        Separator separator = getSeparator();
        return Optional.of(baseDir)
            .map(Paths::get)
            .filter(Files::isDirectory)
            .map(this::listFiles)
            .orElse(Stream.empty())
            .filter(testAndLogFailure(this::isAllowed, log::warn,
                "Skipping ambiguous file %s, because of separator '%s'", Path::toAbsolutePath, path -> separator))
            .collect(Collectors.toMap(this::convertToPropertyName, this::toUri));
    }

    private Separator getSeparator() {
        char property = environment.getProperty(SEPARATOR_PROPERTY, Character.class, DEFAULT_SEPARATOR.getCharacter());
        return Separator.of(property);
    }

    private Stream<Path> listFiles(Path path) {
        try {
            return Files.list(path).filter(Files::isRegularFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAllowed(Path path) {
        return isDefaultSeparator() || !containsDefaultSeparator(path);
    }

    private boolean isDefaultSeparator() {
        return Objects.equals(getSeparator(), DEFAULT_SEPARATOR);
    }

    private boolean containsDefaultSeparator(Path path) {
        String name = getFilename(path);
        return name.lastIndexOf(DEFAULT_SEPARATOR.toString()) > 0;
    }

    private String convertToPropertyName(Path path) {
        Separator separator = getSeparator();
        String name = getFilename(path);
        String property = name.replace(separator.getCharacter(), '.');
        return property.toLowerCase(Locale.US);
    }

    private String getFilename(Path path) {
        File file = path.toFile();
        return file.getName();
    }

    private String toUri(Path path) {
        return path.toUri().toString();
    }

    /**
     * Enumeration of available filename name separators.
     *
     * @author Vincent Nadoll
     */
    @Getter
    @RequiredArgsConstructor
    public enum Separator {
        DOT('.'),
        UNDERSCORE('_'),
        ;

        private final char character;

        public static Separator of(char separator) {
            return Arrays.stream(values())
                .filter(value -> value.character == separator)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown separator char"));
        }

        @Override
        public String toString() {
            return String.valueOf(character);
        }
    }
}
