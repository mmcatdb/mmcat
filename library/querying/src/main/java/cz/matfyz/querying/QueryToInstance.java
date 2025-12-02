package cz.matfyz.querying;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.QueryDescription;
import cz.matfyz.querying.core.QueryExecution;
import cz.matfyz.querying.normalizer.NormalizedQuery;
import cz.matfyz.querying.normalizer.QueryNormalizer;
import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.querying.optimizer.QueryDebugPrinter;
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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a MMQL <code>queryString</code>, execute this query against the given <code>schemaCategory</code>.
 * Returns an instance category with the results of the query.
 */
public class QueryToInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryToInstance.class);

    private final ControlWrapperProvider provider;
    private final SchemaCategory schema;
    private final String queryString;
    private final List<Mapping> kinds;
    @Nullable private final CollectorCache cache;

    @Nullable private QueryPlan planned;

    public QueryToInstance(ControlWrapperProvider provider, SchemaCategory schema, String queryString, List<Mapping> kinds, CollectorCache cache) {
        this.provider = provider;
        this.schema = schema;
        this.queryString = queryString;
        this.kinds = kinds;
        this.cache = cache;
        this.planned = null;
    }

    public QueryExecution execute() {
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

    private QueryExecution innerExecute() {
        final long startNanos = System.nanoTime();

        final ParsedQuery parsed = QueryParser.parse(queryString);
        final NormalizedQuery normalized = QueryNormalizer.normalize(parsed);
        final var context = new QueryContext(schema, provider, normalized.selection.variables());

        planned = QueryPlanner.run(context, kinds, normalized.selection);
        ResultStructureResolver.run(planned);

        planned = QueryOptimizer.run(planned, cache);

        final long planningTimeInMs = Math.round((System.nanoTime() - startNanos) / 1_000_000.0);

        final QueryResult selected = SelectionResolver.run(planned, cache);
        final QueryResult projected = ProjectionResolver.run(context, normalized.projection, selected);

        final long evaluationTimeInMs = Math.round(planned.root.evaluationTimeInMs);

        LOGGER.info("Parsing & creating plans took {} ms", planningTimeInMs);
        LOGGER.info("Evaluated query took {} ms", evaluationTimeInMs);
        LOGGER.info("Detailed execution time info:\n{}", QueryDebugPrinter.measuredCost(planned.root));

        return new QueryExecution(
            projected.data,
            planningTimeInMs,
            evaluationTimeInMs
        );
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
        final var context = new QueryContext(schema, provider, normalized.selection.variables());

        final QueryPlan planned = QueryPlanner.run(context, kinds, normalized.selection);
        ResultStructureResolver.run(planned);
        final var plannedDescription = QueryPlanDescriptor.run(planned);

        final QueryPlan optimized = QueryOptimizer.run(planned, cache);
        final var optimizedDescription = QueryPlanDescriptor.run(optimized);

        return new QueryDescription(plannedDescription, optimizedDescription);
    }

    public QueryPlan getPlan() {
        return planned;
    }
}
