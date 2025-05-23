package cz.matfyz.querying;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.QueryDescription;
import cz.matfyz.querying.normalizer.NormalizedQuery;
import cz.matfyz.querying.normalizer.QueryNormalizer;
import cz.matfyz.querying.optimizer.QueryOptimizer;
import cz.matfyz.querying.parser.ParsedQuery;
import cz.matfyz.querying.parser.QueryParser;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.planner.QueryPlanner;
import cz.matfyz.querying.planner.ResultStructureResolver;
import cz.matfyz.querying.resolver.QueryPlanDescriptor;
import cz.matfyz.querying.resolver.ProjectionResolver;
import cz.matfyz.querying.resolver.SelectionResolver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a MMQL `queryString`, execute this query against the given `schemaCategory`.
 * Returns an instance category with the results of the query.
 */
public class QueryToInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryToInstance.class);

    private final ControlWrapperProvider provider;
    private final SchemaCategory schema;
    private final String queryString;
    private final List<Mapping> kinds;

    public QueryToInstance(ControlWrapperProvider provider, SchemaCategory schema, String queryString, List<Mapping> kinds) {
        this.provider = provider;
        this.schema = schema;
        this.queryString = queryString;
        this.kinds = kinds;
    }

    public ListResult execute() {
        try {
            return innerExecute();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            LOGGER.error("execute", e);
            throw new OtherException(e);
        }
    }

    private ListResult innerExecute() {
        final ParsedQuery parsed = QueryParser.parse(queryString);
        final NormalizedQuery normalized = QueryNormalizer.normalize(parsed);

        normalized.context.setProvider(provider);

        final QueryPlan planned = QueryPlanner.run(normalized.context, schema, kinds, normalized.selection);
        ResultStructureResolver.run(planned);

        final QueryPlan optimized = QueryOptimizer.run(planned);

        final QueryResult selected = SelectionResolver.run(optimized);
        final QueryResult projected = ProjectionResolver.run(normalized.context, normalized.projection, selected);

        return projected.data;
    }

    public QueryDescription describe() {
        try {
            return innerDescribe();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            LOGGER.error("describe", e);
            throw new OtherException(e);
        }
    }

    private QueryDescription innerDescribe() {
        final ParsedQuery parsed = QueryParser.parse(queryString);
        final NormalizedQuery normalized = QueryNormalizer.normalize(parsed);

        normalized.context.setProvider(provider);

        final QueryPlan planned = QueryPlanner.run(normalized.context, schema, kinds, normalized.selection);
        ResultStructureResolver.run(planned);
        final var plannedDescription = QueryPlanDescriptor.run(planned);

        final QueryPlan optimized = QueryOptimizer.run(planned);
        final var optimizedDescription = QueryPlanDescriptor.run(optimized);

        return new QueryDescription(plannedDescription, optimizedDescription);
    }

}
