package cz.matfyz.querying.resolver;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.QueryDescription.QueryPartDescription;
import cz.matfyz.querying.core.QueryDescription.QueryPlanDescription;
import cz.matfyz.querying.core.patterntree.PatternTree.SerializedPatternTree;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.DatasourceNode.SerializedDatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.FilterNode.SerializedFilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.JoinNode.SerializedJoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.MinusNode.SerializedMinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.OptionalNode.SerializedOptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryNode.SerializedQueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.core.querytree.UnionNode.SerializedUnionNode;
import cz.matfyz.querying.planner.QueryPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class QueryPlanDescriptor implements QueryVisitor<SerializedQueryNode> {

    public static QueryPlanDescription run(QueryPlan plan) {
        return new QueryPlanDescriptor(plan.context, plan.root).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private final List<QueryPartDescription> parts = new ArrayList<>();

    private QueryPlanDescriptor(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private QueryPlanDescription run() {
        final SerializedQueryNode serialized = rootNode.accept(this);

        return new QueryPlanDescription(parts, serialized);
    }

    public SerializedDatasourceNode visit(DatasourceNode node) {
        final QueryStatement query = DatasourceTranslator.run(context, node);
        parts.add(new QueryPartDescription(node.datasource.identifier, query.structure(), query.content().toString()));

        final var serializedKinds = new TreeMap<String, SerializedPatternTree>();
        for (final var kindPattern : node.kinds)
            serializedKinds.put(kindPattern.kind.kindName(), kindPattern.root.serialize());

        return new SerializedDatasourceNode(
            node.datasource.identifier,
            serializedKinds,
            node.joinCandidates.stream().map(candidate -> candidate.serialize()).toList(),
            node.filters.stream().map(Computation::toString).toList(),
            "TODO root term");
    }

    public SerializedFilterNode visit(FilterNode node) {
        return new SerializedFilterNode(
            node.child().accept(this),
            node.filter.toString()
        );
    }

    public SerializedJoinNode visit(JoinNode node) {
        return new SerializedJoinNode(
            node.fromChild().accept(this),
            node.toChild().accept(this),
            node.candidate.serialize()
        );
    }

    public SerializedMinusNode visit(MinusNode node) {
        return new SerializedMinusNode(
            node.primaryChild().accept(this),
            node.minusChild().accept(this)
        );
    }

    public SerializedOptionalNode visit(OptionalNode node) {
        return new SerializedOptionalNode(
            node.primaryChild().accept(this),
            node.optionalChild().accept(this)
        );
    }

    public SerializedUnionNode visit(UnionNode node) {
        return new SerializedUnionNode(
            node.children().stream().map(child -> child.accept(this)).toList()
        );
    }

}
