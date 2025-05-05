package cz.matfyz.server.global;

import org.springframework.boot.context.properties.ConfigurationProperties;

public class Configuration {

    @ConfigurationProperties("server")
    public record ServerProperties(
        Integer port,
        String origin,
        Boolean executeModels
    ) {}

    @ConfigurationProperties("database")
    public record DatabaseProperties(
        String host,
        String port,
        String database,
        String username,
        String password
    ) {}

    @ConfigurationProperties("setup")
    public record SetupProperties(
        Boolean isInDocker,
        String username,
        String password,
        String basicDatabase,
        String queryEvolutionDatabase,
        String inferenceDatabase,
        String adminerDatabase
    ) {}

    @ConfigurationProperties("spark")
    public record SparkProperties(
        String master,
        String checkpoint
    ) {}

}
