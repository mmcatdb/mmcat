package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.Term.Variable;

import java.util.List;

public class SelectClause implements ParserNode {

    public final List<TermTree<String>> originalTermTrees;
    public final TermTree<String> termTree;

    public SelectClause(List<TermTree<String>> originalTermTrees) {
        this.originalTermTrees = originalTermTrees;
        termTree = TermTree.fromList(originalTermTrees);
    }

    public static class SelectTriple {

        // TODO is this needed? Maybe we can just directly project from the trees ...

        public final Variable subject;
        public final String name;
        // This has to be either Variable or Aggregation.
        public final Term object;

        public SelectTriple(Variable subject, String name, Term object) {
            this.subject = subject;
            this.name = name;
            this.object = object;
        }

        @Override public String toString() {
            return subject.toString() + " " + name + " " + object.toString();
        }

    }

}
