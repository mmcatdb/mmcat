package cz.matfyz.querying.algorithms;

import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.core.utils.GraphUtils.Edge;
import cz.matfyz.querying.algorithms.queryresult.QueryStructureTformer;
import cz.matfyz.querying.algorithms.queryresult.TformContext;
import cz.matfyz.querying.algorithms.queryresult.TformingQueryStructure;
import cz.matfyz.querying.algorithms.queryresult.TformStep.TformRoot;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.exception.ProjectingException;
import cz.matfyz.querying.parsing.Term.Aggregation;
import cz.matfyz.querying.parsing.SelectClause;
import cz.matfyz.querying.parsing.SelectClause.SelectTriple;
import cz.matfyz.querying.parsing.Term.Variable;

import java.util.List;

import cz.matfyz.querying.parsing.Term;

public class QueryProjector {

    public static QueryResult run(QueryContext context, SelectClause selectClause, QueryResult selection) {
        return new QueryProjector(context, selectClause, selection).run();
    }

    private final QueryContext context;
    private final SelectClause selectClause;
    private final QueryResult selection;

    private QueryProjector(QueryContext context, SelectClause selectClause, QueryResult selection) {
        this.context = context;
        this.selectClause = selectClause;
        this.selection = selection;
    }

    private QueryResult run() {
        final TformingQueryStructure projectionStructure = computeProjectionStructure();
        final TformRoot tform = QueryStructureTformer.run(selection.structure, projectionStructure);
        final var tformContext = new TformContext(selection.data);

        tform.apply(tformContext);

        final ResultList data = (ResultList) tformContext.getOutput();

        return new QueryResult(data, projectionStructure.toQueryStructure());
    }

    private List<SelectTriple> triples;

    private TformingQueryStructure computeProjectionStructure() {
        // TODO use selection term tree for this (in some way - we can't use just it, because we still need the original tripes because that's what the user wants).
        triples = selectClause.originalTermTrees.stream()
            .flatMap(tree -> tree.toTriples(SelectTriple::new).stream())
            .toList();
        final Variable rootVariable = findRootVariable();
        final var projectionStructure = new TformingQueryStructure(rootVariable.getIdentifier(), rootVariable.getIdentifier(), context.getObject(rootVariable));
        addChildrenToStructure(rootVariable, projectionStructure);

        return projectionStructure;
    }

    private record ProjectionEdge(Variable from, Term to, SelectTriple triple) implements Edge<Term> {}

    private Variable findRootVariable() {
        final var edges = triples.stream().map(t -> new ProjectionEdge(t.subject, t.object, t)).toList();
        final var components = GraphUtils.findComponents(edges);
        if (components.size() != 1)
            throw ProjectingException.notSingleComponent();

        final var roots = GraphUtils.findRoots(components.iterator().next());
        if (roots.size() != 1) {
            final List<SchemaObject> objects = roots.stream().map(node -> {
                if (node instanceof Variable variable)
                    return context.getObject(variable);
                else if (node instanceof Aggregation aggregation)
                    return context.getObject(aggregation.variable());
                else
                    // Select clause can't contain constants.
                    throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
            }).toList();

            throw ProjectingException.notSingleRoot(objects);
        }

        final Term rootTerm = roots.iterator().next();
        if (!(rootTerm instanceof Variable rootVariable))
            throw ProjectingException.notRootVariable(rootTerm);

        return rootVariable;
    }

    private void addChildrenToStructure(Variable parentVariable, TformingQueryStructure parentStructure) {
        triples.stream().filter(t -> t.subject.equals(parentVariable)).forEach(t -> {
            // We don't know (yet) if the structure is supposed to be an array. We fill figure it out later during the transformation.
            // Like we can find out now, but that would require doing the whole tree search again.
            final var childStructure = new TformingQueryStructure(t.object.getIdentifier(), t.name, context.getObject(t.object));
            parentStructure.children.add(childStructure);
            if (t.object instanceof Variable childVariable)
                addChildrenToStructure(childVariable, childStructure);
        });
    }

}
