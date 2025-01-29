package cz.matfyz.querying.resolver;

import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.normalizer.ExpressionTree;
import cz.matfyz.querying.normalizer.NormalizedQuery.ProjectionClause;
import cz.matfyz.querying.resolver.queryresult.ResultStructureTformer;
import cz.matfyz.querying.resolver.queryresult.TformContext;
import cz.matfyz.querying.resolver.queryresult.TformingResultStructure;
import cz.matfyz.querying.resolver.queryresult.TformStep.TformRoot;

public class ProjectionResolver {

    public static QueryResult run(QueryContext context, ProjectionClause clause, QueryResult selection) {
        return new ProjectionResolver(context, clause, selection).run();
    }

    private final QueryContext context;
    private final ProjectionClause clause;
    private final QueryResult selection;

    private ProjectionResolver(QueryContext context, ProjectionClause clause, QueryResult selection) {
        this.context = context;
        this.clause = clause;
        this.selection = selection;
    }

    private QueryResult run() {
        final TformingResultStructure projectionStructure = computeProjectionStructure();
        final TformRoot tform = ResultStructureTformer.run(selection.structure, projectionStructure);
        final var tformContext = new TformContext(selection.data);

        tform.apply(tformContext);

        final ListResult data = (ListResult) tformContext.getOutput();

        return new QueryResult(data, projectionStructure.toResultStructure());
    }

    private TformingResultStructure computeProjectionStructure() {
        final Variable rootVariable = clause.properties().asVariable();
        final var projectionStructure = new TformingResultStructure(rootVariable.name(), rootVariable.name(), context.getObjexForVariable(rootVariable));
        addChildrenToStructure(clause.properties(), projectionStructure);

        return projectionStructure;
    }

    private void addChildrenToStructure(ExpressionTree parent, TformingResultStructure parentStructure) {
        for (final ExpressionTree child : parent.children()) {
            // We don't know (yet) if the structure is supposed to be an array. We will figure it out later during the transformation.
            // Like we can find out now, but that would require doing the whole tree search again.
            if (child.expression instanceof final Variable variable) {
                final var childStructure = new TformingResultStructure(variable.name(), child.edgeFromParent, context.getObjexForVariable(variable));
                parentStructure.children.add(childStructure);
                addChildrenToStructure(child, childStructure);
            }

            // TODO Aggregation and string values - this would require extending transformations since they don't yet support these.
            // new TformingResultStructure(child.expression.identifier(), child.edgeFromParent, context.getObjexForVariable(child.expression))
        }
    }

}
