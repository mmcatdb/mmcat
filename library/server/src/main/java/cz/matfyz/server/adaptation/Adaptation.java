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
        List<AdaptationMorphism> morphisms
    ) implements Serializable {}

    /**
     * An entity (see {@link SchemaObjex#isEntity}) that is a subject to this {@link Adaptation}.
     */
    record AdaptationObjex(
        Key key,
        Id datasourceId
    ) implements Serializable {}

    /**
     * A morphism between two {@link AdaptationObjex}.
     * At leas one way of including the target into the source must be allowed.
     */
    record AdaptationMorphism(
        BaseSignature signature,
        boolean isReferenceAllowed,
        boolean isEmbeddingAllowed,
        boolean isInliningAllowed
    ) implements Serializable {
        static AdaptationMorphism createDefault(BaseSignature signature) {
            return new AdaptationMorphism(signature, true, false, false);
        }
    }

    record AdaptationSolution(
        double price,
        List<AdaptationObjex> objexes
    ) implements Serializable {}

}
