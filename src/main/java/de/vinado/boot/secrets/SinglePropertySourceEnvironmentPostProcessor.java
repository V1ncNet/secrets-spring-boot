package de.vinado.boot.secrets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * An environment post processor that adds a single property source to the index of properties.
 *
 * @author Vincent Nadoll
 */
@Deprecated
public abstract class SinglePropertySourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MapPropertySource propertySource = getPropertySource(environment, application);
        SecretPropertiesPropertySource.merge(propertySource.getSource(), environment.getPropertySources());
    }

    protected abstract MapPropertySource getPropertySource(ConfigurableEnvironment environment, SpringApplication application);
}
