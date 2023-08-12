package cz.matfyz.server.entity.datasource;

/**
 * @author jachym.bartik
 */
public record DataSourceInit(
    String url,
    String label,
    DataSource.Type type
) {}