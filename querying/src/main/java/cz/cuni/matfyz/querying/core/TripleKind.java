package cz.cuni.matfyz.querying.core;

import cz.cuni.matfyz.core.mapping.KindInstance;
import cz.cuni.matfyz.querying.parsing.WhereTriple;

public class TripleKind {

    public final WhereTriple triple;
    public final KindInstance kind;

    public TripleKind(WhereTriple triple, KindInstance kind) {
        this.triple = triple;
        this.kind = kind;
    }

}