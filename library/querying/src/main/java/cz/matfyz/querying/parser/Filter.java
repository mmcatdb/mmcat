package cz.matfyz.querying.parser;

import cz.matfyz.core.querying.Computation;

public record Filter(
    Computation computation
) implements ParserNode {

    @Override public Filter asFilter() {
        return this;
    }

}
