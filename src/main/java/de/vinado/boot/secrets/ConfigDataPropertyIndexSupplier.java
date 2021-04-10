package de.vinado.boot.secrets;

import lombok.NonNull;
import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.log.LogMessage;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * A supplier implementation for creating a property index over a map of application configuration properties from
 * application.properties (or similar). These properties are prefixed with <em>secrets.file.properties</em>.
 * <p>
 * <em>secrets.&lt;infix&gt;.properties.spring.mail.host</em> → <em>spring.mail.host</em>
 *
 * @author Vincent Nadoll
 */
public class ConfigDataPropertyIndexSupplier implements PropertyIndexSupplier {

    private static final String PROPERTY_PREFIX_TEMPLATE = "secrets.%s.properties";

    private final Log log;
    private final ConfigurableEnvironment environment;
    private final String prefix;

    public ConfigDataPropertyIndexSupplier(@NonNull DeferredLogFactory logFactory,
                                           @NonNull ConfigurableEnvironment environment,
                                           @NonNull String propertyInfix) {
        this.log = logFactory.getLog(getClass());
        this.environment = environment;
        this.prefix = String.format(PROPERTY_PREFIX_TEMPLATE, propertyInfix);
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
            .filter(this::isValid)
            .collect(Collectors.toMap(substring(prefix.length() + 1), resolver::getProperty));
    }

    private static Predicate<String> startsWith(String prefix) {
        return key -> key.startsWith(prefix);
    }

    private boolean isValid(String property) {
        boolean valid = property.length() >= prefix.length() + 1;
        if (!valid) {
            log.warn(LogMessage.format("Property [%s] is too short to assign.", property));
        }
        return valid;
    }

    private static UnaryOperator<String> substring(int beginIndex) {
        return str -> str.substring(beginIndex);
    }
}
