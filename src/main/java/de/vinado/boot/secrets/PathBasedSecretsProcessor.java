package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * An environment post-processor that resolves all files from a configurable directory.
 *
 * @author Vincent Nadoll
 */
public final class PathBasedSecretsProcessor extends SecretsProcessor implements Ordered {

    private final DeferredLogFactory logFactory;

    public PathBasedSecretsProcessor(DeferredLogFactory logFactory) {
        super(logFactory.getLog(PathBasedSecretsProcessor.class));
        this.logFactory = logFactory;
    }

    @Override
    protected Map<String, String> getSystemProperties(ConfigurableEnvironment environment) {
        return new FilenamePropertyIndexSupplier(logFactory, environment).get();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 11;
    }
}
