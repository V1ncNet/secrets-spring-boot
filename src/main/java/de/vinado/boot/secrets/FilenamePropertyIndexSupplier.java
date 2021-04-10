package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.log.LogMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A supplier for creating a property index over filenames in a configurable directory. The property name is based on
 * this filename. The file 'spring.datasource.username' will add the 'spring.datasource.username' property key and its
 * URI as value of the index. Filenames in snake case will work as well (spring_datasource_username). Other characters
 * won't be altered.
 * <p>
 * The supplier will fall back to <em>/run/secrets</em>, Docker's default secretes path, in case
 * <em>secrets.file.base-dir</em> is not set.
 *
 * @author Vincent Nadoll
 */
public class FilenamePropertyIndexSupplier implements PropertyIndexSupplier {

    public static final String BASE_DIR_PROPERTY = "secrets.file.base-dir";
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
        return Optional.of(baseDir)
            .map(Paths::get)
            .filter(Files::isDirectory)
            .map(this::listFiles)
            .orElse(Stream.empty())
            .collect(Collectors.toMap(this::convertToPropertyName, this::toUri, this::firstComeFirstServe));
    }

    private Stream<Path> listFiles(Path path) {
        try {
            return Files.list(path).filter(Files::isRegularFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertToPropertyName(Path filename) {
        File file = filename.toFile();
        String name = file.getName();
        return name.toLowerCase(Locale.US);
    }

    private String toUri(Path path) {
        return path.toUri().toString();
    }

    private String firstComeFirstServe(String existing, String replacement) {
        log.warn(LogMessage.format("Encountered duplicates. Secret in %s will be ignored. Reading content of %s instead.",
            replacement, existing));
        return existing;
    }
}
