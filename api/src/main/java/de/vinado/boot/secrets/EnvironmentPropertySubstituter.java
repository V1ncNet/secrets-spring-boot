package de.vinado.boot.secrets;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Substitutes the given environment property name with its value.
 *
 * @author Vincent Nadoll
 */
@RequiredArgsConstructor
public class EnvironmentPropertySubstituter implements Substituter {

    private final ConfigurableEnvironment environment;

    @Override
    public Optional<String> substitute(String environmentProperty) {
        return Optional.of(environmentProperty)
            .map(environment::getProperty)
            .filter(StringUtils::hasText)
            .map(String::trim);
    }
}
