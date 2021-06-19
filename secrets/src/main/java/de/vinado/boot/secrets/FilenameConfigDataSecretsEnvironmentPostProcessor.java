package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * An environment post-processor that resolves <em>secrets.file.properties</em> from the application.properties file and
 * overrides existing properties.
 *
 * @author Vincent Nadoll
 */
public final class FilenameConfigDataSecretsEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor {

    public static final String CONFIG_DATA_INFIX = "file";
    public static final int ORDER = FilenameSecretsEnvironmentPostProcessor.ORDER + 1;

    private final DeferredLogFactory logFactory;

    public FilenameConfigDataSecretsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
        this.logFactory = logFactory;
    }

    @Override
    protected PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment) {
        return new ConfigDataPropertyIndexSupplier(logFactory, environment, CONFIG_DATA_INFIX);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
