package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * An environment post-processor that resolves <em>secrets.file.properties</em> from the application.properties file and
 * overrides existing properties. These property values must be resolvable environment properties.
 *
 * @author Vincent Nadoll
 */
public final class EnvironmentConfigDataSecretsEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor implements Ordered {

    public static final String CONFIG_DATA_INFIX = "env";
    public static final int ORDER = FilenameConfigDataSecretsEnvironmentPostProcessor.ORDER + 1;

    private final DeferredLogFactory logFactory;

    public EnvironmentConfigDataSecretsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
        this.logFactory = logFactory;
    }

    @Override
    protected PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment) {
        return new ConfigDataPropertyIndexSupplier(logFactory, environment, CONFIG_DATA_INFIX)
            .substituteValues(environment);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
