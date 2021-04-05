package de.vinado.spring.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A processor that resolves every environment variable with a <em>_FILE</em> suffix. If the variable value contains a
 * file URI, the content of this file is loaded. The variable name is used to override the system property. The name is
 * converted to lower case, underscores are replaced by periods and <em>_FILE</em> suffix will be removed. The secret's
 * value will replace an existing property value with the same name.
 * <p>
 * The variable <em>SPRING_DATASOURCE_PASSWORD_FILE</em> will be converted to <em>spring.datasource.password</em>. Other
 * special characters won't be altered.
 *
 * @author Vincent Nadoll
 */
public class EnvironmentPropertySecretsProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String PROPERTY_SOURCE_NAME = "environmentPropertySecrets";

    private final Log log;

    public EnvironmentPropertySecretsProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(getClass());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.trace("Examine for secret related environment variables");

        ResourceLoader resourceLoader = getResourceLoader(application);
        Map<String, Object> resolved = resolveSecretResources(environment, resourceLoader);

        environment.getPropertySources()
            .addAfter(FilenameSecretsProcessor.PROPERTY_SOURCE_NAME,
                new MapPropertySource(PROPERTY_SOURCE_NAME, resolved));
    }

    private ResourceLoader getResourceLoader(SpringApplication application) {
        return null == application.getResourceLoader()
            ? new DefaultResourceLoader()
            : application.getResourceLoader();
    }

    private Map<String, Object> resolveSecretResources(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        Map<String, Object> source = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : getSystemProperties(environment).entrySet()) {
            String systemProperty = entry.getKey();
            String environmentVariable = entry.getValue();
            resolve(environmentVariable, environment, resourceLoader)
                .ifPresent(add(systemProperty, source));
        }

        return source;
    }

    private Map<String, String> getSystemProperties(ConfigurableEnvironment environment) {
        return environment.getSystemEnvironment().keySet().stream()
            .filter(endsWith("_FILE"))
            .collect(Collectors.toMap(this::convertToPropertyName, Function.identity()));
    }

    private static Predicate<String> endsWith(String suffix) {
        return key -> key.endsWith(suffix);
    }

    private String convertToPropertyName(String fileEnvironmentVariableName) {
        String deSuffixed = fileEnvironmentVariableName.substring(0, fileEnvironmentVariableName.length() - 5);
        String property = deSuffixed.replace("_", ".");
        return property.toLowerCase(Locale.US);
    }

    private Optional<String> resolve(String environmentVariable, ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        return Optional.of(environmentVariable)
            .map(environment::getProperty)
            .flatMap(loadResource(resourceLoader));
    }

    private Function<String, Optional<String>> loadResource(ResourceLoader resourceLoader) {
        return location -> loadResource(location, resourceLoader);
    }

    private Optional<String> loadResource(String location, ResourceLoader resourceLoader) {
        log.trace(String.format("Reading secret from %s", location));
        return Optional.of(location)
            .filter(StringUtils::hasText)
            .map(resourceLoader::getResource)
            .filter(Resource::exists)
            .map(this::readContent)
            .filter(StringUtils::hasText);
    }

    private String readContent(Resource resource) {
        try (InputStream stream = resource.getInputStream()) {
            return StreamUtils.copyToString(stream, Charset.defaultCharset()).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Consumer<String> add(String systemProperty, Map<String, Object> source) {
        return secretValue -> source.put(systemProperty, secretValue);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
