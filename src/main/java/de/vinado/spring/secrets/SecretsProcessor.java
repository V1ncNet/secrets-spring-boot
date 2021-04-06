package de.vinado.spring.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.vinado.spring.secrets.Functions.log;

/**
 * A processor that resolves secret files. It's value will replace an existing property value with the same name.
 *
 * @author Vincent Nadoll
 */
public abstract class SecretsProcessor extends SinglePropertySourceEnvironmentPostProcessor {

    private final Log log;
    private final String propertySourceName;

    public SecretsProcessor(Log log, String propertySourceName) {
        this(log, propertySourceName, StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
    }

    public SecretsProcessor(Log log, String propertySourceName, String relativePropertySourceName) {
        super(relativePropertySourceName);
        this.log = log;
        this.propertySourceName = propertySourceName;
    }

    @Override
    protected MapPropertySource getPropertySource(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> source = resolveAll(environment).entrySet().stream()
            .filter(this::hasNonEmptyValue)
            .peek(log(log::info, entry -> String.format("Use secret's value to set %s", entry.getKey())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new MapPropertySource(propertySourceName, source);
    }

    protected abstract Map<String, Object> resolveAll(ConfigurableEnvironment environment);

    protected String convertToPropertyName(Path filename) {
        File file = filename.toFile();
        String name = file.getName();
        String property = name.replace("_", ".");
        return property.toLowerCase(Locale.US);
    }

    protected Object getFileContent(Path path) {
        log.trace(String.format("Reading from secret %s", path));
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
}
