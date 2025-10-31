package cz.matfyz.querying.normalizer;

import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.querying.parser.WhereClause.ClauseType;

import java.util.List;

public class NormalizedQuery {

    public final ProjectionClause projection;
    public final SelectionClause selection;

    public NormalizedQuery(ProjectionClause projection, SelectionClause selection) {
        this.projection = projection;
        this.selection = selection;
    }

    public record ProjectionClause(
        ExpressionTree properties
    ) {}

    public record SelectionClause(
        ClauseType type,
        ExpressionScope scope,
        VariableTree variables,
        List<Computation> filters,
        List<SelectionClause> nestedClauses
    ) {}

}
