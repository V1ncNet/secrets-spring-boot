package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * An environment post-processor that resolves every environment variable with a <em>_FILE</em> suffix.
 *
 * @author Vincent Nadoll
 */
public final class EnvironmentSecretsPropertyEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor implements Ordered {

    public static final String ENV_VAR_SUFFIX = "_FILE";
    public static final int ORDER = EnvironmentConfigDataSecretsEnvironmentPostProcessor.ORDER + 1;

    public EnvironmentSecretsPropertyEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
    }

    @Override
    protected PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment) {
        return new EnvironmentPropertyIndexSupplier(environment, ENV_VAR_SUFFIX);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
