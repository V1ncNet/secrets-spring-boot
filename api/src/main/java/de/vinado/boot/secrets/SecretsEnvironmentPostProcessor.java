package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * An {@link EnvironmentPostProcessor} that loads and applies a {@link SecretsEnvironment} to Spring's {@link
 * org.springframework.core.env.Environment}.
 *
 * @author Vincent Nadoll
 */
public abstract class SecretsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final Log log;
    private final DeferredLogFactory logFactory;

    public SecretsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        this.logFactory = logFactory;
        this.log = logFactory.getLog(getClass());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        ResourceLoader resourceLoader = application.getResourceLoader();
        postProcessEnvironment(environment, null == resourceLoader ? new DefaultResourceLoader() : resourceLoader);
    }

    void postProcessEnvironment(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        log.trace("Post-processing environment to add secrets");
        createSecretsEnvironment(environment, resourceLoader).processAndApply();
    }

    /**
     * Creates a new instance of {@link SecretsEnvironment}.
     *
     * @param environment    the environment to post-process
     * @param resourceLoader the resource loader to be used
     * @return new instance of {@link SecretsEnvironment}
     */
    protected SecretsEnvironment createSecretsEnvironment(ConfigurableEnvironment environment,
                                                          ResourceLoader resourceLoader) {
        SecretResolver secretResolver = getSecretResolver(resourceLoader);
        PropertyIndexSupplier indexSupplier = getPropertyIndexSupplier(environment);
        return new SecretsEnvironment(logFactory, environment, secretResolver, indexSupplier);
    }

    protected SecretResolver getSecretResolver(ResourceLoader resourceLoader) {
        return new DefaultSecretResolver(resourceLoader);
    }

    protected abstract PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment);
}
