package cz.matfyz.server.entity.datasource;

/**
 * @author jachym.bartik
 */
public record DataSourceUpdate(
    String url,
    String label
) {}