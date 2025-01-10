package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.querying.algorithms.translator.QueryTranslator;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.QueryDescription;
import cz.matfyz.querying.core.QueryDescription.QueryPartDescription;
import cz.matfyz.querying.core.patterntree.PatternObject.SerializedPatternObject;
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
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.PatternNode.SerializedPatternNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryNode.SerializedQueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.core.querytree.UnionNode.SerializedUnionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class QueryDescriptor implements QueryVisitor<SerializedQueryNode> {

    public static QueryDescription run(QueryContext context, QueryNode rootNode) {
        return new QueryDescriptor(context, rootNode).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private final List<QueryPartDescription> parts = new ArrayList<>();

    private QueryDescriptor(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private QueryDescription run() {
        final SerializedQueryNode serialized = rootNode.accept(this);

        return new QueryDescription(parts, serialized);
    }

    public SerializedDatasourceNode visit(DatasourceNode node) {
        final QueryStatement query = QueryTranslator.run(context, node);
        parts.add(new QueryPartDescription(node.datasource.identifier, query.structure(), query.content().toString()));

        final var child = node.child.accept(this);

        return new SerializedDatasourceNode(child, node.datasource.identifier);
    }

    public SerializedPatternNode visit(PatternNode node) {
        final var serializedKinds = new TreeMap<String, SerializedPatternObject>();
        for (final var kindPattern : node.kinds)
            serializedKinds.put(kindPattern.kind.kindName(), kindPattern.root.serialize());

        return new SerializedPatternNode(
            serializedKinds,
            node.joinCandidates.stream().map(candidate -> candidate.serialize()).toList(),
            "TODO root term"
        );
    }

    public SerializedFilterNode visit(FilterNode node) {
        return new SerializedFilterNode(
            node.child.accept(this),
            "TODO filter"
        );
    }

    public SerializedJoinNode visit(JoinNode node) {
        return new SerializedJoinNode(
            node.fromChild.accept(this),
            node.toChild.accept(this),
            node.candidate.serialize()
        );
    }

    public SerializedMinusNode visit(MinusNode node) {
        return new SerializedMinusNode(
            node.primaryChild.accept(this),
            node.minusChild.accept(this)
        );
    }

    public SerializedOptionalNode visit(OptionalNode node) {
        return new SerializedOptionalNode(
            node.primaryChild.accept(this),
            node.optionalChild.accept(this)
        );
    }

    public SerializedUnionNode visit(UnionNode node) {
        return new SerializedUnionNode(
            node.children.stream().map(child -> child.accept(this)).toList()
        );
    }

}
