package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A processor that resolves secret files. It's value will replace an existing property value with the same name.
 *
 * @author Vincent Nadoll
 */
public abstract class SecretsProcessor implements EnvironmentPostProcessor {

    protected final Log log;

    public SecretsProcessor(Log log) {
        this.log = log;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MapPropertySource propertySource = getPropertySource(environment, application);
        SecretPropertiesPropertySource.merge(propertySource.getSource(), environment.getPropertySources());
    }

    protected MapPropertySource getPropertySource(ConfigurableEnvironment environment, SpringApplication application) {
        ResourceLoader resourceLoader = getResourceLoader(application);
        return new MapPropertySource(SecretPropertiesPropertySource.NAME, resolveSecretResources(environment, resourceLoader));
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
            getResolver(environment, resourceLoader).loadContent(location)
                .ifPresent(doAndLog(add(propertyName, source), log::info, String.format("Use secret's value to set %s", propertyName)));
        }

        return source;
    }

    protected abstract Map<String, String> getSystemProperties(ConfigurableEnvironment environment);

    /**
     * Creates a new instance of {@link SecretResolver}.
     *
     * @param environment    the environment this post-processor runs in
     * @param resourceLoader
     * @return new {@link SecretResolver}
     */
    protected SecretResolver getResolver(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        return new DefaultSecretResolver(resourceLoader);
    }

    public static Consumer<Object> doAndLog(Consumer<Object> consumer, Consumer<Object> level, String format, Object... arguments) {
        return consumer.andThen(p -> level.accept(String.format(format, arguments)));
    }

    private Consumer<Object> add(String systemProperty, Map<String, Object> source) {
        return secretValue -> source.put(systemProperty, secretValue);
    }
}
