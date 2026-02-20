package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.evolution.category.GroupProperties;
import cz.matfyz.evolution.category.GroupProperties.MovedMorphism;
import cz.matfyz.evolution.category.JoinProperties;
import cz.matfyz.evolution.category.SplitProperty;
import cz.matfyz.evolution.propagation.EvolutionPropagator;
import cz.matfyz.evolution.propagation.MappingEvolver;
import cz.matfyz.tests.example.basic.Schema;
import cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper;

/**
 * Tests for EvolutionPropagator and MappingEvolver.
 * Verifies that SMOs correctly translate to database statements
 * and that mapping access paths are properly updated.
 */
class PropagationTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    // === EvolutionPropagator Tests ===

    @Test
    void propagatorGeneratesGroupStatements() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Create mapping: "orders" table with columns "number"
        // order (key=1) -> number (key=2) via signature 1
        final var datasource = new Datasource(DatasourceType.postgresql, "test-pg");
        final var accessPath = new ComplexProperty(
            new TypedName(TypedName.ROOT), Signature.empty(),
            List.of(
                new SimpleProperty(new StringName("number"), new BaseSignature(1))
            )
        );
        final var mapping = new Mapping(datasource, "orders", schema, Schema.order.key(), accessPath, List.of());

        // Create GROUP operation: Order.number -> Order.details.number
        final var groupOp = createGroupOperation(
            Schema.order.key(), 1000, "details", 1001,
            List.of(new MovedMorphismSpec(1, 1002, 2, "number")),
            metadata
        );

        // Apply schema changes first
        groupOp.up(schema, metadata);

        // Now propagate to PostgreSQL
        final var wrapper = new PostgreSQLEvolutionWrapper();
        final var propagator = new EvolutionPropagator(wrapper, List.of(mapping), metadata);
        final List<AbstractStatement> statements = groupOp.accept(propagator);

        assertFalse(statements.isEmpty(), "GROUP should generate database statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("\"orders\""), "Should reference the orders table");
        assertTrue(allSql.contains("\"details\""), "Should reference the group name 'details'");
        assertTrue(allSql.contains("\"number\""), "Should reference the property 'number'");
    }

    @Test
    void propagatorGeneratesJoinStatements() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Create mapping: "products" table with columns "label", "price"
        final var datasource = new Datasource(DatasourceType.postgresql, "test-pg");
        final var accessPath = new ComplexProperty(
            new TypedName(TypedName.ROOT), Signature.empty(),
            List.of(
                new SimpleProperty(new StringName("id"), new BaseSignature(54)),
                new SimpleProperty(new StringName("label"), new BaseSignature(55)),
                new SimpleProperty(new StringName("price"), new BaseSignature(56))
            )
        );
        final var mapping = new Mapping(datasource, "products", schema, Schema.product.key(), accessPath, List.of());

        // Create JOIN: product.label + product.price -> product.info
        final var joinOp = createJoinOperation(
            Schema.product.key(),
            List.of(
                new SourcePropertySpec(55, 55, "label"),
                new SourcePropertySpec(56, 56, "price")
            ),
            new TargetPropertySpec(3000, 3001, "info"),
            " - ", true,
            metadata
        );

        joinOp.up(schema, metadata);

        final var wrapper = new PostgreSQLEvolutionWrapper();
        final var propagator = new EvolutionPropagator(wrapper, List.of(mapping), metadata);
        final List<AbstractStatement> statements = joinOp.accept(propagator);

        assertFalse(statements.isEmpty(), "JOIN should generate database statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("\"products\""), "Should reference the products table");
        assertTrue(allSql.contains("\"info\""), "Should reference the target property 'info'");
    }

    @Test
    void propagatorSkipsUnmappedOperations() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Create mapping for 'orders' but operation targets 'customer'
        final var datasource = new Datasource(DatasourceType.postgresql, "test-pg");
        final var accessPath = new ComplexProperty(
            new TypedName(TypedName.ROOT), Signature.empty(),
            List.of(
                new SimpleProperty(new StringName("number"), new BaseSignature(1))
            )
        );
        final var mapping = new Mapping(datasource, "orders", schema, Schema.order.key(), accessPath, List.of());

        // GROUP on customer (key=21) - which is NOT mapped
        final var groupOp = createGroupOperation(
            Schema.customer.key(), 1000, "details", 1001,
            List.of(new MovedMorphismSpec(22, 1002, 22, "name")),
            metadata
        );
        groupOp.up(schema, metadata);

        final var wrapper = new PostgreSQLEvolutionWrapper();
        final var propagator = new EvolutionPropagator(wrapper, List.of(mapping), metadata);
        final List<AbstractStatement> statements = groupOp.accept(propagator);

        assertTrue(statements.isEmpty(), "Should skip operation when parent objex is not mapped");
    }

    // === MappingEvolver Tests ===

    @Test
    void mappingEvolverGroupUpdatesAccessPath() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Create mapping: "orders" with subpath "number" (sig=1)
        final var datasource = new Datasource(DatasourceType.postgresql, "test-pg");
        final var accessPath = new ComplexProperty(
            new TypedName(TypedName.ROOT), Signature.empty(),
            List.of(
                new SimpleProperty(new StringName("number"), new BaseSignature(1))
            )
        );
        final var mapping = new Mapping(datasource, "orders", schema, Schema.order.key(), accessPath, List.of());

        // GROUP: Order.number -> Order.details.number
        final var groupOp = createGroupOperation(
            Schema.order.key(), 1000, "details", 1001,
            List.of(new MovedMorphismSpec(1, 1002, 2, "number")),
            metadata
        );

        // Apply schema change
        groupOp.up(schema, metadata);

        // Apply mapping evolution
        final var evolver = new MappingEvolver(List.of(mapping), schema, metadata);
        groupOp.accept(evolver);

        final var evolvedMappings = evolver.getEvolvedMappings();
        assertEquals(1, evolvedMappings.size());

        final var evolvedPath = evolvedMappings.get(0).accessPath();
        // The evolved path should contain a complex subpath "details" with "number" inside
        boolean foundDetails = false;
        for (final var subpath : evolvedPath.subpaths()) {
            if (subpath.name() instanceof StringName name && name.value.equals("details")) {
                foundDetails = true;
                assertTrue(subpath instanceof ComplexProperty,
                    "details should be a ComplexProperty");
                final var detailsComplex = (ComplexProperty) subpath;
                boolean foundNumber = false;
                for (final var innerSubpath : detailsComplex.subpaths()) {
                    if (innerSubpath.name() instanceof StringName innerName && innerName.value.equals("number")) {
                        foundNumber = true;
                    }
                }
                assertTrue(foundNumber, "Should find 'number' inside 'details'");
            }
        }
        assertTrue(foundDetails, "Should find 'details' in evolved access path");
    }

    @Test
    void mappingEvolverJoinUpdatesAccessPath() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Create mapping: "products" with "label" (sig=55) and "price" (sig=56)
        final var datasource = new Datasource(DatasourceType.postgresql, "test-pg");
        final var accessPath = new ComplexProperty(
            new TypedName(TypedName.ROOT), Signature.empty(),
            List.of(
                new SimpleProperty(new StringName("id"), new BaseSignature(54)),
                new SimpleProperty(new StringName("label"), new BaseSignature(55)),
                new SimpleProperty(new StringName("price"), new BaseSignature(56))
            )
        );
        final var mapping = new Mapping(datasource, "products", schema, Schema.product.key(), accessPath, List.of());

        // JOIN: label + price -> info (deleteSourceProperties=true)
        final var joinOp = createJoinOperation(
            Schema.product.key(),
            List.of(
                new SourcePropertySpec(55, 55, "label"),
                new SourcePropertySpec(56, 56, "price")
            ),
            new TargetPropertySpec(3000, 3001, "info"),
            " - ", true,
            metadata
        );

        joinOp.up(schema, metadata);

        final var evolver = new MappingEvolver(List.of(mapping), schema, metadata);
        joinOp.accept(evolver);

        final var evolvedMappings = evolver.getEvolvedMappings();
        final var evolvedPath = evolvedMappings.get(0).accessPath();

        // Should have "id" and "info" but NOT "label" or "price" (deleted)
        boolean foundInfo = false;
        boolean foundLabel = false;
        boolean foundPrice = false;
        for (final var subpath : evolvedPath.subpaths()) {
            if (subpath.name() instanceof StringName name) {
                if (name.value.equals("info")) foundInfo = true;
                if (name.value.equals("label")) foundLabel = true;
                if (name.value.equals("price")) foundPrice = true;
            }
        }
        assertTrue(foundInfo, "Should find 'info' in evolved access path");
        assertFalse(foundLabel, "Should NOT find 'label' after join with delete");
        assertFalse(foundPrice, "Should NOT find 'price' after join with delete");
    }

    // === Helper methods ===

    record MovedMorphismSpec(int oldSig, int newSig, int propertyKey, String label) {}
    record SourcePropertySpec(int objexKey, int morphismSig, String label) {}
    record TargetPropertySpec(int objexKey, int morphismSig, String label) {}

    private GroupProperties createGroupOperation(
        Key parentKey, int groupKeyInt, String groupLabel, int parentToGroupSigInt,
        List<MovedMorphismSpec> specs, MetadataCategory metadata
    ) {
        final var groupKey = new Key(groupKeyInt);
        final var groupObjex = new SerializedObjex(groupKey, ObjexIds.fromIds(SignatureId.empty()));
        final var groupObjexMeta = new SerializedMetadataObjex(groupKey, groupLabel, null);

        final var parentToGroupSig = new BaseSignature(parentToGroupSigInt);
        final var parentToGroupMorphism = new SerializedMorphism(
            parentToGroupSig, parentKey, groupKey, Min.ONE, Set.of()
        );
        final var parentToGroupMeta = new SerializedMetadataMorphism(parentToGroupSig, groupLabel);

        // Set metadata so EvolutionPropagator can resolve labels
        metadata.setObjex(groupKey, new cz.matfyz.core.metadata.MetadataObjex(
            groupLabel, cz.matfyz.core.metadata.MetadataObjex.Position.createDefault()));

        final var movedMorphisms = specs.stream().map(spec -> {
            final var oldSig = new BaseSignature(spec.oldSig);
            final var newSig = new BaseSignature(spec.newSig);
            final var propKey = new Key(spec.propertyKey);
            return new MovedMorphism(
                new SerializedMorphism(oldSig, parentKey, propKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(oldSig, spec.label),
                new SerializedMorphism(newSig, groupKey, propKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(newSig, spec.label)
            );
        }).toList();

        return new GroupProperties(parentKey, groupObjex, groupObjexMeta,
            parentToGroupMorphism, parentToGroupMeta, movedMorphisms);
    }

    private JoinProperties createJoinOperation(
        Key parentKey, List<SourcePropertySpec> sourceSpecs,
        TargetPropertySpec targetSpec, String delimiter,
        boolean deleteSources, MetadataCategory metadata
    ) {
        final var sourceProperties = sourceSpecs.stream().map(spec -> {
            final var objexKey = new Key(spec.objexKey);
            final var sig = new BaseSignature(spec.morphismSig);
            return new JoinProperties.SourceProperty(
                new SerializedObjex(objexKey, ObjexIds.fromIds(SignatureId.empty())),
                new SerializedMetadataObjex(objexKey, spec.label, null),
                new SerializedMorphism(sig, parentKey, objexKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(sig, spec.label)
            );
        }).toList();

        final var targetKey = new Key(targetSpec.objexKey);
        final var targetSig = new BaseSignature(targetSpec.morphismSig);
        final var targetProperty = new JoinProperties.TargetProperty(
            new SerializedObjex(targetKey, ObjexIds.fromIds(SignatureId.empty())),
            new SerializedMetadataObjex(targetKey, targetSpec.label, null),
            new SerializedMorphism(targetSig, parentKey, targetKey, Min.ZERO, Set.of()),
            new SerializedMetadataMorphism(targetSig, targetSpec.label)
        );

        // Set metadata so propagator can resolve
        metadata.setObjex(targetKey, new cz.matfyz.core.metadata.MetadataObjex(
            targetSpec.label, cz.matfyz.core.metadata.MetadataObjex.Position.createDefault()));

        return new JoinProperties(parentKey, sourceProperties, targetProperty, delimiter, deleteSources);
    }

    private String statementsToString(List<AbstractStatement> statements) {
        final var sb = new StringBuilder();
        for (final var stmt : statements) {
            sb.append(stmt.getContent()).append("\n");
        }
        return sb.toString();
    }
}
