package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * An environment post-processor that resolves every environment variable with a <em>_FILE</em> suffix.
 *
 * @author Vincent Nadoll
 */
public final class SuffixBasedEnvironmentPropertySecretsProcessor extends EnvironmentPropertySecretsProcessor implements Ordered {

    public static final String ENV_VAR_SUFFIX = "_FILE";

    public SuffixBasedEnvironmentPropertySecretsProcessor(DeferredLogFactory logFactory) {
        super(logFactory.getLog(SuffixBasedEnvironmentPropertySecretsProcessor.class));
    }

    @Override
    protected Map<String, String> getSystemProperties(ConfigurableEnvironment environment) {
        return new EnvironmentPropertyIndexSupplier(environment, ENV_VAR_SUFFIX).get();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
