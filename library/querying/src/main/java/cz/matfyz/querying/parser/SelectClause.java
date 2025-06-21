package cz.matfyz.querying.parser;

import java.util.List;

public class SelectClause implements ParserNode {

    public final List<TermTree<String>> termTrees;

    public SelectClause(List<TermTree<String>> termTrees) {
        this.termTrees = termTrees;
    }

}
