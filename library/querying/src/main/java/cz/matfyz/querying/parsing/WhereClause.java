package cz.matfyz.querying.parsing;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class WhereClause extends ParserNode {

    @Override WhereClause asWhereClause() {
        return this;
    }

    public static enum Type {
        Where,
        Optional,
        Minus,
        Union,
    }

    public final Type type;
    @Nullable
    public final GroupGraphPattern pattern;
    public final List<WhereClause> nestedClauses;

    public WhereClause(Type type, GroupGraphPattern pattern, List<WhereClause> nestedClauses) {
        this.type = type;
        this.pattern = pattern;
        this.nestedClauses = nestedClauses;
    }
    
}