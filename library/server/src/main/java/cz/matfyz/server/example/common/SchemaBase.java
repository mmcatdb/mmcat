package cz.matfyz.server.example.common;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.Composite;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.CreateMorphism;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.CreateObject;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.EditMorphism;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.EditObject;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.evolution.VersionedSMO;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.MetadataUpdate;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class SchemaBase {

    private static final double POSITION_UNIT = 125;

    // The wrapper is needed for updates (if the schema isn't created from scratch, we need the previous data).
    private final SchemaCategoryWrapper wrapper;
    private final SchemaCategory schema;
    private final VersionCounter counter;

    protected SchemaBase(SchemaCategoryWrapper wrapper, String lastUpdateVersion, SchemaCategory schema) {
        this.wrapper = wrapper;
        this.schema = schema;
        this.counter = VersionCounter.fromString(lastUpdateVersion);
    }

    protected SchemaUpdateInit innerCreateNewUpdate() {
        createOperations();

        return new SchemaUpdateInit(wrapper.version, operations, metadata);
    }

    protected abstract void createOperations();

    private List<VersionedSMO> operations = new ArrayList<>();

    private Map<Key, SchemaObjectWrapper.Data> wrapperCache = new TreeMap<>();

    private SchemaObjectWrapper.Data getObjectData(Key key) {
        return wrapperCache.containsKey(key)
            ? wrapperCache.get(key)
            : Stream.of(wrapper.objects).filter(object -> object.key().equals(key)).findFirst().get().data();
    }

    private List<MetadataUpdate> metadata = new ArrayList<>();

    protected void moveObject(Key key, double x, double y) {
        metadata.add(new MetadataUpdate(key, new Position(x * POSITION_UNIT, y * POSITION_UNIT)));
    }

    protected void addObject(Key key, double x, double y) {
        final var object = schema.getObject(key);
        // Signature ids can't be defined yet because there are no morphisms. Even in the composite operations the ids are defined later.
        final ObjectIds ids = !object.ids().isSignatures()
                ? object.ids()
                : null;
        final SignatureId superId = ids != null
            ? ids.generateDefaultSuperId()
            : new SignatureId();

        final var data = new SchemaObjectWrapper.Data(object.label(), ids, superId);
        wrapperCache.put(key, data);

        operations.add(new VersionedSMO(
            counter.next(),
            new CreateObject(key, data)
        ));

        moveObject(key, x, y);
    }

    protected void addIds(Key key) {
        final var object = schema.getObject(key);
        final var data = getObjectData(key);
        final var newData = new SchemaObjectWrapper.Data(data.label(), object.ids(), object.superId());
        wrapperCache.put(key, newData);

        operations.add(new VersionedSMO(
            counter.next(),
            new EditObject(key, newData, data)
        ));
    }

    protected void editIds(Key key, ObjectIds ids) {
        final var data = getObjectData(key);
        final var newData = new SchemaObjectWrapper.Data(data.label(), ids, ids.generateDefaultSuperId());
        wrapperCache.put(key, newData);

        operations.add(new VersionedSMO(
            counter.next(),
            new EditObject(key, newData, data)
        ));
    }

    protected void addMorphism(Signature signature) {
        final var morphism = schema.getMorphism(signature);

        operations.add(new VersionedSMO(
            counter.next(),
            new CreateMorphism(SchemaMorphismWrapper.fromSchemaMorphism(morphism))
        ));
    }

    protected void editMorphism(Signature signature, @Nullable Key newDom, @Nullable Key newCod) {
        final var morphism = schema.getMorphism(signature);
        final var newWrapper = new SchemaMorphismWrapper(
            morphism.signature(),
            morphism.label,
            newDom != null ? newDom : morphism.dom().key(),
            newCod != null ? newCod : morphism.cod().key(),
            morphism.min(),
            morphism.tags()
        );

        operations.add(new VersionedSMO(
            counter.next(),
            new EditMorphism(newWrapper, SchemaMorphismWrapper.fromSchemaMorphism(morphism))
        ));
    }

    protected interface CompositeOperationContent {
        void apply();
    }

    protected void addComposite(String name, CompositeOperationContent content) {
        counter.nextLevel();
        content.apply();
        counter.prevLevel();
        operations.add(new VersionedSMO(counter.next(), new Composite(name)));
    }

    protected static final String ADD_PROPERTY = "addProperty";
    protected static final String ADD_SET = "addSet";
    protected static final String ADD_MAP = "addMap";

    private static class VersionCounter {

        private final int branchId;
        private final List<Integer> levelIds;

        VersionCounter(int branchId, Collection<Integer> levelIds) {
            this.branchId = branchId;
            this.levelIds = new ArrayList<>(levelIds);
        }

        static VersionCounter fromString(String version) {
            final var parts = version.split(":");
            final var branchId = Integer.parseInt(parts[0]);
            final var levelIds = Stream.of(parts[1].split("\\.")).map(Integer::parseInt).toList();

            return new VersionCounter(branchId, levelIds);
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

            return branchId + ":" + levelIds.stream().map(Object::toString).collect(Collectors.joining("."));
        }

    }

}
