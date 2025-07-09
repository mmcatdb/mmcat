package cz.matfyz.querying.optimizer;

import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;

import java.text.NumberFormat;
import java.util.Locale;

import scala.collection.mutable.StringBuilder;

public final class QueryDebugPrinter implements QueryVisitor<Void> {

    private static final NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);

    private final StringBuilder stringBuilder = new StringBuilder();
    private int indent = 0;

    public static String run(QueryNode node) {
        final var qdp = new QueryDebugPrinter();
        final var sb = qdp.stringBuilder;
        node.accept(qdp);
        return sb.substring(0, sb.length() - 1); // remove last newline
    }

    private void indentAccept(QueryNode child) { indent++; child.accept(this); indent--; }
    private String indent() { return "  ".repeat(indent); }

    @Override
    public Void visit(DatasourceNode node) {
        stringBuilder
            .append(indent())
            .append("DATASRC[")
            .append(node.datasource.type.name())
            .append("](cost ")
            .append(fmt.format(node.costData.network()))
            .append(")( ")
            .append(String.join(
                " JOIN ", node.kinds.stream().map(k -> k.kind.kindName()).toList()
            ))
            .append(" )\n");
        return null;
    }

    @Override
    public Void visit(FilterNode node) {
        stringBuilder
            .append(indent())
            .append("FILTER(cost ")
            .append(fmt.format(node.costData.network()))
            .append(")(\n");
        indentAccept(node.child());
        stringBuilder.append(indent()).append(")\n");
        return null;
    }

    @Override
    public Void visit(JoinNode node) {
        stringBuilder
            .append(indent())
            .append("JOIN(cost")
            .append(fmt.format(node.costData.network()))
            .append(")(\n");

        stringBuilder.append(indent()).append("FROM\n");
        indentAccept(node.fromChild());

        stringBuilder.append(indent()).append("TO\n");
        indentAccept(node.toChild());

        stringBuilder.append(indent()).append(")\n");
        return null;
    }

    @Override
    public Void visit(MinusNode node) {
        stringBuilder.append(indent()).append("(\n");
        indentAccept(node.primaryChild());
        stringBuilder.append(indent()).append("MINUS\n");
        indentAccept(node.minusChild());
        stringBuilder.append(indent()).append(")\n");
        return null;
    }

    @Override
    public Void visit(OptionalNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(UnionNode node) {
        stringBuilder.append(indent()).append("(\n");
        indentAccept(node.children().stream().findFirst().get());

        node.children().stream().skip(1).forEach(child -> {
            stringBuilder.append(indent()).append("UNION\n");
            indentAccept(child);
        });

        stringBuilder.append(indent()).append(")\n");
        return null;
    }
}
