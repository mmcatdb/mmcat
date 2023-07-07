package cz.cuni.matfyz.server.entity.job;

import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record JobInit(
    Id categoryId,
    String label,
    JobPayload payload
) {}