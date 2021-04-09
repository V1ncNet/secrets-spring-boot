package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;

/**
 * An environment post-processor that resolves all files from a configurable directory.
 *
 * @author Vincent Nadoll
 */
public final class FilenameSecretsEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor implements Ordered {

    public static final int ORDER = EnvironmentSecretsPropertyEnvironmentPostProcessor.ORDER + 1;

    private final DeferredLogFactory logFactory;

    public FilenameSecretsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
        this.logFactory = logFactory;
    }

    @Override
    protected SecretsEnvironment createSecretsEnvironment(ConfigurableEnvironment environment,
                                                          ResourceLoader resourceLoader) {
        DefaultSecretResolver resolver = new DefaultSecretResolver(Substituter.noop(), resourceLoader);
        FilenamePropertyIndexSupplier propertyIndexSupplier = new FilenamePropertyIndexSupplier(logFactory, environment);
        return new SecretsEnvironment(logFactory, environment, resolver, propertyIndexSupplier);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
