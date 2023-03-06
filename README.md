Spring Boot Secrets
===================

Spring Boot Secrets is a collection of environment post-processors with which
files with sensitive content, so-called secrets, can be loaded and added to
the Spring configuration.

The project currently provides four sophisticated post-processors. These can
either be used individually or concurrently. If you want to implement your
own post-processor, use the API artifact, which is available separately.

Spring Boot Secrets is ideal for resolving **Docker Secrets** and making them
accessible to the Spring Boot application.

**Note:** Compatible with Spring Boot 3


Features
--------

* 4 configurable post-processors
* separated API artifact


Usage
-----

In order to activate one or, if necessary, several post processors, the
`META-INF/spring.factories` file must be created in the `resources/` folder.
One or more post-processors can then be added to this file as required.

```properties
org.springframework.boot.env.EnvironmentPostProcessor=\
  de.vinado.boot.secrets.FilenameConfigDataSecretsEnvironmentPostProcessor,\
  de.vinado.boot.secrets.EnvironmentConfigDataSecretsEnvironmentPostProcessor,\
  de.vinado.boot.secrets.FilenameSecretsEnvironmentPostProcessor,\
  de.vinado.boot.secrets.EnvironmentSecretsPropertyEnvironmentPostProcessor
```

The post-processors have an order of execution which can be taken from the
example above. It should be noted that the post-processors can overwrite the
set values of the previously executed ones if they set the same property. The
order is fixed and cannot be changed.

### API Package

With the API package, new post-processors can be implemented quickly and easily.
The API provides the `SecretsEnvironmentPostProcessor` class, which only has to
be inherited from.

```java
public class DockerSecretProcessor extends SecretsEnvironmentPostProcessor {

    public DockerSecretProcessor(DeferredLogFactory logFactory) {
        super(logFactory);
    }

    @Override
    protected PropertyIndexSupplier getPropertyIndexSupplier(ConfigurableEnvironment environment) {
        Map<String, String> envProperties = new HashMap<>();
        envProperties.put("spring.datasource.username", "DATABASE_USER_FILE");
        envProperties.put("spring.datasource.password", "DATABASE_PASSWORD_FILE");
        envProperties.put("spring.mail.username", "SMTP_USER_FILE");
        envProperties.put("spring.mail.password", "SMTP_PASSWORD_FILE");
        PropertyIndexSupplier env = PropertyIndexSupplier.from(envProperties);

        Map<String, String> fileProperties = new HashMap<>();
        fileProperties.put("spring.mail.username", "/run/secrets/smtp_username");
        fileProperties.put("spring.mail.password", "/run/secrets/smtp_password");

        return CompositePropertyIndexSupplier.overriding()
            .add(env)
            .add(fileProperties)
            .buildAndSubstitute(environment);
    }
}
```

spring.factories
```properties
org.springframework.boot.env.EnvironmentPostProcessor=package.of.your.DockerSecretProcessor
```

### Maven Configuration

The collection is available under following coordinates:

```xml
<dependency>
    <groupId>de.vinado.boot</groupId>
    <artifactId>secrets</artifactId>
    <version>2.0.0</version>
</dependency>
```

or use the following coordinates if you just wish to implement your own
post-processors:

```xml
<dependency>
    <groupId>de.vinado.boot</groupId>
    <artifactId>secrets-api</artifactId>
    <version>2.0.0</version>
</dependency>
```


Available Post-Processors
---------------------------

### `FilenameConfigDataSecretsEnvironmentPostProcessor`

This post-processor loads the properties already set by Spring Boot from the
`application.{properties|yml}` file. All file names prefixed with
`secrets.file.properties` are processed.

```properties
secrets.file.properties.spring.mail.host=classpath:spring_mail_host
secrets.file.properties.spring.datasource.username=/run/secrets/spring.datasource.username
secrets.file.properties.spring.datasource.password=file:/run/secrets/spring.datasource.password
```

As with the following post-processor, the values can be specified using an
absolute path or URI.

### `EnvironmentConfigDataSecretsEnvironmentPostProcessor`

This component works similarly to the
`FilenameConfigDataSecretsEnvironmentPostProcessor`. The prefix for all
properties to be processed is `secrets.env.properties`. However, this
post-processor expects system properties or environment variables, which are
additionally substituted before they are made available to the application.

```properties
secrets.file.properties.spring.mail.host=SMTP_USER_FILE_LOCATION
secrets.file.properties.spring.datasource.username=EMPTY_SECRET_FILE
```

### `FilenameSecretsEnvironmentPostProcessor`

The `FilenameSecretsEnvironmentPostProcessor` is interesting for those who
deploy their Spring Boot application in Docker Swarm Mode and want to use
Docker Secrets. By default, all files located under `/run/secrets` are resolved
and added to the Spring configuration.

However, all file names must follow a certain syntax so that they can then be
assigned to the correct configuration. The file with the name
`spring.datasource.password` becomes the property `spring.datasource.password`
and the content of the file, its value.

The base directory and separator can be configured. The separator can
alternatively take the value `_` and will be replaced by a point during
processing.

### `EnvironmentSecretsPropertyEnvironmentPostProcessor`

This post processor is also for those who use Docker Secrets. All environment
variables ending with `_FILE` are processed. The name of the variable forms the
name of the property to be set with its suffixed stripped off. Again, the
underscores are replaced by dots.

#### Example

```shell
echo /run/secrets/database_password > foo
export SPRING_DATASOURCE_PASSWORD_FILE=/run/secrets/database_password
```

becomes

```properties
spring.datasource.password=foo
```


Configuration
-------------

| application.properties  | Data Type  | Default Value  | Example                                                              | Post-Processor                                         |
|-------------------------|------------|----------------|----------------------------------------------------------------------|--------------------------------------------------------|
| secrets.file.properties | _`Map`_    |                | `spring.datasource.username=/run/secrets/spring.datasource.username` | `FilenameConfigDataSecretsEnvironmentPostProcessor`    |
| secrets.file.base-dir   | _`String`_ | `/run/secrets` | `/some/base/directory`                                               | `FilenameSecretsEnvironmentPostProcessor`              |
| secrets.file.separator  | _`String`_ | `.`            | Only `.` or `_`                                                      | `FilenameSecretsEnvironmentPostProcessor`              |
| secrets.env.properties  | _`Map`_    |                | `spring.mail.host=SMTP_USER_FILE`                                    | `EnvironmentConfigDataSecretsEnvironmentPostProcessor` |


Licence
-------

Apache License 2.0 - [Vinado](https://vinado.de) - Built with :heart: in Dresden
