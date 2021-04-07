package de.vinado.boot.secrets;

import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A processor that resolves every environment variable with a <em>_FILE</em> suffix. If the variable value contains a
 * file URI, the content of this file is loaded. The variable name is used to override the system property. The name is
 * converted to lower case, underscores are replaced by periods and <em>_FILE</em> suffix will be removed. The secret's
 * value will replace an existing property value with the same name.
 * <p>
 * The variable <em>SPRING_DATASOURCE_PASSWORD_FILE</em> will be converted to <em>spring.datasource.password</em>. Other
 * special characters won't be altered.
 *
 * @author Vincent Nadoll
 */
public final class SuffixBasedEnvironmentPropertySecretsProcessor extends EnvironmentPropertySecretsProcessor implements Ordered {

    public static final String PROPERTY_SOURCE_NAME = "suffixBasedEnvironmentPropertySecrets";

    public SuffixBasedEnvironmentPropertySecretsProcessor(DeferredLogFactory logFactory) {
        super(logFactory.getLog(SuffixBasedEnvironmentPropertySecretsProcessor.class), PROPERTY_SOURCE_NAME);
    }

    @Override
    protected Map<String, String> getSystemProperties(ConfigurableEnvironment environment) {
        return environment.getSystemEnvironment().keySet().stream()
            .filter(endsWith("_FILE"))
            .collect(Collectors.toMap(this::convertToPropertyName, Function.identity()));
    }

    private static Predicate<String> endsWith(String suffix) {
        return key -> key.endsWith(suffix);
    }

    private String convertToPropertyName(String fileEnvironmentVariableName) {
        String deSuffixed = fileEnvironmentVariableName.substring(0, fileEnvironmentVariableName.length() - 5);
        String property = deSuffixed.replace("_", ".");
        return property.toLowerCase(Locale.US);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
