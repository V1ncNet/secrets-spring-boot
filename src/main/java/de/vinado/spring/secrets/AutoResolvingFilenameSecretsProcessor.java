package de.vinado.spring.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class AutoResolvingFilenameSecretsProcessor extends SecretsProcessor implements Ordered {

    public static final String PROPERTY_SOURCE_NAME = "autoResolvingFilenameSecrets";
    public static final String BASE_DIR_PROPERTY = "secrets.base-dir";
    private static final String DEFAULT_BASE_DIR_PROPERTY = "/run/secrets";

    public AutoResolvingFilenameSecretsProcessor(DeferredLogFactory logFactory) {
        super(logFactory.getLog(AutoResolvingFilenameSecretsProcessor.class), PROPERTY_SOURCE_NAME);
    }

    @Override
    protected Map<String, Object> resolveAll(ConfigurableEnvironment environment) {
        String baseDir = environment.getProperty(BASE_DIR_PROPERTY, DEFAULT_BASE_DIR_PROPERTY);
        return Optional.of(baseDir)
            .map(Paths::get)
            .filter(Files::isDirectory)
            .map(this::listFiles)
            .orElse(Stream.empty())
            .collect(Collectors.toMap(this::convertToPropertyName, this::getFileContent));
    }

    private Stream<Path> listFiles(Path path) {
        try {
            return Files.list(path).filter(Files::isRegularFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 11;
    }
}
