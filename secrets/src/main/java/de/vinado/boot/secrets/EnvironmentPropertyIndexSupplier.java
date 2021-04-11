package de.vinado.boot.secrets;

import lombok.NonNull;
import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

import static de.vinado.boot.secrets.Utils.endsWith;

/**
 * A supplier for creating a property index over all environment properties ending with the configured suffix. The
 * property's name gets converted to the an applicable system property name and is used to set the index's key.
 * 1. Trimming of suffix
 * 2. Replacement of underscores with periods
 * 3. Conversion to lowercase
 * The value will be the the property name.
 * <p>
 * <em>SPRING_DATASOURCE_PASSWORD_FILE</em> → <em>spring.datasource.password</em>
 * <p>
 * The suffix should contain at least one character. Otherwise every environment property will be present in the index
 * which might be a security risk.
 *
 * @author Vincent Nadoll
 */
public class EnvironmentPropertyIndexSupplier implements PropertyIndexSupplier {

    private final ConfigurableEnvironment environment;
    private final String suffix;

    public EnvironmentPropertyIndexSupplier(@NonNull DeferredLogFactory logFactory,
                                            @NonNull ConfigurableEnvironment environment) {
        this(logFactory, environment, "");
    }

    public EnvironmentPropertyIndexSupplier(@NonNull DeferredLogFactory logFactory,
                                            @NonNull ConfigurableEnvironment environment,
                                            @NonNull String suffix) {
        this.environment = environment;
        this.suffix = suffix;

        Log log = logFactory.getLog(getClass());
        Utils.<String>testAndLogFailure(StringUtils::hasText, log::warn, "Suffix doesn't contain any characters." +
            "Thus all environment properties will be indexed which might be a security risk.").test(suffix);
    }

    @Override
    public Map<String, String> get() {
        return environment.getSystemEnvironment().keySet().stream()
            .filter(endsWith(suffix))
            .filter(entry -> StringUtils.hasText(environment.getProperty(entry)))
            .collect(Collectors.toMap(this::convertToPropertyName, substitute(environment)));
    }

    private String convertToPropertyName(String propertyName) {
        String deSuffixed = propertyName.substring(0, propertyName.length() - suffix.length());
        String property = deSuffixed.replace("_", ".");
        return property.toLowerCase();
    }
}
