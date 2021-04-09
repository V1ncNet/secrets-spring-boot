package de.vinado.boot.secrets;

import java.util.Map;
import java.util.function.Supplier;

/**
 * An interface for specifying {@link Supplier}'s type.
 *
 * @author Vincent Nadoll
 */
@FunctionalInterface
public interface PropertyIndexSupplier extends Supplier<Map<String, String>> {
}
