package cz.matfyz.server.example.common;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.Composite;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.CreateMorphism;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.CreateObject;
import cz.matfyz.server.entity.evolution.SchemaModificationOperation.EditObject;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.evolution.VersionedSMO;
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

public abstract class SchemaBase {

    private final SchemaCategory schema;

    protected SchemaBase(SchemaCategory schema) {
        this.schema = schema;
    }

    protected SchemaUpdateInit innerCreateNewUpdate() {
        createOperations();
        
        return new SchemaUpdateInit(Version.generateInitial(), operations, metadata);
    }

    protected abstract void createOperations();
    
    private List<VersionedSMO> operations = new ArrayList<>();
    private Map<Key, SchemaObjectWrapper.Data> lastVersions = new TreeMap<>();
    private List<MetadataUpdate> metadata = new ArrayList<>();

    private VersionCounter counter = new VersionCounter(List.of(0));

    protected void addObject(Key key, double x, double y) {
        final var object = schema.getObject(key);
        // Signature ids can't be defined yet because there are no morphisms. Even in the composite operations the ids are defined later.
        final ObjectIds ids = !object.ids().isSignatures()
                ? object.ids()
                : null;
        final SignatureId superId = ids != null
            ? ids.generateDefaultSuperId()
            : new SignatureId();
        
        final var data = new SchemaObjectWrapper.Data(object.label(), ids, superId, object.iri, object.pimIri);
        lastVersions.put(key, data);

        operations.add(new VersionedSMO(
            counter.next(),
            new CreateObject(key, data)
        ));

        final double positionUnit = 125;

        metadata.add(new MetadataUpdate(key, new Position(x * positionUnit, y * positionUnit)));
    }

    protected void addIds(Key key) {
        final var object = schema.getObject(key);
        final var data = lastVersions.get(key);
        final var newData = new SchemaObjectWrapper.Data(data.label(), object.ids(), object.superId(), data.iri(), data.pimIri());
        lastVersions.put(key, newData);

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

    private class VersionCounter {

        private List<Integer> version;

        VersionCounter(Collection<Integer> version) {
            this.version = new ArrayList<>(version);
        }

        private int last() {
            return version.size() - 1;
        }

        void nextLevel() {
            version.add(0);
        }
    
        void prevLevel() {
            version.remove(last());
        }

        String next() {
            version.set(last(), version.get(last()) + 1);

            return "0:" + version.stream().map(Object::toString).collect(Collectors.joining("."));
        }

    }

}
