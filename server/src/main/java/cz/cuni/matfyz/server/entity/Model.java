package cz.cuni.matfyz.server.entity;

import java.io.Serializable;

/**
 * @author jachym.bartik
 */
public record Model(
    Id jobId,
    Id categoryId,
    String jobLabel,
    String commands
) implements Serializable {}
