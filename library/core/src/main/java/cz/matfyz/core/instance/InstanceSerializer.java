package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class InstanceSerializer {

    public record SerializedInstance(
        List<SerializedInstanceObject> objects,
        List<SerializedInstanceMorphism> morphisms
    ) implements Serializable {}

    public record SerializedInstanceObject(
        Key key,
        List<SerializedDomainRow> rows
    ) {}

    public record SerializedDomainRow(
        int id,
        SuperIdWithValues superId,
        List<String> technicalIds,
        List<Signature> pendingReferences
    ) {}

    public record SerializedInstanceMorphism(
        Signature signature,
        List<SerializedMappingRow> mappings
    ) {}

    public record SerializedMappingRow(
        int dom,
        int cod
    ) {}

    private final Map<Key, Map<DomainRow, Integer>> keyToRowToId = new TreeMap<>();

    public static SerializedInstance serialize(InstanceCategory instance) {
        return new InstanceSerializer().serializeInstance(instance);
    }

    private SerializedInstance serializeInstance(InstanceCategory instance) {
        final List<SerializedInstanceObject> objects = instance.allObjects().stream()
            .map(this::serializeObject)
            .toList();

        final List<SerializedInstanceMorphism> morphisms = instance.allMorphisms().stream()
            .map(this::serializeMorphism)
            .toList();

        return new SerializedInstance(
            objects,
            morphisms
        );
    }

    private SerializedInstanceObject serializeObject(InstanceObject object) {
        final Map<DomainRow, Integer> rowToId = new TreeMap<>();
        keyToRowToId.put(object.schema.key(), rowToId);

        int lastId = 0;
        final List<SerializedDomainRow> rows = new ArrayList<>();

        for (final DomainRow row : object.allRowsToSet()) {
            final var rowWrapper = new SerializedDomainRow(
                lastId++,
                row.superId,
                row.technicalIds.stream().toList(),
                row.pendingReferences.stream().toList()
            );
            rowToId.put(row, rowWrapper.id());
            rows.add(rowWrapper);
        }

        return new SerializedInstanceObject(
            object.schema.key(),
            rows
        );
    }

    private SerializedInstanceMorphism serializeMorphism(InstanceMorphism morphism) {
        final var domRowToId = keyToRowToId.get(morphism.schema.dom().key());
        final var codRowToId = keyToRowToId.get(morphism.schema.cod().key());
        final List<SerializedMappingRow> mappings = new ArrayList<>();

        for (final var mapping : morphism.allMappings()) {
            final int dom = domRowToId.get(mapping.domainRow());
            final int cod = codRowToId.get(mapping.codomainRow());
            mappings.add(new SerializedMappingRow(dom, cod));
        }

        return new SerializedInstanceMorphism(
            morphism.schema.signature(),
            mappings
        );
    }

    public static InstanceCategory deserialize(SerializedInstance serializedInstance, SchemaCategory schemaCategory) {
        return new InstanceSerializer().deserializeInstance(serializedInstance, schemaCategory);
    }

    private final Map<Key, Map<Integer, DomainRow>> keyToIdToRow = new TreeMap<>();

    private InstanceCategory deserializeInstance(SerializedInstance serializedInstance, SchemaCategory schemaCategory) {
        final var instance = new InstanceCategoryBuilder().setSchemaCategory(schemaCategory).build();

        for (final SerializedInstanceObject serializedObject : serializedInstance.objects)
            deserializeObject(serializedObject, instance);

        for (final SerializedInstanceMorphism serializedMorphism : serializedInstance.morphisms)
            deserializeMorphism(serializedMorphism, instance);

        return instance;
    }

    private void deserializeObject(SerializedInstanceObject serializedObject, InstanceCategory instance) {
        final var object = instance.getObject(serializedObject.key);

        final Map<Integer, DomainRow> idToRow = new TreeMap<>();
        keyToIdToRow.put(serializedObject.key, idToRow);

        for (final var serializedRow : serializedObject.rows) {
            final var row = new DomainRow(
                serializedRow.superId,
                new TreeSet<>(serializedRow.technicalIds),
                new TreeSet<>(serializedRow.pendingReferences)
            );

            idToRow.put(serializedRow.id, row);
            final var ids = row.superId.findAllIds(object.schema.ids()).foundIds();
            object.setRow(row, ids);
        }
    }

    private void deserializeMorphism(SerializedInstanceMorphism serializedMorphism, InstanceCategory instance) {
        final var morphism = instance.getMorphism(serializedMorphism.signature);

        final var domIdToRow = keyToIdToRow.get(morphism.schema.dom().key());
        final var codIdToRow = keyToIdToRow.get(morphism.schema.cod().key());

        for (final var serializedMapping : serializedMorphism.mappings) {
            final var domRow = domIdToRow.get(serializedMapping.dom);
            final var codRow = codIdToRow.get(serializedMapping.cod);

            morphism.createMapping(domRow, codRow);
        }
    }

}
