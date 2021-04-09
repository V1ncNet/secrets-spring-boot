package de.vinado.boot.secrets;

import org.apache.commons.logging.Log;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;

/**
 * A processor that resolves environment variables and loads the file content from its value.
 *
 * @author Vincent Nadoll
 */
public abstract class EnvironmentPropertySecretsProcessor extends SecretsProcessor {

    public EnvironmentPropertySecretsProcessor(Log log, String propertySourceName) {
        super(log, propertySourceName);
    }

    /**
     * @param environment must not be {@code null}
     * @return index of system properties (key) and environment variables which contains the secret's file-URI (value).
     */
    @Override
    protected abstract Map<String, String> getSystemProperties(ConfigurableEnvironment environment);

    @Override
    protected SecretResolver getResolver(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        EnvironmentPropertySubstituter substituter = new EnvironmentPropertySubstituter(environment);
        return new DefaultSecretResolver(substituter, resourceLoader);
    }
}
