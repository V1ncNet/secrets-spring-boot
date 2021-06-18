package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static de.vinado.boot.secrets.Utils.acceptAndLog;

/**
 * A wrapper around {@link ConfigurableEnvironment} which loads secrets and applies them to {@link
 * SecretPropertiesPropertySource}.
 *
 * @author Vincent Nadoll
 */
public class SecretsEnvironment {

    private final Log log;
    private final ConfigurableEnvironment environment;
    private final SecretResolver resolver;
    private final PropertyIndexSupplier propertyIndexSupplier;

    private final Map<String, Object> source = new HashMap<>();

    public SecretsEnvironment(DeferredLogFactory logFactory, ConfigurableEnvironment environment,
                              SecretResolver resolver, PropertyIndexSupplier propertyIndexSupplier) {
        Assert.notNull(logFactory, "Log factory must not be null");
        Assert.notNull(environment, "Environment must not be null");
        Assert.notNull(resolver, "Resolver must not be null");
        Assert.notNull(propertyIndexSupplier, "Property index supplier must not be null");

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
                .ifPresent(acceptAndLog(putTo(propertyName), log::info, "Use secret value to set [%s]",
                    propertyValue -> propertyName));
        }
    }

    private Consumer<Object> putTo(String systemProperty) {
        return secretValue -> source.put(systemProperty, secretValue);
    }
}
