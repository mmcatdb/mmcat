package cz.cuni.matfyz.querying.core;

import cz.cuni.matfyz.querying.parsing.Statement;

import java.util.List;

public class QueryPart {

    public final List<TripleKind> triplesMapping;
    public final List<Statement> statements;
    public QueryPartCompiled compiled;

    public QueryPart(List<TripleKind> triplesMapping, List<Statement> statements) {
        this.triplesMapping = triplesMapping;
        this.statements = statements;
    }

}