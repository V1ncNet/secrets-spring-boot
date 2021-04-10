package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;

/**
 * An environment post-processor that resolves <em>secrets.file.properties</em> from the application.properties
 * file and overrides existing properties.
 *
 * @author Vincent Nadoll
 */
public final class FilenameConfigDataSecretsEnvironmentPostProcessor extends SecretsEnvironmentPostProcessor implements Ordered {

    public static final String CONFIG_DATA_INFIX = "file";
    public static final int ORDER = FilenameSecretsEnvironmentPostProcessor.ORDER + 1;

    private final DeferredLogFactory logFactory;

    public FilenameConfigDataSecretsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
        this.logFactory = logFactory;
    }

    @Override
    protected SecretsEnvironment createSecretsEnvironment(ConfigurableEnvironment environment,
                                                          ResourceLoader resourceLoader) {
        ConfigDataPropertyIndexSupplier indexSupplier =
            new ConfigDataPropertyIndexSupplier(logFactory, environment, CONFIG_DATA_INFIX);
        DefaultSecretResolver secretResolver = new DefaultSecretResolver(resourceLoader);
        return new SecretsEnvironment(logFactory, environment, secretResolver, indexSupplier);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
