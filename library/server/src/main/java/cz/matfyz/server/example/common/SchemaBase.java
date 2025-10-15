package cz.matfyz.server.example.common;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.evolution.category.Composite;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.SMO;
import cz.matfyz.evolution.category.UpdateMorphism;
import cz.matfyz.evolution.category.UpdateObjex;
import cz.matfyz.evolution.metadata.MMO;
import cz.matfyz.evolution.metadata.MorphismMetadata;
import cz.matfyz.evolution.metadata.ObjexMetadata;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class SchemaBase {

    private static final double POSITION_UNIT = 125;

    // The entity is needed for updates (if the schema isn't created from scratch, we need the previous data).
    private final SchemaCategoryEntity entity;

    /** The example schema category from which we take inspiration for objexes and morphisms in the new one. */
    private final SchemaCategory originalSchema;
    // The original metadata isn't needes since it's mostly included in the builder objexes and morphisms. Except for the positions, but those are specified manually.

    /** The new schema category as it's being created from scratch. */
    private final SchemaCategory newSchema;
    private final MetadataCategory newMetadata;

    protected SchemaBase(SchemaCategoryEntity entity, SchemaCategory schema) {
        this.entity = entity;
        this.originalSchema = schema;
        this.newSchema = new SchemaCategory();
        this.newMetadata = MetadataCategory.createEmpty(newSchema);
    }

    private SerializedObjex getOldObjex(Key key) {
        final var objex = newSchema.getObjex(key);
        return new SerializedObjex(key, objex.ids());
    }

    protected SchemaEvolutionInit innerCreateNewUpdate() {
        createOperations();

        return new SchemaEvolutionInit(entity.version(), schemaOperations, metadataOperations);
    }

    protected abstract void createOperations();

    private List<SMO> schemaOperations = new ArrayList<>();

    private void addSchemaOperation(SMO smo) {
        schemaOperations.add(smo);
        smo.up(newSchema, newMetadata);
    }

    private List<MMO> metadataOperations = new ArrayList<>();

    protected void addMetadataOperation(MMO mmo) {
        metadataOperations.add(mmo);
        mmo.up(newMetadata);
    }

    protected void addObjex(BuilderObjex builderObjex, double x, double y) {
        final var key = builderObjex.key();
        final var objex = originalSchema.getObjex(key);
        // Signature ids can't be defined yet because there are no morphisms. Even in the composite operations the ids are defined later.
        final ObjexIds ids = objex.ids().isSignatures()
            // However, ids can't be null in any case, so we create a generated one.
            ? ObjexIds.createGenerated()
            : objex.ids();

        final var schema = new SerializedObjex(key, ids);
        final var metadata = new SerializedMetadataObjex(key, builderObjex.label(), createPosition(x, y));
        addSchemaOperation(new CreateObjex(schema, metadata));
        addMetadataOperation(new ObjexMetadata(metadata, null));
    }

    private static Position createPosition(double x, double y) {
        return new Position(x * POSITION_UNIT, y * POSITION_UNIT);
    }

    protected void addIds(BuilderObjex builderObjex) {
        final var key = builderObjex.key();
        final var objex = originalSchema.getObjex(key);
        final var newObjex = new SerializedObjex(key, objex.ids());
        final var oldObjex = getOldObjex(key);

        addSchemaOperation(new UpdateObjex(newObjex, oldObjex));
    }

    protected void editIds(BuilderObjex builderObjex, ObjexIds ids) {
        final var key = builderObjex.key();
        final var newObjex = new SerializedObjex(key, ids);
        final var oldObjex = getOldObjex(key);

        addSchemaOperation(new UpdateObjex(newObjex, oldObjex));
    }

    protected void addMorphism(BuilderMorphism builderMorphism) {
        final var signature = builderMorphism.signature();
        final var morphism = originalSchema.getMorphism(signature);

        final var schema = SerializedMorphism.serialize(morphism);
        final var metadata = new SerializedMetadataMorphism(signature, builderMorphism.label());
        addSchemaOperation(new CreateMorphism(schema, metadata));
        addMetadataOperation(new MorphismMetadata(metadata, null));
    }

    protected void updateMorphism(BaseSignature signature, @Nullable Key newDom, @Nullable Key newCod) {
        final var oldMorphism = SerializedMorphism.serialize(originalSchema.getMorphism(signature));
        final var newMorphism = new SerializedMorphism(
            oldMorphism.signature(),
            newDom != null ? newDom : oldMorphism.domKey(),
            newCod != null ? newCod : oldMorphism.codKey(),
            oldMorphism.min(),
            oldMorphism.tags()
        );

        addSchemaOperation(new UpdateMorphism(newMorphism, oldMorphism));
    }

    protected void updateMorphism(BuilderMorphism morphism, @Nullable BuilderObjex newDom, @Nullable BuilderObjex newCod) {
        updateMorphism(morphism.signature(), newDom != null ? newDom.key() : null, newCod != null ? newCod.key() : null);
    }

    protected void moveObjex(BuilderObjex objex, double x, double y) {
        final var key = objex.key();
        final var oldMo = newMetadata.getObjex(key);
        final var serializedOldMo = new SerializedMetadataObjex(key, oldMo.label, oldMo.position);

        final var mo = new SerializedMetadataObjex(key, oldMo.label, createPosition(x, y));
        addMetadataOperation(new ObjexMetadata(mo, serializedOldMo));
    }

    protected void addComposite(String name, Runnable content) {
        final var innerContext = new ArrayList<SMO>();
        final var currentContext = schemaOperations;
        schemaOperations = innerContext;
        content.run();
        schemaOperations = currentContext;
        addSchemaOperation(new Composite(name, innerContext));
    }

    protected static final String ADD_PROPERTY = "addProperty";
    protected static final String ADD_SET = "addSet";
    protected static final String ADD_MAP = "addMap";

    private static class VersionCounter {

        private final List<Integer> levelIds;

        VersionCounter(Collection<Integer> levelIds) {
            this.levelIds = new ArrayList<>(levelIds);
        }

        static VersionCounter fromString(String version) {
            final var levelIds = Stream.of(version.split("\\.")).map(Integer::parseInt).toList();

            return new VersionCounter(levelIds);
        }

        private int last() {
            return levelIds.size() - 1;
        }

        void nextLevel() {
            levelIds.add(0);
        }

        void prevLevel() {
            levelIds.remove(last());
        }

        String next() {
            levelIds.set(last(), levelIds.get(last()) + 1);

            return levelIds.stream().map(Object::toString).collect(Collectors.joining("."));
        }

    }

}
