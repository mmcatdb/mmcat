package cz.matfyz.querying.parser;

import cz.matfyz.core.identifiers.Signature;

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

    public final List<Filter> filters;

    public WhereClause(
        ClauseType type,
        List<WhereClause> nestedClauses,
        List<TermTree<Signature>> termTrees,
        List<Filter> filters
    ) {
        this.type = type;
        this.nestedClauses = nestedClauses;

        this.termTrees = termTrees;

        this.filters = filters;
    }

}
