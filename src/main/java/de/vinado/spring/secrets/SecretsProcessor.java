package de.vinado.spring.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static de.vinado.spring.secrets.Functions.doAndLog;

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
        ResourceLoader resourceLoader = getResourceLoader(application);
        return new MapPropertySource(propertySourceName, resolveSecretResources(environment, resourceLoader));
    }

    protected ResourceLoader getResourceLoader(SpringApplication application) {
        return null == application.getResourceLoader()
            ? new DefaultResourceLoader()
            : application.getResourceLoader();
    }

    protected Map<String, Object> resolveSecretResources(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        Map<String, Object> source = new HashMap<>();

        for (Map.Entry<String, String> entry : getSystemProperties(environment).entrySet()) {
            String propertyName = entry.getKey();
            String location = entry.getValue();
            substitute(location, environment)
                .flatMap(resolveWith(resourceLoader))
                .map(this::readContent)
                .filter(StringUtils::hasText)
                .ifPresent(doAndLog(add(propertyName, source), log::info, String.format("Use secret's value to set %s", propertyName)));
        }

        return source;
    }

    protected abstract Map<String, String> getSystemProperties(ConfigurableEnvironment environment);

    protected Optional<String> substitute(String location, ConfigurableEnvironment environment) {
        return Optional.of(location);
    }

    protected Function<String, Optional<Resource>> resolveWith(ResourceLoader resourceLoader) {
        return location -> loadResource(location, resourceLoader);
    }

    protected Optional<Resource> loadResource(String location, ResourceLoader resourceLoader) {
        log.trace(String.format("Reading from secret %s", location));
        return Optional.of(location)
            .filter(StringUtils::hasText)
            .map(resourceLoader::getResource)
            .filter(Resource::exists);
    }

    private String readContent(Resource resource) {
        try (InputStream stream = resource.getInputStream()) {
            return StreamUtils.copyToString(stream, Charset.defaultCharset()).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
