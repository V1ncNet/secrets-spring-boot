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

public abstract class SinglePropertySourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final String relativePropertySourceName;

    public SinglePropertySourceEnvironmentPostProcessor() {
        this(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
    }

    public SinglePropertySourceEnvironmentPostProcessor(String relativePropertySourceName) {
        this.relativePropertySourceName = relativePropertySourceName;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Optional.of(environment.getPropertySources())
            .map(peek(adder(relativePropertySourceName, getPropertySource(environment, application))));
    }

    private static UnaryOperator<MutablePropertySources> peek(Consumer<MutablePropertySources> action) {
        return t -> {
            action.accept(t);
            return t;
        };
    }

    protected Consumer<MutablePropertySources> adder(String relativePropertySourceName, PropertySource<?> propertySource) {
        return sources -> sources.addAfter(relativePropertySourceName, propertySource);
    }

    protected abstract MapPropertySource getPropertySource(ConfigurableEnvironment environment, SpringApplication application);
}
