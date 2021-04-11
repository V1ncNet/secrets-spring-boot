package de.vinado.boot.secrets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
 *
 * @author Vincent Nadoll
 */
@RequiredArgsConstructor
public class EnvironmentPropertyIndexSupplier implements PropertyIndexSupplier {

    @NonNull
    private final ConfigurableEnvironment environment;
    @NonNull
    private final String suffix;

    public EnvironmentPropertyIndexSupplier(@NonNull ConfigurableEnvironment environment) {
        this(environment, "");
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
