package cz.matfyz.querying.algorithms;

import cz.matfyz.core.category.Morphism.Min;
import cz.matfyz.core.category.Morphism.Tag;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.schema.SchemaGraph;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.core.Utils;
import cz.matfyz.querying.exception.ProjectionException;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.SelectTriple;
import cz.matfyz.querying.parsing.StringValue;
import cz.matfyz.querying.parsing.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class creates a mapping that corresponds to the SELECT part of the query. It should be then used in the DML algorithm to project the query results to a readable format like JSON. Aggregation functions and simillar are not supported yet. However, it should be easy to add them - we just need to add new properties that represent their results and compute them. Then we just use add them to the mapping and project them as usual.
 */
public class QueryMappingProjector {

    private Query query;
    private InstanceCategory whereInstance;
    private Map<String, SchemaObject> variableTypes;

    /**
     * Given a query `query` and the instance category `whereInstance` containing the results of the `WHERE` clause, perform projection to the final MMQL query result instance category.
     */
    public Mapping project(Query query, InstanceCategory whereInstance) {
        this.query = query;
        this.whereInstance = whereInstance;
        this.variableTypes = Utils.getVariableTypesFromQuery(query, whereInstance.schema);

        return createMapping();
    }

    private Signature.Generator signatureGenerator;
    private SchemaGraph selectGraph;
    private SchemaGraph whereGraph;

    /**
     * Creates a schema category that consists of only those objects and morphisms that have appeared in the where part of the query
     */
    private Mapping createMapping() {
        signatureGenerator = new Signature.Generator(whereInstance.schema.allMorphisms().stream().map(m -> m.signature()).toList());
        final var selectMorphisms = query.select.triples.stream().map(this::createSelectMorphism).toList();
        selectGraph = new SchemaGraph(selectMorphisms); // Proxy morphisms representing the select triples.
        if (!selectGraph.findIsDirectedTrees())
            throw ProjectionException.notTree();

        final var selectRoots = selectGraph.findRoots();
        if (selectRoots.size() != 1)
            throw ProjectionException.notSingleRoot(selectRoots);

        final var whereMorphisms = query.where.pattern.triples.stream()
            .map(triple -> triple.signature) // All signatures that appeared in the where clause
            .map(whereInstance.schema::getEdge) // Corresponding schema edges
            .map(SchemaEdge::morphism)
            .toList();
        whereGraph = new SchemaGraph(whereMorphisms);


        final var rootObject = selectRoots.get(0);
        final var accessPath = createAccessPath(rootObject);

        return new Mapping(whereInstance.schema, rootObject.key(), null, accessPath, List.of());
    }

    /**
     * Creates a proxy schema morphism representing given triple (with brand new base signature). It isn't a real schema morphism - its cardinality is probably wrong.
     * Nevertheless, such "morphism" can be used in some algorithms here.
     */
    private SchemaMorphism createSelectMorphism(SelectTriple triple) {
        final String objectName = triple.object instanceof Variable variable
            ? variable.name
            : ((StringValue) triple.object).value;

        final SchemaObject domObject = variableTypes.get(triple.subject.name);
        final SchemaObject codObject = variableTypes.get(objectName);

        return new SchemaMorphism.Builder()
            .label(triple.name)
            .tags(Set.of(Tag.projection))
            .fromArguments(signatureGenerator.next(), domObject, codObject, Min.ZERO);
    }

    private ComplexProperty createAccessPath(SchemaObject rootObject) {
        return ComplexProperty.createRoot(createSubpaths(rootObject));
    }

    private List<AccessPath> createSubpaths(SchemaObject parentObject) {
        return selectGraph.getChildren(parentObject).stream().map(morphism -> {
            var object = morphism.cod();
            var path = whereGraph.findPath(parentObject, object);
            if (path == null)
                throw ProjectionException.PathNotFound(parentObject, object);
                
            var signature = Signature.concatenate(path);
            var name = new StaticName(morphism.label);
            var subpaths = createSubpaths(object);

            return subpaths.isEmpty()
                ? new SimpleProperty(name, signature)
                : new ComplexProperty(name, signature, false, subpaths);
        }).toList();

    }

}