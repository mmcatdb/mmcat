package cz.matfyz.querying.core;

import cz.matfyz.querying.parsing.Statement;

import java.util.List;

@Deprecated
public class QueryPart_old {

    public final List<TripleKind> triplesMapping;
    public final List<Statement> statements;
    public QueryPartCompiled compiled;

    public QueryPart_old(List<TripleKind> triplesMapping, List<Statement> statements) {
        this.triplesMapping = triplesMapping;
        this.statements = statements;
    }

}