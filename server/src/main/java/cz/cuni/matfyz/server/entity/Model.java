package cz.cuni.matfyz.server.entity;

/**
 * @author jachym.bartik
 */
public record Model(
    int jobId,
    int schemaId,
    String jobName,
    String commands
) {}
