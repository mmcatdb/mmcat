package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.querying.parsing.ValueNode;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.List;

public class KindTree {

    public Kind kind() {
        throw new UnsupportedOperationException();
    }

    public WhereTriple rootTriple() {
        throw new UnsupportedOperationException();
    }

    public List<WhereTriple> getOutgoingTriples(ValueNode object) {
        throw new UnsupportedOperationException();
    }

    public boolean isOptional(WhereTriple triple) {
        throw new UnsupportedOperationException();
    }

}