package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;

/**
 * An environment post-processor that resolves every environment variable with a <em>_FILE</em> suffix.
 *
 * @author Vincent Nadoll
 */
public final class EnvironmentSecretsPropertyEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor implements Ordered {

    public static final String ENV_VAR_SUFFIX = "_FILE";
    public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 10;

    private final DeferredLogFactory logFactory;

    public EnvironmentSecretsPropertyEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
        this.logFactory = logFactory;
    }

    @Override
    protected SecretsEnvironment createSecretsEnvironment(ConfigurableEnvironment environment,
                                                          ResourceLoader resourceLoader) {
        Substituter substituter = new EnvironmentPropertySubstituter(environment);
        DefaultSecretResolver resolver = new DefaultSecretResolver(substituter, resourceLoader);
        EnvironmentPropertyIndexSupplier propertyIndexSupplier = new EnvironmentPropertyIndexSupplier(environment, ENV_VAR_SUFFIX);
        return new SecretsEnvironment(logFactory, environment, resolver, propertyIndexSupplier);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
