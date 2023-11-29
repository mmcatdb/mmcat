package cz.matfyz.server.example.basic;

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
import cz.matfyz.tests.schema.BasicSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SchemaSetup {

    public static SchemaUpdateInit createNewUpdate() {
        final var update = new SchemaSetup();
        update.createOperations();
        
        return new SchemaUpdateInit(Version.generateInitial(), update.operations, update.metadata);
    }

    private SchemaSetup() {}

    private SchemaCategory schema = BasicSchema.newSchemaCategory();

    private void createOperations() {
        // Order

        addObject(BasicSchema.order, 0, 0);
        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.number, 0, -1);
            addMorphism(BasicSchema.orderToNumber);
        });
        addIds(BasicSchema.order);

        addObject(BasicSchema.tag, -1, -1);
        addMorphism(BasicSchema.tagToOrder);

        // Customer

        addObject(BasicSchema.customer, -2, 0);
        addMorphism(BasicSchema.orderToCustomer);
        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.name, -2, -1);
            addMorphism(BasicSchema.customerToName);
        });
        addIds(BasicSchema.customer);

        addObject(BasicSchema.friend, -3, -0);
        addMorphism(BasicSchema.friendToCustomerA);
        addMorphism(BasicSchema.friendToCustomerB);
        addIds(BasicSchema.friend);

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.since, -3, -1);
            addMorphism(BasicSchema.friendToSince);
        });

        // Address

        addObject(BasicSchema.address, -2, 1);
        addMorphism(BasicSchema.orderToAddress);

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.street, -3, 1);
            addMorphism(BasicSchema.addressToStreet);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.city, -3, 2);
            addMorphism(BasicSchema.addressToCity);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.zip, -2, 2);
            addMorphism(BasicSchema.addressToZip);
        });

        // Item - Product

        addObject(BasicSchema.product, 2, 1);
        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.id, 1, 2);
            addMorphism(BasicSchema.productToId);
        });
        addIds(BasicSchema.product);

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.label, 2, 2);
            addMorphism(BasicSchema.productToLabel);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.price, 3, 2);
            addMorphism(BasicSchema.productToPrice);
        });

        // Item

        addComposite(ADD_SET, () -> {
            addObject(BasicSchema.item, 1, 1);
            addMorphism(BasicSchema.itemToOrder);
            addMorphism(BasicSchema.itemToProduct);
            addIds(BasicSchema.item);
        });
        
        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.quantity, 1, 0);
            addMorphism(BasicSchema.itemToQuantity);
        });

        // Contact

        addObject(BasicSchema.value, 0, 2);

        addComposite(ADD_MAP, () -> {
            addObject(BasicSchema.type, -1, 2);
            addObject(BasicSchema.contact, 0, 1);
            addMorphism(BasicSchema.contactToType);
            addMorphism(BasicSchema.contactToOrder);
            addMorphism(BasicSchema.contactToValue);
            addIds(BasicSchema.contact);
        });

        // Note - Data

        addObject(BasicSchema.data, 2, -1);

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.subject, 3, -1);
            addMorphism(BasicSchema.dataToSubject);
        });

        addComposite(ADD_PROPERTY, () -> {
            addObject(BasicSchema.content, 3, 0);
            addMorphism(BasicSchema.dataToContent);
        });

        // Note

        addComposite(ADD_MAP, () -> {
            addObject(BasicSchema.locale, 2, 0);
            addObject(BasicSchema.note, 1, -1);
            addMorphism(BasicSchema.noteToLocale);
            addMorphism(BasicSchema.noteToOrder);
            addMorphism(BasicSchema.noteToData);
            addIds(BasicSchema.note);
        });
    }

    private List<VersionedSMO> operations = new ArrayList<>();
    private Map<Key, SchemaObjectWrapper.Data> lastVersions = new TreeMap<>();
    private List<MetadataUpdate> metadata = new ArrayList<>();

    private VersionCounter counter = new VersionCounter(List.of(0));

    private void addObject(Key key, int x, int y) {
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

    private void addIds(Key key) {
        final var object = schema.getObject(key);
        final var data = lastVersions.get(key);
        final var newData = new SchemaObjectWrapper.Data(data.label(), object.ids(), object.superId(), data.iri(), data.pimIri());
        lastVersions.put(key, newData);

        operations.add(new VersionedSMO(
            counter.next(),
            new EditObject(key, newData, data)
        ));
    }

    private void addMorphism(Signature signature) {
        final var morphism = schema.getMorphism(signature);

        operations.add(new VersionedSMO(
            counter.next(),
            new CreateMorphism(SchemaMorphismWrapper.fromSchemaMorphism(morphism))
        ));
    }

    private interface CompositeOperationContent {
        void apply();
    }

    private void addComposite(String name, CompositeOperationContent content) {
        counter.nextLevel();
        content.apply();
        counter.prevLevel();
        operations.add(new VersionedSMO(counter.next(), new Composite(name)));
    }

    private static final String ADD_PROPERTY = "addProperty";
    private static final String ADD_SET = "addSet";
    private static final String ADD_MAP = "addMap";

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
