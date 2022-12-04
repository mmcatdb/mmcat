package cz.cuni.matfyz.server.entity.job;

/**
 * @author jachym.bartik
 */
public record JobInit(
    int logicalModelId,
    String label,
    Job.Type type
) {}