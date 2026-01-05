package cz.matfyz.querying.validator;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.normalizer.NormalizedQuery;
import cz.matfyz.querying.normalizer.VariableTree;

import java.util.HashSet;

/** Validators help identify errors with a more helpful message than a generic technical error. */
public class NormalizedQueryValidator {
    private NormalizedQueryValidator() { }

    public static void run(NormalizedQuery normalizedQuery, SchemaCategory schema) {
        final var rootVarTree = normalizedQuery.selection.variables();
        failOnNonPropertyLeaves(rootVarTree, schema);
        failOnDupicateObjexInSelection(rootVarTree, schema, new HashSet<>());
    }

    /**
     * It does not make sense to only match an entity without any property leaves (and the further stages cannot handle it anyways), so this reports the error immediately.
     */
    private static void failOnNonPropertyLeaves(VariableTree varTree, SchemaCategory schema) {
        GraphUtils.forEachDFS(varTree, node -> {
            if (node.children().isEmpty() && getObjex(node, schema).isEntity()) {
                throw new UnsupportedOperationException(
                    "Variable \"" + node.variable + "\" is a non-property query leaf, which is forbidden. Perhaps the signature is incomplete?"
                );
            }
        });
    }

    /**
     * Right now, duplicate objexes as distinct variables, e.g. when selecting over a loop in the graph, are unsupported. TODO: Remove this once it is implemented.
     */
    private static void failOnDupicateObjexInSelection(VariableTree varTree, SchemaCategory schema, HashSet<SchemaObjex> trackedObjexes) {
        GraphUtils.forEachDFS(varTree, node -> {
            if (!trackedObjexes.add(getObjex(node, schema))) {
                throw new UnsupportedOperationException(
                    "Objex of variable \"" + node.variable + "\" is selected multiple times, which is not supported yet."
                );
            }
        });
    }

    private static SchemaObjex getObjex(VariableTree varTree, SchemaCategory schema) {
        // NOTE: I assume a variable has at least one child or parent, because otherwise it's useless
        return varTree.edgeFromParent != null
            ? schema.getEdge(varTree.edgeFromParent).to()
            : schema.getEdge(varTree.children().iterator().next().edgeFromParent).from();
    }

}
