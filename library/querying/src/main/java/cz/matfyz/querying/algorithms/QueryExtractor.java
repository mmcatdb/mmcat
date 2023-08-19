package cz.matfyz.querying.algorithms;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.core.KindDefinition;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class extract a subset of schema category based on the query pattern. It also modifies mappings by discarding unnecessary objects.
 */
public class QueryExtractor {

    private final SchemaCategory schema;
    private final List<KindDefinition> kinds;
    private final List<WhereTriple> pattern;

    public QueryExtractor(SchemaCategory schema, List<KindDefinition> kinds, List<WhereTriple> pattern) {
        this.schema = schema;
        this.kinds = kinds;
        this.pattern = pattern;
    }

    public static record Result(
        SchemaCategory schema,
        List<KindDefinition> kinds
    ) {}

    public Result run() {
        createNewSchema();
        final var newMappings = createNewMappings();

        return new Result(newSchema, newMappings);
    }

    private SchemaCategory newSchema;
    private Queue<SchemaMorphism> morphismQueue;

    private void createNewSchema() {
        newSchema = new SchemaCategory(schema.label);
        // The triples already contain only base signatures.
        final var morphismsToAdd = pattern.stream().map(triple -> schema.getMorphism(triple.signature)).toList();
        morphismQueue = new LinkedList<>(morphismsToAdd);

        // We have to use queue because the morphisms need to add objects which need to add their ids which consist of objects and morphisms ... so we have to break the chain somewhere.
        while (!morphismQueue.isEmpty())
            addMorphism(morphismQueue.poll());
    }

    private void addMorphism(SchemaMorphism morphism) {
        if (newSchema.hasMorphism(morphism.signature()))
            return;

        newSchema.addMorphism(morphism);
        addObject(morphism.dom());
        addObject(morphism.cod());
    }

    private void addObject(SchemaObject object) {
        if (newSchema.hasObject(object.key()))
            return;

        newSchema.addObject(object);
        object.ids().toSignatureIds()
            .stream().flatMap(id -> id.signatures().stream())
            .flatMap(signature -> signature.toBases().stream())
            .forEach(base -> morphismQueue.add(schema.getMorphism(base)));
    }

    private List<KindDefinition> createNewMappings() {
        return kinds.stream()
            .filter(kind -> newSchema.hasObject(kind.mapping.rootObject().key()))
            .map(kind -> {
                final var newAccessPath = purgeComplexProperty(kind.mapping.accessPath());
                final var newMapping = new Mapping(newSchema, kind.mapping.rootObject().key(), kind.mapping.kindName(), newAccessPath, kind.mapping.primaryKey());
                return new KindDefinition(newMapping, kind.databaseId, kind.wrapper);
            }).toList();
    }

    private ComplexProperty purgeComplexProperty(ComplexProperty path) {
        final var newSubpaths = path.subpaths().stream()
            .filter(subpath -> newSchema.hasMorphism(subpath.signature()))
            .map(subpath -> subpath instanceof ComplexProperty complex
                ? purgeComplexProperty(complex)
                : subpath // Simple subpaths can be simply reused.
            ).toList();

        return new ComplexProperty(path.name(), path.signature(), path.isAuxiliary(), newSubpaths);
    }

}