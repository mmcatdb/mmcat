package cz.matfyz.querying.core;

import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.Statement;

import java.util.List;

@Deprecated
public class QueryPlan {

    public final Query query;
    public final List<QueryPart_old> parts;
    public final List<Statement> deferredStatements;
    public final int cost;

    public QueryPlan(Query query, List<QueryPart_old> parts, List<Statement> deferredStatements, int cost) {
        this.query = query;
        this.parts = parts;
        this.deferredStatements = deferredStatements;
        this.cost = cost;
    }

}