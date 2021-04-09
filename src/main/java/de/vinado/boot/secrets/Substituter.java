package de.vinado.boot.secrets;

import java.util.Optional;

/**
 * Substitutes the given value with another value.
 *
 * @author Vincent Nadoll
 */
@FunctionalInterface
public interface Substituter {

    /**
     * Substitutes the given value.
     *
     * @param original the value to substitute
     * @return substituted value
     */
    Optional<String> substitute(String original);

    /**
     * @return a substituter which wraps the given value in an {@link Optional}.
     */
    static Substituter noop() {
        return Optional::of;
    }
}
