package cz.matfyz.querying.validator;

import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.normalizer.NormalizedQuery;
import cz.matfyz.querying.normalizer.VariableTree;

import java.util.HashMap;
import java.util.HashSet;

/** Validators help identify errors with a more helpful message than a generic technical error. */
public class NormalizedQueryValidator {
    private NormalizedQueryValidator(NormalizedQuery query, SchemaCategory schema) {
        this.query = query;
        this.schema = schema;
    }

    private final HashMap<Variable, SchemaObjex> varToObjex = new HashMap<>();
    private final NormalizedQuery query;
    private final SchemaCategory schema;

    public static void run(NormalizedQuery normalizedQuery, SchemaCategory schema) {
        final var validator = new NormalizedQueryValidator(normalizedQuery, schema);

        validator.initVarToObjex();
        validator.failOnNonPropertyLeaves();
        validator.failOnDupicateObjexInSelection(new HashSet<>());
    }

    private void initVarToObjex() {
        GraphUtils.forEachDFS(query.selection.variables(), node -> {
            varToObjex.put(node.variable, varTreeToObjex(node));
        });
    }

    /**
     * It does not make sense to only match an entity without any property leaves (and the further stages cannot handle it anyways), so this reports the error immediately.
     */
    private void failOnNonPropertyLeaves() {
        GraphUtils.forEachDFS(query.projection.properties(), node -> {
            if (node.children().isEmpty() &&
                node.expression instanceof final Variable var &&
                varToObjex.get(var).isEntity()
            ) {
                throw new UnsupportedOperationException(
                    "Variable \"" + var + "\" is a non-property query leaf, which is forbidden. Perhaps the signature is incomplete?"
                );
            }
        });
    }

    /**
     * Right now, duplicate objexes as distinct variables, e.g. when selecting over a loop in the graph, are unsupported. TODO: Remove this once it is implemented.
     */
    private void failOnDupicateObjexInSelection(HashSet<SchemaObjex> trackedObjexes) {
        GraphUtils.forEachDFS(query.selection.variables(), node -> {
            if (!trackedObjexes.add(varTreeToObjex(node))) {
                throw new UnsupportedOperationException(
                    "Objex of variable \"" + node.variable + "\" is selected multiple times, which is not supported yet."
                );
            }
        });
    }

    private SchemaObjex varTreeToObjex(VariableTree node) {
        // NOTE: I assume a variable has at least one child or parent, because otherwise it's useless
        return node.edgeFromParent != null
            ? schema.getEdge(node.edgeFromParent).to()
            : schema.getEdge(node.children().iterator().next().edgeFromParent).from();
    }

}
