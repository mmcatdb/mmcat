package cz.matfyz.querying.parser;

import cz.matfyz.querying.exception.ParsingException;

import java.io.Serializable;

public interface ParserNode extends Serializable {

    default Term asTerm() {
        throw ParsingException.wrongNode(Term.class, this);
    }

    default Filter asFilter() {
        throw ParsingException.wrongNode(Filter.class, this);
    }

}
