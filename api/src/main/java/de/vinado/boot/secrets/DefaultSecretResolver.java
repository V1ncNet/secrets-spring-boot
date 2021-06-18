package de.vinado.boot.secrets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

/**
 * The default resolver implementation for loading the content of a {@link Resource}.
 *
 * @author Vincent Nadoll
 */
@RequiredArgsConstructor
public class DefaultSecretResolver implements SecretResolver {

    @NonNull
    private final ResourceLoader resourceLoader;

    /**
     * Substitutes the given location and loads its secret afterwards.
     *
     * @param location location from which the content is loaded
     * @return secret
     */
    public Optional<String> loadContent(@Nullable String location) {
        return Optional.ofNullable(location)
            .flatMap(resolveWith(resourceLoader))
            .map(this::readContent)
            .filter(StringUtils::hasText)
            .map(String::trim);
    }

    private Function<String, Optional<Resource>> resolveWith(ResourceLoader resourceLoader) {
        return location -> loadResource(location, resourceLoader);
    }

    private Optional<Resource> loadResource(String location, ResourceLoader resourceLoader) {
        return Optional.of(location)
            .filter(StringUtils::hasText)
            .map(resourceLoader::getResource)
            .filter(Resource::exists);
    }

    @SneakyThrows(IOException.class)
    private String readContent(Resource resource) {
        try (InputStream stream = resource.getInputStream()) {
            return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        }
    }
}
