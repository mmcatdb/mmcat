package cz.cuni.matfyz.server.view;


/**
 * @author jachym.bartik
 */
public record NewJobView(
    int mappingId,
    String name,
    String type
) {}