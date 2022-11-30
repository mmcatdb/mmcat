package cz.cuni.matfyz.server.entity.logicalmodel;

/**
 * @author jachym.bartik
 */
public record LogicalModelInit(
    int databaseId,
    int categoryId,
    String jsonValue
) {}
