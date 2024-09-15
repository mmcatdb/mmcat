package cz.matfyz.server.example.common;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObject.Position;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObject;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObject;
import cz.matfyz.evolution.metadata.MMO;
import cz.matfyz.evolution.metadata.MorphismMetadata;
import cz.matfyz.evolution.metadata.ObjectMetadata;
import cz.matfyz.evolution.schema.Composite;
import cz.matfyz.evolution.schema.CreateMorphism;
import cz.matfyz.evolution.schema.CreateObject;
import cz.matfyz.evolution.schema.UpdateMorphism;
import cz.matfyz.evolution.schema.UpdateObject;
import cz.matfyz.evolution.schema.SMO;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class SchemaBase {

    private static final double POSITION_UNIT = 125;

    // The wrapper is needed for updates (if the schema isn't created from scratch, we need the previous data).
    private final SchemaCategoryWrapper wrapper;

    /** The example schema category from which we take inspiration for objects and morphisms in the new one. */
    private final SchemaCategory originalSchema;
    // The original metadata isn't needes since it's mostly included in the builder objects and morphisms. Except for the positions, but those are specified manually.

    /** The new schema category as it's being created from scratch. */
    private final SchemaCategory newSchema;
    private final MetadataCategory newMetadata;

    protected SchemaBase(SchemaCategoryWrapper wrapper, SchemaCategory schema) {
        this.wrapper = wrapper;
        this.originalSchema = schema;
        this.newSchema = new SchemaCategory();
        this.newMetadata = MetadataCategory.createEmpty(newSchema);
    }

    private SerializedObject getOldObject(Key key) {
        final var object = newSchema.getObject(key);
        return new SerializedObject(key, object.ids(), object.superId());
    }

    protected SchemaUpdateInit innerCreateNewUpdate() {
        createOperations();

        return new SchemaUpdateInit(wrapper.version, schemaOperations, metadataOperations);
    }

    protected abstract void createOperations();

    private List<SMO> schemaOperations = new ArrayList<>();

    private void addSchemaOperation(SMO smo) {
        schemaOperations.add(smo);
        smo.up(newSchema);
    }

    private List<MMO> metadataOperations = new ArrayList<>();

    protected void addMetadataOperation(MMO mmo) {
        metadataOperations.add(mmo);
        mmo.up(newMetadata);
    }

    protected void addObject(BuilderObject builderObject, double x, double y) {
        final var key = builderObject.key();
        final var object = originalSchema.getObject(key);
        // Signature ids can't be defined yet because there are no morphisms. Even in the composite operations the ids are defined later.
        final ObjectIds ids = !object.ids().isSignatures()
                ? object.ids()
                : null;
        final SignatureId superId = ids != null
            ? ids.generateDefaultSuperId()
            : new SignatureId();

        addSchemaOperation(new CreateObject(new SerializedObject(key, ids, superId)));
        final var mo = new SerializedMetadataObject(key, builderObject.label(), createPosition(x, y));
        addMetadataOperation(new ObjectMetadata(mo, null));
    }

    private static Position createPosition(double x, double y) {
        return new Position(x * POSITION_UNIT, y * POSITION_UNIT);
    }

    protected void addIds(BuilderObject builderObject) {
        final var key = builderObject.key();
        final var object = originalSchema.getObject(key);
        final var newObject = new SerializedObject(key, object.ids(), object.superId());
        final var oldObject = getOldObject(key);

        addSchemaOperation(new UpdateObject(newObject, oldObject));
    }

    protected void editIds(BuilderObject builderObject, ObjectIds ids) {
        final var key = builderObject.key();
        final var newObject = new SerializedObject(key, ids, ids.generateDefaultSuperId());
        final var oldObject = getOldObject(key);

        addSchemaOperation(new UpdateObject(newObject, oldObject));
    }

    protected void addMorphism(BuilderMorphism builderMorphism) {
        final var signature = builderMorphism.signature();
        final var morphism = originalSchema.getMorphism(signature);

        addSchemaOperation(new CreateMorphism(SerializedMorphism.serialize(morphism)));
        final var mm = new SerializedMetadataMorphism(signature, builderMorphism.label());
        addMetadataOperation(new MorphismMetadata(mm, null));
    }

    protected void updateMorphism(Signature signature, @Nullable Key newDom, @Nullable Key newCod) {
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

    protected void updateMorphism(BuilderMorphism morphism, @Nullable BuilderObject newDom, @Nullable BuilderObject newCod) {
        updateMorphism(morphism.signature(), newDom != null ? newDom.key() : null, newCod != null ? newCod.key() : null);
    }

    protected void moveObject(BuilderObject object, double x, double y) {
        final var key = object.key();
        final var oldMo = newMetadata.getObject(key);
        final var serializedOldMo = new SerializedMetadataObject(key, oldMo.label, oldMo.position);

        final var mo = new SerializedMetadataObject(key, oldMo.label, createPosition(x, y));
        addMetadataOperation(new ObjectMetadata(mo, serializedOldMo));
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
