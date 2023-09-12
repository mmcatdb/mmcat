package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.abstractwrappers.other.JsonDMLWrapper;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;
import cz.matfyz.transformations.algorithms.DMLAlgorithm;

import java.util.List;

/**
 * Given a MMQL `queryString`, execute this query against the given `schemaCategory`.
 * Returns an instance category with the results of the query.
 */
public class QueryToInstance {

    private String queryString;
    private SchemaCategory schema;
    private List<Kind> kinds;

    public void input(SchemaCategory category, String queryString, List<Kind> kinds) {
        this.schema = category;
        this.queryString = queryString;
        this.kinds = kinds;
    }

    public static record Result(
        InstanceCategory instanceCategory,
        List<String> jsonValues
    ) {}

    public Result algorithm() {
        final Query query = QueryParser.run(queryString);
        final var queryTree = QueryTreeBuilder.run(schema, kinds, query.where);
        final InstanceCategory whereInstance = QueryResolver.run(queryTree);

        final List<String> jsonResults = createJsonResults(query, whereInstance);

        return new Result(whereInstance, jsonResults);
    }

    private List<String> createJsonResults(Query query, InstanceCategory whereInstance) {
        final var projector = new QueryMappingProjector();
        final var projectionMapping = projector.project(query, whereInstance);

        final var dmlTransformation = new DMLAlgorithm();
        dmlTransformation.input(projectionMapping, whereInstance, new JsonDMLWrapper());

        return dmlTransformation.algorithm().stream().map(AbstractStatement::getContent).toList();
    }
    
}
