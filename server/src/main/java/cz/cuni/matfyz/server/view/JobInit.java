package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.server.entity.Job;

/**
 * @author jachym.bartik
 */
public record JobInit(
    int mappingId,
    String label,
    Job.Type type
) {}