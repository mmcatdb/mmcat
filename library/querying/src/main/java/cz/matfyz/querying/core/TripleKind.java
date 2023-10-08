package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.querying.parsing.WhereTriple;

@Deprecated
public class TripleKind {

    public final WhereTriple triple;
    public final Kind kind;

    public TripleKind(WhereTriple triple, Kind kind) {
        this.triple = triple;
        this.kind = kind;
    }

}