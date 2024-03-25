package cz.matfyz.evolution.querying;

import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
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
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.parsing.ConditionFilter;
import cz.matfyz.querying.parsing.GroupGraphPattern;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;
import cz.matfyz.querying.parsing.SelectClause;
import cz.matfyz.querying.parsing.SelectTriple;
import cz.matfyz.querying.parsing.ValueFilter;
import cz.matfyz.querying.parsing.WhereClause;
import cz.matfyz.querying.parsing.WhereTriple;
import cz.matfyz.querying.parsing.ParserNode.TermBuilder;
import cz.matfyz.querying.parsing.WhereClause.Type;

import java.util.ArrayList;
import java.util.List;

public class QueryEvolver implements SchemaEvolutionVisitor<Void> {

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

    private List<SelectTriple> selectTriples;
    private List<WhereTriple> whereTriples;
    private List<ConditionFilter> conditionFilters;
    private List<ValueFilter> valueFilters;

    private List<QueryUpdateError> errors;

    private QueryUpdateResult innerRun(String prevContent) throws Exception {
        final Query parsedQuery = QueryParser.parse(prevContent);
        selectTriples = new ArrayList<>(parsedQuery.select.triples);
        whereTriples = new ArrayList<>(parsedQuery.where.pattern.triples);
        conditionFilters = new ArrayList<>(parsedQuery.where.pattern.conditionFilters);
        valueFilters = new ArrayList<>(parsedQuery.where.pattern.valueFilters);

        for (final var update : updates) {
            for (final var operation : update.operations) {
                operation.accept(this);
            }
        }

        final Query updatedQuery = new Query(
            new SelectClause(selectTriples),
            new WhereClause(
                Type.Where,
                new GroupGraphPattern(whereTriples, conditionFilters, valueFilters, new TermBuilder()),
                List.of()
            ),
            new QueryContext()
        );

        final String newContent = QueryParser.write(updatedQuery);

        return new QueryUpdateResult(newContent, List.of(
            new QueryUpdateError(ErrorType.UpdateError, "Unexpected error in the query", null)
        ));
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
        errors.add(new QueryUpdateError(ErrorType.UpdateError, "Unexpected error in the query", null));
        return null;
    }

    @Override
    public Void visit(DeleteObject operation) {
        errors.add(new QueryUpdateError(ErrorType.UpdateError, "Unexpected error in the query", null));
        return null;
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
