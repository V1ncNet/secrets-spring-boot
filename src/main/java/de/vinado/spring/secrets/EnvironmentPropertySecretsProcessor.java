package de.vinado.spring.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A processor that resolves environment variables and loads the file content from its value.
 *
 * @author Vincent Nadoll
 */
public abstract class EnvironmentPropertySecretsProcessor implements EnvironmentPostProcessor {

    private final Log log;

    public EnvironmentPropertySecretsProcessor(DeferredLogFactory logFactory,
                                               Class<? extends EnvironmentPropertySecretsProcessor> clazz) {
        this.log = logFactory.getLog(clazz);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.trace("Examine for secret related environment variables");

        ResourceLoader resourceLoader = getResourceLoader(application);
        Map<String, Object> resolved = resolveSecretResources(environment, resourceLoader);

        environment.getPropertySources()
            .addAfter(getRelativePropertySourceName(),
                new MapPropertySource(getPropertySourceName(), resolved));
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

    /**
     * @param environment must not be {@code null}
     * @return index of system properties (key) and environment variables which contains the secret's file-URI (value).
     */
    protected abstract Map<String, String> getSystemProperties(ConfigurableEnvironment environment);

    private Optional<String> resolve(String environmentVariable, ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        return Optional.of(environmentVariable)
            .map(environment::getProperty)
            .flatMap(loadResource(resourceLoader));
    }

    private Function<String, Optional<String>> loadResource(ResourceLoader resourceLoader) {
        return location -> loadResource(location, resourceLoader);
    }

    protected Optional<String> loadResource(String location, ResourceLoader resourceLoader) {
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

    protected String getRelativePropertySourceName() {
        return StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
    }

    protected abstract String getPropertySourceName();
}
