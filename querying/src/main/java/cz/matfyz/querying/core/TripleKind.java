package cz.matfyz.querying.core;

import cz.matfyz.core.mapping.KindInstance;
import cz.matfyz.querying.parsing.WhereTriple;

public class TripleKind {

    public final WhereTriple triple;
    public final KindInstance kind;

    public TripleKind(WhereTriple triple, KindInstance kind) {
        this.triple = triple;
        this.kind = kind;
    }

}