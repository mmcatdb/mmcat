package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.BaseSignature;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

public class InstanceBuilder {

    private final InstanceCategory instance;

    public InstanceBuilder(SchemaCategory schema) {
        this.instance = createEmptyInstance(schema);
    }

    private static InstanceCategory createEmptyInstance(SchemaCategory schema) {
        final Map<Key, InstanceObjex> objexes = new TreeMap<>();
        final Map<BaseSignature, InstanceMorphism> morphisms = new TreeMap<>();
        final var instance = new InstanceCategory(schema, objexes, morphisms);
        /** For each objex A, we store a list of signatures of morphisms from A to all value-identified objexes B. */
        final Map<SchemaObjex, Set<BaseSignature>> dependentObjexes = new TreeMap<>();

        final var baseMorphisms = schema.allMorphisms().stream().filter(SchemaMorphism::isBase).toList();
        for (final var schemaMorhpism : baseMorphisms) {
            if (schemaMorhpism.cod().ids().isValue() && !schemaMorhpism.dom().superId().contains(schemaMorhpism.signature())) {
                final var set = dependentObjexes.computeIfAbsent(schemaMorhpism.dom(), x -> new TreeSet<>());
                set.add((BaseSignature) schemaMorhpism.signature());
            }
            else if (schemaMorhpism.dom().ids().isValue() && !schemaMorhpism.cod().superId().contains(schemaMorhpism.signature().dual())) {
                final var set = dependentObjexes.computeIfAbsent(schemaMorhpism.cod(), x -> new TreeSet<>());
                set.add(((BaseSignature) schemaMorhpism.signature()).dual());
            }
        }

        for (final SchemaObjex schemaObjex : schema.allObjexes()) {
            if (schemaObjex.ids().isValue())
                continue;

            var dependents = dependentObjexes.get(schemaObjex);
            if (dependents == null)
                dependents = Set.of();

            final InstanceObjex instanceObjex = new InstanceObjex(schemaObjex, instance, dependents);
            objexes.put(instanceObjex.schema.key(), instanceObjex);
        }

        // The base moprhisms must be created first because the composite ones use them.
        for (final var schemaMorphism : baseMorphisms) {
            final var instanceMorphism = new InstanceMorphism(schemaMorphism);
            morphisms.put((BaseSignature) schemaMorphism.signature(), instanceMorphism);
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

    private final Map<Signature, String> values = new TreeMap<>();

    private InstanceBuilder value(Signature signature, @Nullable String value) {
        if (value != null)
            values.put(signature, value);

        return this;
    }

    public InstanceBuilder value(BuilderMorphism morphism, @Nullable String value) {
        return value(morphism.signature(), value);
    }

    public InstanceBuilder generatedId(String value) {
        return value(Signature.createEmpty(), value);
    }

    public DomainRow objex(Key key) {
        final var instanceObjex = instance.getObjex(key);

        final var builder = new SuperIdValues.Builder();
        instanceObjex.schema.superId().forEach(s -> {
            final var value = values.get(s);
            if (value != null)
                builder.add(s, value);
        });
        final SuperIdValues superId = builder.build();

        final var row = instanceObjex.createRow(superId);
        createdRows.computeIfAbsent(key, k -> new ArrayList<>()).add(row);

        instanceObjex.simpleSignatures().forEach(s -> {
            final var value = values.get(s);
            if (value != null)
                row.addSimpleValue((BaseSignature) s, value);
        });

        values.clear(); // Clear the values for the next objex.

        return row;
    }

    public DomainRow objex(BuilderObjex objex) {
        return objex(objex.key());
    }

    // Building mapping rows

    public MappingRow morphism(Signature signature, DomainRow domainRow, DomainRow codomainRow) {
        return instance.getMorphism((BaseSignature) signature).createMapping(domainRow, codomainRow);
    }

    public MappingRow morphism(BuilderMorphism morphism, DomainRow domainRow, DomainRow codomainRow) {
        return morphism(morphism.signature(), domainRow, codomainRow);
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
