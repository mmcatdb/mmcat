package cz.matfyz.server.utils;

import cz.matfyz.server.example.common.DatasourceBuilder.DatasourceProperties;

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
        String adminerDatabase,
        String queryEvolutionDatabase,
        String inferenceDatabase,
        String tpchDatabase
    ) implements DatasourceProperties {}

    @ConfigurationProperties("spark")
    public record SparkProperties(
        String master,
        String checkpoint
    ) {}

    @ConfigurationProperties("uploads")
    public record UploadsProperties(
        String directory
    ) {}

    @ConfigurationProperties("adaptation")
    public record AdaptationProperties(
        String pythonPath,
        String scriptName
    ) {}

}
