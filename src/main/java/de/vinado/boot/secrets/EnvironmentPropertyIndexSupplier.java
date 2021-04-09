package de.vinado.boot.secrets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A supplier for creating a property index over all environment properties ending with the configured suffix. The
 * property's name gets converted to the an applicable system property name and is used to set the index's key.
 * 1. Trimming of suffix
 * 2. Replacement of underscores with periods
 * 3. Conversion to lowercase
 * The value will be the the property name and has to be substituted.
 * <p>
 * <em>SPRING_DATASOURCE_PASSWORD_FILE</em> → <em>spring.datasource.password</em>
 *
 * @author Vincent Nadoll
 * @see EnvironmentPropertySubstituter
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
            .collect(Collectors.toMap(this::convertToPropertyName, Function.identity()));
    }

    private static Predicate<String> endsWith(String suffix) {
        return key -> key.endsWith(suffix);
    }

    private String convertToPropertyName(String propertyName) {
        String deSuffixed = propertyName.substring(0, propertyName.length() - suffix.length());
        String property = deSuffixed.replace("_", ".");
        return property.toLowerCase();
    }
}
