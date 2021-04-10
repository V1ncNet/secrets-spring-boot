package de.vinado.boot.secrets;

import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;

/**
 * An environment post-processor that resolves <em>secrets.file.properties</em> from the application.properties file and
 * overrides existing properties. These property values must be resolvable environment properties.
 *
 * @author Vincent Nadoll
 */
public final class EnvironmentConfigDataSecretsEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor implements Ordered {

    public static final String CONFIG_DATA_INFIX = "env";
    public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 10;

    private final DeferredLogFactory logFactory;

    public EnvironmentConfigDataSecretsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
        this.logFactory = logFactory;
    }

    @Override
    protected SecretsEnvironment createSecretsEnvironment(ConfigurableEnvironment environment,
                                                          ResourceLoader resourceLoader) {
        EnvironmentPropertySubstituter substituter = new EnvironmentPropertySubstituter(environment);
        ConfigDataPropertyIndexSupplier indexSupplier =
            new ConfigDataPropertyIndexSupplier(logFactory, environment, CONFIG_DATA_INFIX);
        DefaultSecretResolver secretResolver = new DefaultSecretResolver(substituter, resourceLoader);
        return new SecretsEnvironment(logFactory, environment, secretResolver, indexSupplier);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
