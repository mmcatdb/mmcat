package cz.matfyz.server.adaptation;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.utils.entity.Entity;
import cz.matfyz.server.utils.entity.Id;

import java.io.Serializable;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

class Adaptation extends Entity implements Serializable {

    public final Id categoryId;
    /** Version of the system for which we do the optimization. */
    public final Version systemVersion;
    public AdaptationSettings settings;
    /** Once the settings are set, a job can start. The job can then be restarted ... */
    public @Nullable Id runId;

    public Adaptation(Id id, Id categoryId, Version systemVersion, AdaptationSettings settings, @Nullable Id runId) {
        super(id);
        this.categoryId = categoryId;
        this.systemVersion = systemVersion;
        this.settings = settings;
        this.runId = runId;
    }

    static Adaptation createNew(SchemaCategoryEntity category, AdaptationSettings settings) {
        return new Adaptation(
            Id.createNew(),
            category.id(),
            category.systemVersion(),
            settings,
            null
        );
    }

    record AdaptationSettings(
        /** Preferably [0, infinity) but it's not a hard rule. */
        double explorationWeight,
        List<AdaptationObjex> objexes,
        List<AdaptationMorphism> morphisms,
        /** All datasources that might be used in the adaptation. */
        List<Id> datasourceIds
    ) implements Serializable {}

    /**
     * An entity (see {@link SchemaObjex#isEntity}) that is a subject to this {@link Adaptation}.
     */
    record AdaptationObjex(
        Key key,
        List<AdaptationMapping> mappings
    ) implements Serializable {}

    // TODO Probably remove or like inline to the objex (and make the whole objex optional? Or sth ...)
    record AdaptationMapping(
        Id datasourceId,
        // Some of these properties might be undefined if the DB doesn't support it (or if it would be too much pain to implement).
        /** Estimated size of data in bytes. */
        @Nullable Long dataSizeInBytes,
        /** Estimated number of records. */
        @Nullable Integer recordCount
    ) implements Serializable {}

    /**
     * A morphism between two {@link AdaptationObjex}.
     * At leas one way of including the target into the source must be allowed.
     */
    record AdaptationMorphism(
        BaseSignature signature,
        boolean isReferenceAllowed,
        // TODO We might want to allow this per-datasource.
        boolean isEmbeddingAllowed
    ) implements Serializable {}

    record AdaptationSolution(
        double price,
        List<AdaptationObjex> objexes
    ) implements Serializable {}

}
