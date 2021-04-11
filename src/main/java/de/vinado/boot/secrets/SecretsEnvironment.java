package de.vinado.boot.secrets;

import lombok.NonNull;
import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.log.LogMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A wrapper around {@link ConfigurableEnvironment} which loads secrets and applies them to
 * {@link SecretPropertiesPropertySource}.
 *
 * @author Vincent Nadoll
 */
public class SecretsEnvironment {

    private final Log log;
    private final ConfigurableEnvironment environment;
    private final SecretResolver resolver;
    private final PropertyIndexSupplier propertyIndexSupplier;

    private final Map<String, Object> source = new HashMap<>();

    public SecretsEnvironment(@NonNull DeferredLogFactory logFactory,
                              @NonNull ConfigurableEnvironment environment,
                              @NonNull SecretResolver resolver,
                              @NonNull PropertyIndexSupplier propertyIndexSupplier) {
        this.log = logFactory.getLog(getClass());
        this.environment = environment;
        this.resolver = resolver;
        this.propertyIndexSupplier = propertyIndexSupplier;
    }

    public final void processAndApply() {
        resolveSecretResources();
        SecretPropertiesPropertySource.merge(source, environment.getPropertySources());
    }

    protected void resolveSecretResources() {
        for (Map.Entry<String, String> entry : propertyIndexSupplier.get().entrySet()) {
            String propertyName = entry.getKey();
            String location = entry.getValue();
            resolver.loadContent(location)
                .ifPresent(doAndLog(add(propertyName), log::info, "Use secret value to set %s", propertyValue -> propertyName));
        }
    }

    @SafeVarargs
    private static <T> Consumer<T> doAndLog(Consumer<T> consumer, Consumer<Object> level, String format,
                                            Function<T, Object>... argumentTransformers) {
        Consumer<T> after = p -> {
            Object[] args = Arrays.stream(argumentTransformers)
                .map(transformer -> transformer.apply(p))
                .filter(not(Throwable.class::isInstance))
                .toArray(Object[]::new);
            LogMessage message = LogMessage.format(format, args);
            level.accept(message);
        };
        return consumer.andThen(after);
    }

    private static <T> Predicate<T> not(Predicate<T> input) {
        return input.negate();
    }

    private Consumer<Object> add(String systemProperty) {
        return secretValue -> source.put(systemProperty, secretValue);
    }
}
