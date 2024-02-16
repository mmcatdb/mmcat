package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.PatternObject;
import cz.matfyz.querying.core.patterntree.KindPattern;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.querying.parsing.WhereTriple;
import cz.matfyz.querying.parsing.ParserNode.Term;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 * This class extract a subset of schema category based on the query pattern. It also modifies mappings by discarding unnecessary objects.
 */
public class SchemaExtractor {

    public static ExtractorResult run(QueryContext context, SchemaCategory schema, List<Kind> kinds, List<WhereTriple> pattern) {
        return new SchemaExtractor(context, schema, kinds, pattern).run();
    }

    private final QueryContext context;
    private final SchemaCategory schema;
    private final List<Kind> kinds;
    private final List<WhereTriple> pattern;

    private SchemaExtractor(QueryContext context, SchemaCategory schema, List<Kind> kinds, List<WhereTriple> pattern) {
        this.context = context;
        this.schema = schema;
        this.kinds = kinds;
        this.pattern = pattern;
    }

    public record ExtractorResult(
        SchemaCategory schema,
        List<KindPattern> kindPatterns
    ) {}

    /**
     * List of all morphisms that appear directly in the pattern.
     * They already contain only base signatures without duals.
     */
    private List<SchemaMorphism> patternMorphisms;
    
    private ExtractorResult run() {
        patternMorphisms = pattern.stream().map(triple -> schema.getMorphism(triple.signature)).toList();

        createNewSchema();
        updateContext();
        final var patterns = createKindPatterns();
        // At this point, we can check whether the patterns cover all morphisms from the query. But it isn't necessary, because if some morphisms aren't covered, the QueryPlanner shouldn't be able to create any plan.

        return new ExtractorResult(newSchema, patterns);
    }

    // The schema category of all objects and morphisms that are reachable from the pattern plus those that are needed to identify the objects.
    private SchemaCategory newSchema;
    private Queue<SchemaMorphism> morphismQueue;

    private void createNewSchema() {
        newSchema = new SchemaCategory(schema.label);
        morphismQueue = new ArrayDeque<>(patternMorphisms);

        // We have to use queue because the morphisms need to add objects which need to add their ids which consist of objects and morphisms ... so we have to break the chain somewhere.
        while (!morphismQueue.isEmpty())
            addMorphism(morphismQueue.poll());
    }

    private void addMorphism(SchemaMorphism morphism) {
        // There are no duals in the queue on the start and we are not adding them during the process. So this is safe.
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
            // We don't have to worry about duals here because ids can't contain them (ids have to have cardinality at most 1).
            .forEach(base -> morphismQueue.add(schema.getMorphism(base)));
    }

    // TODO the whole context probably isn't needed anymore
    private Map<BaseSignature, WhereTriple> signatureToTriple = new TreeMap<>();
    private Map<Key, Term> keyToTerm = new TreeMap<>();

    private void updateContext() {
        pattern.forEach(triple -> {
            final var morphism = newSchema.getMorphism(triple.signature);
            signatureToTriple.put(triple.signature, triple);

            context.defineVariable(triple.subject, morphism.dom());
            if (triple.object instanceof Variable variableObject)
                context.defineVariable(variableObject, morphism.cod());

            keyToTerm.put(morphism.dom().key(), triple.subject);
            keyToTerm.put(morphism.cod().key(), triple.object);
        });
    }

    private List<KindPattern> createKindPatterns() {
        return kinds.stream()
            .filter(kind -> newSchema.hasObject(kind.mapping.rootObject().key()))
            .map(kind -> {
                final var rootObject = kind.mapping.rootObject();
                final var rootNode = PatternObject.createRoot(rootObject, keyToTerm.get(rootObject.key()));
                processComplexProperty(rootNode, kind.mapping.accessPath());

                return new KindPattern(kind, rootNode);
            }).toList();
    }

    private void processComplexProperty(PatternObject node, ComplexProperty path) {
        path.subpaths().stream()
            .forEach(subpath -> {
                // TODO - is this going to work? Because it might not be possible to browse a database with composed signatures only.
                // if (!(subpath.signature() instanceof BaseSignature baseSignature))
                //     return;

                var currentNode = node;
                for (final BaseSignature baseSignature : subpath.signature().toBases()) {
                    final var nonDualSignature = baseSignature.toNonDual();
                    if (!newSchema.hasMorphism(nonDualSignature))
                        return;

                    currentNode = currentNode.getOrCreateChild(schema.getEdge(baseSignature), signatureToTriple.get(nonDualSignature));
                }

                if (subpath instanceof ComplexProperty complex)
                    processComplexProperty(currentNode, complex);
            });
    }

}