package de.vinado.spring.secrets;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * @author Vincent Nadoll
 */
final class Functions {

    private Functions() {
        throw new UnsupportedOperationException();
    }

    public static <T> UnaryOperator<T> peek(Consumer<T> action) {
        return t -> {
            action.accept(t);
            return t;
        };
    }

    public static <T> Consumer<T> doAndLog(Consumer<T> consumer, Consumer<Object> level, String format, Object... arguments) {
        return consumer.andThen(p -> level.accept(String.format(format, arguments)));
    }
}
