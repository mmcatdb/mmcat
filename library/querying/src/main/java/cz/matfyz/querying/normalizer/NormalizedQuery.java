package cz.matfyz.querying.normalizer;

import cz.matfyz.core.querying.Expression.FunctionExpression;
import cz.matfyz.core.querying.Variable.VariableScope;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.parser.WhereClause.ClauseType;

import java.util.List;

public class NormalizedQuery {

    public final ProjectionClause projection;
    public final SelectionClause selection;
    public final QueryContext context;

    public NormalizedQuery(ProjectionClause projection, SelectionClause selection, QueryContext context) {
        this.projection = projection;
        this.selection = selection;
        this.context = context;
    }

    public record ProjectionClause(
        ExpressionTree properties
    ) {}

    public record SelectionClause(
        ClauseType type,
        VariableScope variableScope,
        VariableTree variables,
        List<FunctionExpression> filters,
        List<SelectionClause> nestedClauses
    ) {}

}
