package cz.cuni.matfyz.querying.core;

import cz.cuni.matfyz.querying.parsing.Query;
import cz.cuni.matfyz.querying.parsing.Statement;

import java.util.List;

public class QueryPlan {

    public final Query query;
    public final List<QueryPart> parts;
    public final List<Statement> deferredStatements;
    public final int cost;

    public QueryPlan(Query query, List<QueryPart> parts, List<Statement> deferredStatements, int cost) {
        this.query = query;
        this.parts = parts;
        this.deferredStatements = deferredStatements;
        this.cost = cost;
    }

}