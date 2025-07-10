package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
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
        final Map<Key, InstanceObjex> objexes = new TreeMap<>();
        final Map<Signature, InstanceMorphism> morphisms = new TreeMap<>();
        final var instance = new InstanceCategory(schema, objexes, morphisms);

        for (final SchemaObjex schemaObjex : schema.allObjexes()) {
            final InstanceObjex instanceObjex = new InstanceObjex(schemaObjex, instance);
            objexes.put(instanceObjex.schema.key(), instanceObjex);
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
        instance.getObjex(key).technicalIdGenerator = generator;
        return this;
    }

    public InstanceCategory build() {
        return this.instance;
    }

    // Building domain rows

    private final SuperIdValues.Builder valuesBuilder = new SuperIdValues.Builder();

    public InstanceBuilder value(Signature signature, String value) {
        valuesBuilder.add(signature, value);

        return this;
    }

    public InstanceBuilder value(BuilderMorphism morphism, String value) {
        return value(morphism.signature(), value);
    }

    public DomainRow objex(Key key) {
        final var instanceObjex = instance.getObjex(key);
        final SuperIdValues values = valuesBuilder.build();

        final var row = instanceObjex.createRow(values);
        createdRows.computeIfAbsent(key, k -> new ArrayList<>()).add(row);

        return row;
    }

    public DomainRow objex(BuilderObjex objex) {
        return objex(objex.key());
    }

    public DomainRow valueObjex(BuilderObjex objex, String value) {
        return valueObjex(objex.key(), value);
    }

    public DomainRow valueObjex(Key key, String value) {
        return value(Signature.createEmpty(), value).objex(key);
    }

    // Building mapping rows

    public MappingRow morphism(Signature signature, DomainRow domainRow, DomainRow codomainRow) {
        return instance.getMorphism(signature).createMapping(domainRow, codomainRow);
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

    public List<DomainRow> getRows(BuilderObjex objex) {
        return getRows(objex.key());
    }

    public DomainRow getRow(Key key, int index) {
        return createdRows.get(key).get(index);
    }

    public DomainRow getRow(BuilderObjex objex, int index) {
        return getRow(objex.key(), index);
    }

    public interface InstanceAdder {
        void add(InstanceBuilder builder);
    }

}
