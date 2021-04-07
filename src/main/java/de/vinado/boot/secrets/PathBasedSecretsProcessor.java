package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

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
 * A processor that resolves all secrets from a configurable directory. The property name is based on the secret's
 * filename. It's value will replace an existing property value with the same name.
 * <p>
 * The file 'spring.datasource.username' will override the 'spring.datasource.username' property. Filenames in snake
 * case will work as well (spring_datasource_username). Other characters won't be altered.
 * <p>
 * If <em>secrets.base-dir</em> will fall back to <em>/run/secrets</em>, Docker's default secretes path, in case the
 * property is not set.
 *
 * @author Vincent Nadoll
 */
public final class PathBasedSecretsProcessor extends SecretsProcessor implements Ordered {

    public static final String PROPERTY_SOURCE_NAME = "pathBasedSecrets";
    public static final String BASE_DIR_PROPERTY = "secrets.base-dir";
    private static final String DEFAULT_BASE_DIR_PROPERTY = "/run/secrets";

    public PathBasedSecretsProcessor(DeferredLogFactory logFactory) {
        super(logFactory.getLog(PathBasedSecretsProcessor.class), PROPERTY_SOURCE_NAME);
    }

    @Override
    protected Map<String, String> getSystemProperties(ConfigurableEnvironment environment) {
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

    protected String convertToPropertyName(Path filename) {
        File file = filename.toFile();
        String name = file.getName();
        String property = name.replace("_", ".");
        return property.toLowerCase(Locale.US);
    }

    private String toUri(Path path) {
        return path.toUri().toString();
    }

    private String firstComeFirstServe(String existing, String replacement) {
        log.warn(String.format("Encountered duplicates. Secret in %s will be ignored. Reading content of %s instead.", replacement, existing));
        return existing;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 11;
    }
}
