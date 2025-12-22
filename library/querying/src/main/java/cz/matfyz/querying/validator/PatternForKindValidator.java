package cz.matfyz.querying.validator;

import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.normalizer.NormalizedQuery;

import java.util.HashSet;
import java.util.List;

/** Checks that the PatternForKinds cover all Variables. */
public class PatternForKindValidator {
    private PatternForKindValidator() { }

    public static void run(NormalizedQuery.SelectionClause selection, List<PatternForKind> patterns) {
        final var variables = new HashSet<Variable>();
        for (final var pattern : patterns) {
            GraphUtils.forEachDFS(pattern.root, node -> {
                variables.add(node.variable);
            });
        }

        final var rootVarTree = selection.variables();
        GraphUtils.forEachDFS(rootVarTree, node -> {
            if (!variables.contains(node.variable)) {
                throw new UnsupportedOperationException(
                    "Unable to cover the query with the available kinds (variable \"" + node.variable + "\" is not covered)."
                );
            }
        });
    }

}
