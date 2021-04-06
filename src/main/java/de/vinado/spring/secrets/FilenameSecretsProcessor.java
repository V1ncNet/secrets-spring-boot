package de.vinado.spring.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
public class FilenameSecretsProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String PROPERTY_SOURCE_NAME = "filenameSecrets";
    public static final String BASE_DIR_PROPERTY = "secrets.base-dir";

    private final Log log;

    public FilenameSecretsProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(getClass());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String baseDir = environment.getProperty(BASE_DIR_PROPERTY, "/run/secrets");
        log.trace(String.format("Examine for secrets related in %s", baseDir));

        Map<String, Object> resolved = resolveAll(baseDir).entrySet().stream()
            .filter(this::hasNonEmptyValue)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        environment.getPropertySources()
            .addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MapPropertySource(PROPERTY_SOURCE_NAME, resolved));
    }

    private Map<String, Object> resolveAll(String baseDir) {
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

    private String convertToPropertyName(Path filename) {
        File file = filename.toFile();
        String name = file.getName();
        String property = name.replace("_", ".");
        return property.toLowerCase(Locale.US);
    }

    private Object getFileContent(Path path) {
        try (Stream<String> lines = Files.lines(path, Charset.defaultCharset())) {
            StringBuilder builder = new StringBuilder();
            lines.forEach(builder::append);
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasNonEmptyValue(Map.Entry<String, Object> entry) {
        Object value = entry.getValue();
        if (value instanceof String) {
            return StringUtils.hasText((String) value);
        }

        return true;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 11;
    }
}
