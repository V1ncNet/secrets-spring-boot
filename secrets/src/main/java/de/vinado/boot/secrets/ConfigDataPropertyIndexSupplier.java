package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.vinado.boot.secrets.Utils.startsWith;
import static de.vinado.boot.secrets.Utils.substring;
import static de.vinado.boot.secrets.Utils.testAndLogFailure;

/**
 * <p>
 * A supplier implementation for creating a property index over a map of application configuration properties from
 * application.properties (or similar). These properties are prefixed with <em>secrets.file.properties</em>.
 * </p><p>
 * <em>secrets.&lt;infix&gt;.properties.spring.mail.host</em> → <em>spring.mail.host</em>
 * </p>
 *
 * @author Vincent Nadoll
 */
public class ConfigDataPropertyIndexSupplier implements PropertyIndexSupplier {

    private final Log log;
    private final ConfigurableEnvironment environment;
    private final String prefix;

    public ConfigDataPropertyIndexSupplier(DeferredLogFactory logFactory, ConfigurableEnvironment environment,
                                           String propertyInfix) {
        Assert.notNull(logFactory, "Log factory must not be null");
        Assert.notNull(environment, "Environment must not be null");
        Assert.hasText(propertyInfix, "Property infix must not be null or empty");

        this.log = logFactory.getLog(getClass());
        this.environment = environment;
        this.prefix = "secrets." + propertyInfix + ".properties";
    }

    @Override
    public Map<String, String> get() {
        MutablePropertySources sources = environment.getPropertySources();
        PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(sources);
        return sources.stream()
            .filter(MapPropertySource.class::isInstance)
            .map(MapPropertySource.class::cast)
            .map(PropertySource::getSource)
            .map(Map::keySet)
            .flatMap(Set::stream)
            .filter(startsWith(prefix))
            .filter(testAndLogFailure(this::isValid, log::warn, "Property [%s] is too short to assign.",
                Function.identity()))
            .collect(Collectors.toMap(substring(prefix.length() + 1), resolver::getProperty));
    }

    private boolean isValid(String property) {
        return property.length() >= prefix.length() + 1;
    }
}
