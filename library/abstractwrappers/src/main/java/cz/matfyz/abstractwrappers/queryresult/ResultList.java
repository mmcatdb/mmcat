package cz.matfyz.abstractwrappers.queryresult;

import cz.matfyz.core.utils.IndentedStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ResultList implements ResultNode {

    // TODO nad některými sloupci vytvořit stromy pro rychlejší joinování
        // - resp. vytvořit je, když jsou potřeba, a pak je nějak udržovat

    public final List<? extends ResultNode> children;

    public ResultList(List<? extends ResultNode> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        final var isMultilined = children.stream().anyMatch(child -> !(child instanceof ResultLeaf));
        final var builder = new StringBuilder();

        if (isMultilined) {
            builder.append("[\n");
            
            final var nestedBuilder = new IndentedStringBuilder(1);
            for (final var child : children) {
                nestedBuilder
                    .append(child)
                    .append(",\n");
            }

            builder
                .append(nestedBuilder)
                .append("\n]");
        }
        else {
            builder.append("[ ");
            final String childStrings = children.stream()
                .map(child -> child.toString())
                .collect(Collectors.joining(", "));
            builder
                .append(childStrings)
                .append(" ]");
        }

        return builder.toString();
    }

    public static class TableBuilder {

        private List<String> columns = new ArrayList<>();
        private List<List<String>> rows = new ArrayList<>();

        public TableBuilder addColumn(String name) {
            columns.add(name);

            return this;
        }

        public TableBuilder addColumns(Collection<String> names) {
            columns.addAll(names);

            return this;
        }

        public TableBuilder addRow(List<String> values) {
            rows.add(values);

            return this;
        }

        public ResultList build() {
            final List<ResultMap> children = new ArrayList<>();
            
            for (final List<String> row : rows) {
                final Map<String, ResultNode> map = new TreeMap<>();
                if (row.size() != columns.size())
                    throw new IllegalArgumentException("Row size does not match column size");
                
                for (int i = 0; i < columns.size(); i++)
                    map.put(columns.get(i), new ResultLeaf(row.get(i)));

                children.add(new ResultMap(map));
            }

            return new ResultList(children);
        }

    }
    
}