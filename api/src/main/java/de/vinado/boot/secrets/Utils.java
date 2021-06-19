package de.vinado.boot.secrets;

import org.springframework.core.log.LogMessage;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Collection of utility functions.
 *
 * @author Vincent Nadoll
 */
final class Utils {

    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @SafeVarargs
    public static <T> Consumer<T> acceptAndLog(Consumer<T> consumer, Consumer<Object> level, String format,
                                               Function<T, ?>... argumentTransformers) {
        return consumer.andThen(input -> log(input, level, format, argumentTransformers));
    }

    @SafeVarargs
    public static <T> Predicate<T> testAndLogFailure(Predicate<T> predicate, Consumer<Object> level, String format,
                                                     Function<T, ?>... argumentTransformers) {
        return input -> {
            if (predicate.test(input)) {
                return true;
            }

            log(input, level, format, argumentTransformers);
            return false;
        };
    }

    @SafeVarargs
    public static <T> void log(T input, Consumer<Object> level, String format,
                               Function<T, ?>... argumentTransformers) {
        Object[] arguments = Arrays.stream(argumentTransformers)
            .map(transformer -> transformer.apply(input))
            .filter(not(Throwable.class::isInstance))
            .toArray(Object[]::new);
        LogMessage message = LogMessage.format(format, arguments);

        level.accept(message);
    }

    public static <T> Predicate<T> not(Predicate<T> input) {
        return input.negate();
    }

    public static Predicate<String> startsWith(String prefix) {
        return key -> key.startsWith(prefix);
    }

    public static UnaryOperator<String> substring(int beginIndex) {
        return str -> str.substring(beginIndex);
    }

    public static Predicate<String> endsWith(String suffix) {
        return key -> key.endsWith(suffix);
    }

    public static <V> Predicate<Entry<?, V>> value(Predicate<V> predicate) {
        return entry -> predicate.test(entry.getValue());
    }
}
