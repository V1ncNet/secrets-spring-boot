package de.vinado.boot.secrets;

import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * An environment post-processor that resolves all files from a configurable directory.
 *
 * @author Vincent Nadoll
 */
public final class FilenameSecretsEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor {

    public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 100;

    private final DeferredLogFactory logFactory;

    public FilenameSecretsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
        this.logFactory = logFactory;
    }

    @Override
    protected PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment) {
        return new FilenamePropertyIndexSupplier(logFactory, environment);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
