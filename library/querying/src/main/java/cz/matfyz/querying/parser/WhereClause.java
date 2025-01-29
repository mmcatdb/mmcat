package cz.matfyz.querying.parser;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.querying.parser.Filter.ConditionFilter;
import cz.matfyz.querying.parser.Filter.ValueFilter;

import java.util.List;

public class WhereClause implements ParserNode {

    public enum ClauseType {
        Where,
        Optional,
        Minus,
        Union,
    }

    public final ClauseType type;
    public final List<WhereClause> nestedClauses;

    /** This is the terms as the user intended. It's a list, because the user can input them this way. */
    public final List<TermTree<Signature>> termTrees;

    public final List<ConditionFilter> conditionFilters;
    public final List<ValueFilter> valueFilters;

    public WhereClause(
        ClauseType type,
        List<WhereClause> nestedClauses,
        List<TermTree<Signature>> termTrees,
        List<ConditionFilter> conditionFilters,
        List<ValueFilter> valueFilters
    ) {
        this.type = type;
        this.nestedClauses = nestedClauses;

        this.termTrees = termTrees;

        this.conditionFilters = conditionFilters;
        this.valueFilters = valueFilters;
    }

}
