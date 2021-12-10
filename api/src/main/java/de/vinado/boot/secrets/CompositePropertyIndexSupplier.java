package de.vinado.boot.secrets;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static de.vinado.boot.secrets.Utils.value;

/**
 * Composite {@link PropertyIndexSupplier} managing delegates of its own type.
 *
 * @author Vincent Nadoll
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CompositePropertyIndexSupplier implements PropertyIndexSupplier {

    private final List<PropertyIndexSupplier> delegates;
    private final BinaryOperator<String> mergeFunction;
    private final PropertyResolver resolver;

    /**
     * Merges and returns a map of all delegates this component holds. The merge strategy is implicitly set be the prior
     * used instantiation of {@link Builder}.
     *
     * @return a merged result map of all delegates
     * @throws IllegalStateException in case {@link #builder()} where used to instantiate the {@link Builder} and the
     *                               delegates contain duplicate keys.
     * @see #builder()
     * @see #keeping()
     * @see #overriding()
     */
    @Override
    public Map<String, String> get() throws IllegalStateException {
        return delegates.stream()
            .map(PropertyIndexSupplier::get)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .map(value(substituteIfNotNull()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction));
    }

    private UnaryOperator<String> substituteIfNotNull() {
        if (null == resolver) {
            return UnaryOperator.identity();
        }

        return substitute(resolver);
    }

    @Override
    public PropertyIndexSupplier substituteValues(PropertyResolver resolver) {
        Assert.notNull(resolver, "Property resolver must not be null");
        return () -> get().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, substituteValue(resolver)));
    }

    @Override
    public UnaryOperator<String> substitute(PropertyResolver resolver) {
        Assert.notNull(resolver, "Property resolver must not be null");
        return value -> resolver.getProperty(value, value);
    }

    /**
     * Creates a new {@link Builder} throwing an exception lazily in case {@link #get()} encounters duplicate keys while
     * merging the underlying {@link PropertyIndexSupplier delegates}.
     *
     * @return a new instance of {@link Builder}
     * @see #keeping()
     * @see #overriding()
     */
    public static Builder builder() {
        return CompositePropertyIndexSupplier.using((u, v) -> {
            throw new IllegalStateException("Duplicate key " + u);
        });
    }

    /**
     * Creates a new {@link Builder} keeping existing values of duplicate keys during {@link #get() merge}.
     *
     * @return a new instance of {@link Builder}
     */
    public static Builder keeping() {
        return CompositePropertyIndexSupplier.using((u, v) -> u);
    }

    /**
     * Creates a new {@link Builder} overriding existing values of duplicate keys during {@link #get() merge}.
     *
     * @return a new instance of {@link Builder}
     */
    public static Builder overriding() {
        return CompositePropertyIndexSupplier.using((u, v) -> v);
    }


    /**
     * Creates a new {@link Builder} using the given merge function to handle duplicate keys during {@link #get()
     * merge}.
     *
     * @return a new instance of {@link Builder}
     */
    public static Builder using(BinaryOperator<String> mergeFunction) {
        Assert.notNull(mergeFunction, "Merge function must not be null");
        return new Builder(mergeFunction);
    }

    /**
     * Convenience component making use of the Builder Pattern to instantiate a new {@link
     * CompositePropertyIndexSupplier}.
     *
     * @author Vincent Nadoll
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final List<PropertyIndexSupplier> delegates = new ArrayList<>();
        private final BinaryOperator<String> mergeFunction;

        public Builder add(Map<String, String> properties) {
            Assert.notNull(properties, "Properties must not be null");
            PropertyIndexSupplier supplier = PropertyIndexSupplier.from(properties);
            delegates.add(supplier);
            return this;
        }

        public Builder add(PropertyIndexSupplier supplier) {
            Assert.notNull(supplier, "Property index supplier must not be null");
            delegates.add(supplier);
            return this;
        }

        public Builder addAll(Collection<PropertyIndexSupplier> suppliers) {
            Assert.notNull(suppliers, "Property index supplier collection must not be null");
            delegates.addAll(suppliers);
            return this;
        }

        /**
         * Creates a new instance of {@link CompositePropertyIndexSupplier} leaving property values as is.
         *
         * @return a new instance of {@link CompositePropertyIndexSupplier}
         */
        public CompositePropertyIndexSupplier build() {
            return new CompositePropertyIndexSupplier(new ArrayList<>(delegates), mergeFunction, null);
        }

        /**
         * Creates a new instance of {@link CompositePropertyIndexSupplier} substituting property values if necessary.
         * Note that the substitution only takes place when {@link CompositePropertyIndexSupplier#get()} is called.
         *
         * @param resolver must not be {@literal null}
         * @return a new instance of {@link CompositePropertyIndexSupplier}
         */
        public CompositePropertyIndexSupplier buildAndSubstitute(PropertyResolver resolver) {
            Assert.notNull(resolver, "Property resolver collection must not be null");
            return new CompositePropertyIndexSupplier(new ArrayList<>(delegates), mergeFunction, resolver);
        }
    }
}
