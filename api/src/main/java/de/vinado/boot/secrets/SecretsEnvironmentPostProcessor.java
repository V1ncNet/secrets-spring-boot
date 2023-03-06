package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * <p>
 * An {@link EnvironmentPostProcessor} that loads and applies a {@link SecretsEnvironment} to Spring's
 * {@link org.springframework.core.env.Environment}.
 * </p><p>
 * This component implements {@link Ordered} to draw attention to the fact, that {@link SecretsEnvironmentPostProcessor}
 * must be executed after {@link ConfigDataEnvironmentPostProcessor} in order to override configuration properties. I'd
 * recommend to set the value to {@code ConfigDataEnvironmentPostProcessor.ORDER + n}.
 * </p>
 *
 * @author Vincent Nadoll
 */
public abstract class SecretsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

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

    /**
     * Creates a new instance of a {@link SecretResolver}.
     *
     * @param resourceLoader Spring's {@link ResourceLoader} creating {@link org.springframework.core.io.Resource}s;
     *                       never {@literal null}
     * @return a new instance of a {@link SecretResolver}
     */
    protected SecretResolver getSecretResolver(ResourceLoader resourceLoader) {
        return new DefaultSecretResolver(resourceLoader);
    }

    /**
     * Creates a new instance of {@link PropertyIndexSupplier} providing a map of configuration property keys and its
     * locations. Use {@link PropertyIndexSupplier#substituteValues(org.springframework.core.env.PropertyResolver)} if
     * you provided values which has to be substituted first.
     *
     * @param environment the current {@link ConfigurableEnvironment} and
     *                    {@link org.springframework.core.env.PropertyResolver}; never {@literal null}
     * @return a new instance of a {@link PropertyIndexSupplier}
     */
    protected abstract PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment);

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 100;
    }
}
