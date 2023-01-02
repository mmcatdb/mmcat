package cz.cuni.matfyz.server.entity.datasource;

/**
 * @author jachym.bartik
 */
public record DataSourceUpdate(
    String url,
    String label
) {}