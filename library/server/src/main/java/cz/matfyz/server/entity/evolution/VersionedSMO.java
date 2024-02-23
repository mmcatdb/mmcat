package cz.matfyz.server.entity.evolution;

/**
 * @author jachym.bartik
 */
public record VersionedSMO(
    String version,
    SchemaModificationOperation smo
) {}
