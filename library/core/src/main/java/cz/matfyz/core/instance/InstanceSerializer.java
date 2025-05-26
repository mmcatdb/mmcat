package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.UniqueSequentialGenerator;
import cz.matfyz.core.utils.UniqueSequentialGenerator.SerializedUniqueSequentialGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class InstanceSerializer {

    public record SerializedInstance(
        List<SerializedInstanceObjex> objexes,
        List<SerializedInstanceMorphism> morphisms
    ) implements Serializable {}

    public record SerializedInstanceObjex(
        Key key,
        List<SerializedDomainRow> rows,
        SerializedUniqueSequentialGenerator technicalIdGenerator
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
        final List<SerializedInstanceObjex> objexes = instance.allObjexes().stream()
            .map(this::serializeObjex)
            .toList();

        final List<SerializedInstanceMorphism> morphisms = instance.allMorphisms().stream()
            .map(this::serializeMorphism)
            .toList();

        return new SerializedInstance(
            objexes,
            morphisms
        );
    }

    private SerializedInstanceObjex serializeObjex(InstanceObjex objex) {
        final Map<DomainRow, Integer> rowToId = new TreeMap<>();
        keyToRowToId.put(objex.schema.key(), rowToId);

        int lastId = 0;
        final List<SerializedDomainRow> rows = new ArrayList<>();

        for (final DomainRow row : objex.allRowsToSet()) {
            final var rowWrapper = new SerializedDomainRow(
                lastId++,
                row.superId,
                row.technicalIds.stream().toList(),
                row.pendingReferences.stream().toList()
            );
            rowToId.put(row, rowWrapper.id());
            rows.add(rowWrapper);
        }

        return new SerializedInstanceObjex(
            objex.schema.key(),
            rows,
            objex.technicalIdGenerator.serialize()
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

    public static InstanceCategory deserialize(SerializedInstance serializedInstance, SchemaCategory schema) {
        return new InstanceSerializer().deserializeInstance(serializedInstance, schema);
    }

    private final Map<Key, Map<Integer, DomainRow>> keyToIdToRow = new TreeMap<>();

    private InstanceCategory deserializeInstance(SerializedInstance serializedInstance, SchemaCategory schema) {
        final var instance = new InstanceBuilder(schema).build();

        for (final SerializedInstanceObjex serializedObjex : serializedInstance.objexes)
            deserializeObjex(serializedObjex, instance);

        for (final SerializedInstanceMorphism serializedMorphism : serializedInstance.morphisms)
            deserializeMorphism(serializedMorphism, instance);

        return instance;
    }

    private void deserializeObjex(SerializedInstanceObjex serializedObjex, InstanceCategory instance) {
        final var objex = instance.getObjex(serializedObjex.key);
        objex.technicalIdGenerator = UniqueSequentialGenerator.deserialize(serializedObjex.technicalIdGenerator);

        final Map<Integer, DomainRow> idToRow = new TreeMap<>();
        keyToIdToRow.put(serializedObjex.key, idToRow);

        for (final var serializedRow : serializedObjex.rows) {
            final var row = new DomainRow(
                serializedRow.superId,
                new TreeSet<>(serializedRow.technicalIds),
                new TreeSet<>(serializedRow.pendingReferences)
            );

            idToRow.put(serializedRow.id, row);
            final var ids = row.superId.findAllIds(objex.schema.ids()).foundIds();
            objex.setRow(row, ids);
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
