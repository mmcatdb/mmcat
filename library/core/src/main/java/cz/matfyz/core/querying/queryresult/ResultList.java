package cz.matfyz.core.querying.queryresult;

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

@JsonSerialize(using = ResultList.Serializer.class)
public class ResultList implements ResultNode {

    // TODO nad některými sloupci vytvořit stromy pro rychlejší joinování
        // - resp. vytvořit je, když jsou potřeba, a pak je nějak udržovat

    private final List<? extends ResultNode> children;

    public ResultList(List<? extends ResultNode> children) {
        this.children = children;
    }

    public List<? extends ResultNode> children() {
        return this.children;
    }

    @Override public void printTo(Printer printer) {
        final var isMultilined = children.stream().anyMatch(child -> !(child instanceof ResultLeaf));

        if (isMultilined) {
            printer.append("[").down().nextLine();

            for (final var child : children)
                printer.append(child).append(",").nextLine();

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

    public static class Serializer extends StdSerializer<ResultList> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ResultList> t) {
            super(t);
        }

        @Override public void serialize(ResultList resultList, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (final var child : resultList.children)
                generator.writeObject(child);
            generator.writeEndArray();
        }

    }

    public static class Builder<T extends ResultNode> implements NodeBuilder {

        private List<T> children = new ArrayList<>();

        public Builder<T> add(T node) {
            children.add(node);
            return this;
        }

        public ResultList build() {
            return new ResultList(children);
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
