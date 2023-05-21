package cz.cuni.matfyz.server.entity.evolution;

/**
 * @author jachym.bartik
 */
record VersionedSMO(
    String version,
    SchemaModificationOperation smo
) {}