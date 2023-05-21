package cz.cuni.matfyz.server.entity.evolution;

import cz.cuni.matfyz.evolution.Version;

import java.util.List;

/**
 * @author jachym.bartik
 */
public record SchemaUpdateInit(
    Version prevVersion,
    List<VersionedSMO> operations
) {}
