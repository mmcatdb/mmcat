package cz.matfyz.querying.normalizer;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.normalizer.NormalizedQuery.ProjectionClause;
import cz.matfyz.querying.normalizer.NormalizedQuery.SelectionClause;
import cz.matfyz.querying.parser.Filter;
import cz.matfyz.querying.parser.ParsedQuery;
import cz.matfyz.querying.parser.SelectClause;
import cz.matfyz.querying.parser.TermTree;
import cz.matfyz.querying.parser.WhereClause;

import java.util.ArrayList;
import java.util.List;

public class QueryNormalizer {

    /**
     * Normalizes a parsed AST of MMQL query to a another AST that is easier to work with.
     */
    public static NormalizedQuery normalize(ParsedQuery parsed) {
        return new QueryNormalizer().run(parsed);
    }

    private ExpressionScope scope;

    private NormalizedQuery run(ParsedQuery parsed) {
        scope = parsed.scope;

        final var projection = normalizeProjectionClause(parsed.select);
        final var selection = normalizeSelectionClause(parsed.where);

        // FIXME QueryContext.
        final var context = new QueryContext(selection.variables());
        return new NormalizedQuery(projection, selection, context);
    }

    // Projection

    private ProjectionClause normalizeProjectionClause(SelectClause selectClause) {
        // Join all term trees into one.
        final TermTree<String> termTree = TermTree.createFromList(selectClause.termTrees);

        final Variable rootVariable = termTree.term.asVariable();
        final ExpressionTree properties = ExpressionTree.createRoot(rootVariable);
        for (final var child : termTree.children)
            addProjectionChildren(properties, child);

        return new ProjectionClause(properties);
    }

    private void addProjectionChildren(ExpressionTree parent, TermTree<String> termTree) {
        final var current = parent.createChild(termTree.term.asExpression(), termTree.edgeFromParent);
        for (final var child : termTree.children)
            addProjectionChildren(current, child);
    }

    // Selection

    private List<Computation> filters;

    private SelectionClause normalizeSelectionClause(WhereClause whereClause) {
        filters = new ArrayList<>();

        // Join all term trees into one.
        final TermTree<Signature> termTree = TermTree.createFromList(whereClause.termTrees);

        // TODO Remove redundant parts. Are there any?

        final Variable rootVariable = termTree.term.asVariable();
        final VariableTree variables = VariableTree.createRoot(rootVariable);
        for (final var child : termTree.children)
            addSelectionChildren(variables, child);

        addExplicitFilters(whereClause.filters);

        final var nestedClauses = whereClause.nestedClauses.stream().map(this::normalizeSelectionClause).toList();

        return new SelectionClause(
            whereClause.type,
            scope,
            variables,
            filters,
            nestedClauses
        );
    }

    /**
     * Transforms the original term tree to a tree of variables.
     * Compound morphisms (A) -x/y-> (B) are split into a sequence of morphisms (A) -x-> (new v) -y-> (B).
     * String values are transformed to filters.
     */
    private void addSelectionChildren(VariableTree parent, TermTree<Signature> termTree) {
        final var bases = termTree.edgeFromParent.toBases();
        var current = parent;

        // First n-1 variables are newly generated. Then there is the original variable / filter.
        for (int i = 0; i < bases.size() - 1; i++)
            current = current.getOrCreateChild(scope.variable.createGenerated(), bases.get(i));

        final var lastBase = bases.get(bases.size() - 1);

        // The original term is a string value - we transform it to a new variable and filter.
        if (termTree.term.isConstant()) {
            final var variable = scope.variable.createGenerated();
            current.getOrCreateChild(variable, lastBase);
            final var computation = scope.computation.create(Operator.Equal, variable, termTree.term.asConstant());
            filters.add(computation);

            // Also, this has to be a leaf, so we don't continue with children.
            return;
        }

        // The original term is a variable - we just add it.
        current = current.getOrCreateChild(termTree.term.asVariable(), lastBase);

        for (final var child : termTree.children)
            addSelectionChildren(current, child);
    }

    private void addExplicitFilters(List<Filter> parsedFilters) {
        parsedFilters.forEach(filter -> filters.add(filter.computation()));
    }

}
