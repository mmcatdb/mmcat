package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;
import cz.matfyz.core.utils.UniqueSequentialGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InstanceBuilder {

    private final InstanceCategory instance;

    public InstanceBuilder(SchemaCategory schema) {
        this.instance = createEmptyInstance(schema);
    }

    private static InstanceCategory createEmptyInstance(SchemaCategory schema) {
        final Map<Key, InstanceObject> objects = new TreeMap<>();
        final Map<Signature, InstanceMorphism> morphisms = new TreeMap<>();
        final var instance = new InstanceCategory(schema, objects, morphisms);

        for (SchemaObject schemaObject : schema.allObjects()) {
            final InstanceObject instanceObject = new InstanceObject(schemaObject, instance);
            objects.put(instanceObject.schema.key(), instanceObject);
        }

        // The base moprhisms must be created first because the composite ones use them.
        final var baseMorphisms = schema.allMorphisms().stream().filter(SchemaMorphism::isBase).toList();
        for (final var schemaMorphism : baseMorphisms) {
            final var instanceMorphism = new InstanceMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }

        final var compositeMorphisms = schema.allMorphisms().stream().filter(morphism -> !morphism.isBase()).toList();
        for (final var schemaMorphism : compositeMorphisms) {
            final var instanceMorphism = new InstanceMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }

        return instance;
    }

    public InstanceBuilder technicalIdGenerator(Key key, UniqueSequentialGenerator generator) {
        instance.getObject(key).technicalIdGenerator = generator;
        return this;
    }

    public InstanceCategory build() {
        return this.instance;
    }

    // Building domain rows

    private final SuperIdWithValues.Builder superIdBuilder = new SuperIdWithValues.Builder();

    public InstanceBuilder value(Signature signature, String value) {
        superIdBuilder.add(signature, value);

        return this;
    }

    public InstanceBuilder value(BuilderMorphism morphism, String value) {
        return value(morphism.signature(), value);
    }

    public DomainRow object(Key key) {
        final var instanceObject = instance.getObject(key);
        final SuperIdWithValues superId = superIdBuilder.build();

        var row = instanceObject.getRow(superId);
        if (row == null)
            row = instanceObject.getOrCreateRow(superId);

        createdRows.computeIfAbsent(key, k -> new ArrayList<>()).add(row);

        return row;
    }

    public DomainRow object(BuilderObject object) {
        return object(object.key());
    }

    public DomainRow valueObject(BuilderObject object, String value) {
        return valueObject(object.key(), value);
    }

    public DomainRow valueObject(Key key, String value) {
        return value(Signature.createEmpty(), value).object(key);
    }

    // Building mapping rows

    public MappingRow morphism(Signature signature, DomainRow domainRow, DomainRow codomainRow) {
        var row = new MappingRow(domainRow, codomainRow);
        instance.getMorphism(signature).addMapping(row);

        return row;
    }

    public MappingRow morphism(BuilderMorphism morphism, DomainRow domainRow, DomainRow codomainRow) {
        return morphism(morphism.signature(), domainRow, codomainRow);
    }

    public void morphism(BuilderMorphism morphism) {
        morphism(morphism.signature());
    }

    public void morphism(Signature signature) {
        instance.getMorphism(signature);
    }

    // Getters for rows to allow creating mapping rows

    private Map<Key, List<DomainRow>> createdRows = new TreeMap<>();

    public List<DomainRow> getRows(Key key) {
        return createdRows.get(key);
    }

    public List<DomainRow> getRows(BuilderObject object) {
        return getRows(object.key());
    }

    public DomainRow getRow(Key key, int index) {
        return createdRows.get(key).get(index);
    }

    public DomainRow getRow(BuilderObject object, int index) {
        return getRow(object.key(), index);
    }

    public interface InstanceAdder {
        void add(InstanceBuilder builder);
    }

}
