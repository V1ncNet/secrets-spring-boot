package de.vinado.boot.secrets;

import org.springframework.core.env.PropertyResolver;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * An interface for specifying {@link Supplier}'s type.
 *
 * @author Vincent Nadoll
 */
@FunctionalInterface
public interface PropertyIndexSupplier extends Supplier<Map<String, String>> {

    /**
     * Creates an index supplier which substitutes every value contained by the underlying map.
     *
     * @param resolver the component that resolves system property
     * @return new instance of a substituting index supplier
     */
    default PropertyIndexSupplier substituteValues(PropertyResolver resolver) {
        return () -> get().entrySet().stream()
            .filter(entry -> StringUtils.hasText(resolver.getProperty(entry.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, substituteValue(resolver)));
    }

    /**
     * @param resolver the component that resolves system property
     * @return a function which substitutes the entry's value
     */
    default Function<Map.Entry<?, String>, String> substituteValue(PropertyResolver resolver) {
        return substitute(resolver)
            .andThen(String::trim)
            .compose(Map.Entry::getValue);
    }

    /**
     * @param resolver the component that resolves system property
     * @return a function which substitutes the its argument
     */
    default UnaryOperator<String> substitute(PropertyResolver resolver) {
        return resolver::getProperty;
    }
}
