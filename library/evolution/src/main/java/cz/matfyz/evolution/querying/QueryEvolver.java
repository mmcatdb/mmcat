package cz.matfyz.evolution.querying;

import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.querying.QueryUpdateResult.ErrorType;
import cz.matfyz.evolution.querying.QueryUpdateResult.QueryUpdateError;
import cz.matfyz.evolution.schema.Composite;
import cz.matfyz.evolution.schema.CreateMorphism;
import cz.matfyz.evolution.schema.CreateObject;
import cz.matfyz.evolution.schema.DeleteMorphism;
import cz.matfyz.evolution.schema.DeleteObject;
import cz.matfyz.evolution.schema.EditMorphism;
import cz.matfyz.evolution.schema.EditObject;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.evolution.schema.SchemaEvolutionVisitor;
import cz.matfyz.querying.parsing.Filter.ConditionFilter;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;
import cz.matfyz.querying.parsing.SelectClause;
import cz.matfyz.querying.parsing.Term;
import cz.matfyz.querying.parsing.TermTree;
import cz.matfyz.querying.parsing.WhereClause;
import cz.matfyz.querying.parsing.Filter.ValueFilter;
import cz.matfyz.querying.parsing.WhereClause.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryEvolver implements SchemaEvolutionVisitor<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryEvolver.class);

    private SchemaCategory prevCategory;
    private SchemaCategory nextCategory;
    private List<SchemaCategoryUpdate> updates;

    public QueryEvolver(SchemaCategory prevCategory, SchemaCategory nextCategory, List<SchemaCategoryUpdate> updates) {
        this.prevCategory = prevCategory;
        this.nextCategory = nextCategory;
        this.updates = updates;
    }

    public QueryUpdateResult run(String prevContent) {
        try {
            return innerRun(prevContent);
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private Query query;
    private List<TermTree<String>> selectTermTrees;
    private List<TermTree<Signature>> whereTermTrees;
    private List<ConditionFilter> conditionFilters;
    private List<ValueFilter> valueFilters;

    private List<QueryUpdateError> errors = new ArrayList<>();

    private QueryUpdateResult innerRun(String prevContent) throws Exception {
        query = QueryParser.parse(prevContent);
        selectTermTrees = new ArrayList<>(query.select.originalTermTrees);
        whereTermTrees = new ArrayList<>(query.where.originalTermTrees);
        conditionFilters = new ArrayList<>(query.where.conditionFilters);
        valueFilters = new ArrayList<>(query.where.valueFilters);

        for (final var update : updates) {
            LOGGER.info("Executing update from: " + update.getPrevVersion());
            for (final var operation : update.operations) {
                LOGGER.info("Operation: " + operation);
                operation.accept(this);
            }
        }

        final Query updatedQuery = new Query(
            new SelectClause(selectTermTrees),
            new WhereClause(
                Type.Where,
                List.of(),
                new Term.Builder(),
                whereTermTrees,
                conditionFilters,
                valueFilters
            ),
            new QueryContext()
        );

        final String newContent = QueryParser.write(updatedQuery);

        return new QueryUpdateResult(newContent, errors);
    }

    @Override
    public Void visit(Composite operation) {
        /* This function is intentionally empty. */
        return null;
    }

    @Override
    public Void visit(CreateMorphism operation) {
        /* This function is intentionally empty. */
        return null;
    }

    @Override
    public Void visit(CreateObject operation) {
        /* This function is intentionally empty. */
        return null;
    }

    @Override
    public Void visit(DeleteMorphism operation) {
        final Signature signatureToDelete = operation.morphism.signature();

        final var whereDeletor = new SubtreeDeletor<Signature>(tree -> tree.edgeFromParent != null && tree.edgeFromParent.contains(signatureToDelete));
        whereTermTrees = whereTermTrees.stream()
            .map(whereDeletor::run)
            .filter(Objects::nonNull)
            .toList();

        if (whereDeletor.deleted.isEmpty())
            return null;

        // TODO Don't know if this works. We should check whether the term is still contained in the where clause.
        final var selectDeletor = new SubtreeDeletor<String>(tree -> whereDeletor.deleted.stream().anyMatch(d -> d.term.equals(tree.term)));
        selectTermTrees = selectTermTrees.stream()
            .map(selectDeletor::run)
            .filter(Objects::nonNull)
            .toList();

        errors.add(new QueryUpdateError(ErrorType.UpdateWarning, "Query was changed because of delete morphism " + operation.morphism.signature(), null));

        return null;
    }

    @Override
    public Void visit(DeleteObject operation) {
        final @Nullable Term termToDelete = query.context.getTerm(operation.object);
        if (termToDelete == null)
            return null;

        final var whereDeletor = new SubtreeDeletor<Signature>(tree -> tree.term.equals(termToDelete));
        whereTermTrees = whereTermTrees.stream()
            .map(whereDeletor::run)
            .filter(Objects::nonNull)
            .toList();

        final var selectDeletor = new SubtreeDeletor<String>(tree -> tree.term.equals(termToDelete));
        selectTermTrees = selectTermTrees.stream()
            .map(selectDeletor::run)
            .filter(Objects::nonNull)
            .toList();

        final boolean isSomethingChanged = !whereDeletor.deleted.isEmpty() || !selectDeletor.deleted.isEmpty();
        if (isSomethingChanged)
            errors.add(new QueryUpdateError(ErrorType.UpdateWarning, "Query was changed because of delete object " + operation.object.key(), null));

        return null;
    }

    private static class SubtreeDeletor<TEdge> {

        public final List<TermTree<TEdge>> deleted = new ArrayList<>();
        private final Predicate<TermTree<TEdge>> predicate;

        SubtreeDeletor(Predicate<TermTree<TEdge>> predicate) {
            this.predicate = predicate;
        }

        public TermTree<TEdge> run(TermTree<TEdge> input) {
            deleted.clear();
            return innerRun(input);
        }

        private @Nullable TermTree<TEdge> innerRun(TermTree<TEdge> input) {
            if (predicate.test(input)) {
                addToDeleted(input);
                return null;
            }

            final int beforeDeleted = deleted.size();

            final List<TermTree<TEdge>> newChildren = input.children.stream()
                .map(child -> innerRun(child))
                .filter(Objects::nonNull)
                .toList();

            final boolean isNothingDeleted = beforeDeleted == deleted.size();
            if (isNothingDeleted)
                return input;

            final TermTree<TEdge> output = input.parent() == null
                ? TermTree.root(input.term.asVariable())
                : TermTree.child(input.term, input.edgeFromParent);

            newChildren.forEach(output::addChild);

            return output;
        }

        private void addToDeleted(TermTree<TEdge> termTree) {
            deleted.add(termTree);
            termTree.children.forEach(this::addToDeleted);
        }

    }

    @Override
    public Void visit(EditMorphism operation) {
        errors.add(new QueryUpdateError(ErrorType.UpdateError, "Unexpected error in the query", null));
        return null;
    }

    @Override
    public Void visit(EditObject operation) {
        errors.add(new QueryUpdateError(ErrorType.UpdateError, "Unexpected error in the query", null));
        return null;
    }

}
