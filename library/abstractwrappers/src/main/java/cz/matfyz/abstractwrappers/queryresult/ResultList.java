package cz.matfyz.abstractwrappers.queryresult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ResultList implements ResultNode {

    public final List<? extends ResultNode> children;

    public ResultList(List<? extends ResultNode> children) {
        this.children = children;
    }

    public static class TableBuilder {

        private List<String> columns = new ArrayList<>();
        private List<List<String>> rows = new ArrayList<>();

        public TableBuilder addColumn(String name) {
            columns.add(name);

            return this;
        }

        public TableBuilder addColumns(List<String> names) {
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