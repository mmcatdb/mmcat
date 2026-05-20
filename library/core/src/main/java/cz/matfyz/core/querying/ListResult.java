package cz.matfyz.core.querying;

import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = ListResult.Serializer.class)
public class ListResult extends ResultNode {

    // TODO create indexes over some columns for faster joins
    // resp. create them when needed, and then maintain them somehow

    private List<? extends ResultNode> children;

    public ListResult(List<? extends ResultNode> children) {
        this.children = children;
    }

    public List<? extends ResultNode> children() {
        return this.children;
    }

    /** The indices have to be in ascending order. */
    public void removeChildren(List<Integer> sortedIndexes) {
        if (sortedIndexes.isEmpty())
            return;

        final var newChildren = new ArrayList<ResultNode>();
        int j = 0;

        for (int i = 0; i < children.size(); i++) {
            if (j < sortedIndexes.size() && i == sortedIndexes.get(j))
                j++;
            else
                newChildren.add(children.get(i));
        }

        this.children = newChildren;
    }

    @Override public void printTo(Printer printer) {
        final var isMultilined = children.stream().anyMatch(child -> !(child instanceof LeafResult));

        if (isMultilined) {
            printer.append("[").down().nextLine();

            for (final var child : children)
                printer.append(child).append(",").nextLine();

            if (!children.isEmpty())
                printer.remove();

            printer.remove().up().nextLine().append("]");
        }
        else {
            printer.append("[ ");
            for (final var child : children)
                printer.append(child).append(", ");

            if (!children.isEmpty())
                printer.remove();

            printer.append("]");
        }
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    public List<String> toJsonArray() {
        return children.stream().map(Object::toString).toList();
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<ListResult> {
        public Serializer() { this(null); }
        public Serializer(Class<ListResult> t) { super(t); }

        @Override public void serialize(ListResult result, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (final var child : result.children)
                generator.writeObject(child);
            generator.writeEndArray();
        }
    }

    // #endregion

    public static class Builder<T extends ResultNode> implements NodeBuilder {

        private List<T> children = new ArrayList<>();

        public Builder<T> add(T node) {
            children.add(node);
            return this;
        }

        public ListResult build() {
            return new ListResult(children);
        }

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

        public ListResult build() {
            final List<MapResult> children = new ArrayList<>();

            for (final List<String> row : rows) {
                final Map<String, ResultNode> map = new TreeMap<>();
                if (row.size() != columns.size())
                    throw new IllegalArgumentException("Row size does not match column size");

                for (int i = 0; i < columns.size(); i++)
                    map.put(columns.get(i), new LeafResult(row.get(i)));

                children.add(new MapResult(map));
            }

            return new ListResult(children);
        }

    }

}
