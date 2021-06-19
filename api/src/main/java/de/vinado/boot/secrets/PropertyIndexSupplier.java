package de.vinado.boot.secrets;

import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static de.vinado.boot.secrets.Utils.value;

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
        Assert.notNull(resolver, "Property resolver must not be null");
        Predicate<String> wherePropertyHasText = value -> StringUtils.hasText(resolver.getProperty(value));
        return () -> get().entrySet().stream()
            .filter(value(wherePropertyHasText))
            .collect(Collectors.toMap(Map.Entry::getKey, substituteValue(resolver)));
    }

    /**
     * Substitutes the entry's value.
     *
     * @param resolver the component that resolves system property
     * @return a function which substitutes the entry's value
     */
    default Function<Map.Entry<?, String>, String> substituteValue(PropertyResolver resolver) {
        return substitute(resolver)
            .andThen(String::trim)
            .compose(Map.Entry::getValue);
    }

    /**
     * Substitutes the entry's value.
     *
     * @param resolver the component that resolves system property
     * @return a function which substitutes the its argument
     */
    default UnaryOperator<String> substitute(PropertyResolver resolver) {
        return resolver::getProperty;
    }

    /**
     * Creates a instance of this {@link PropertyIndexSupplier} by supplying just the given map.
     *
     * @param properties must not be {@literal null}
     * @return new instance of {@link PropertyIndexSupplier}
     */
    static PropertyIndexSupplier from(Map<String, String> properties) {
        Assert.notNull(properties, "Properties must not be null");
        return () -> new HashMap<>(properties);
    }
}
