package de.vinado.boot.secrets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * An environment post processor that adds a single to the index of properties. The position at which the property is
 * added may be altered by the {@link #adder(PropertySource)}-method.
 *
 * @author Vincent Nadoll
 */
public abstract class SinglePropertySourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Optional.of(environment.getPropertySources())
            .map(peek(adder(getPropertySource(environment, application))));
    }

    private static UnaryOperator<MutablePropertySources> peek(Consumer<MutablePropertySources> action) {
        return t -> {
            action.accept(t);
            return t;
        };
    }

    protected Consumer<MutablePropertySources> adder(PropertySource<?> propertySource) {
        return sources -> sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource);
    }

    protected abstract MapPropertySource getPropertySource(ConfigurableEnvironment environment, SpringApplication application);
}
