package cz.matfyz.wrappermongodb.collector.queryparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents query as parsed tokens
 */
public class QueryTokens {
    /** Field holding info about database name from query */
    public final String db;

    /** Field holding info about parsed collectionName on which is query pointed */
    public final String collectionName;

    /** list of all functions called in the query */
    public final FunctionItem[] functionTokens;

    /** pointer to actual function to be processed */
    private int _index;

    private QueryTokens(String db, String collectionName, FunctionItem[] functionTokens) {
        this.db = db;
        this.collectionName = collectionName;
        this.functionTokens = functionTokens;
        _index = -1;
    }

    /**
     * @return true if there is more functions
     */
    public boolean moveNext() {
        _index += 1;
        return _index < functionTokens.length;
    }

    public FunctionItem getActualFunction() {
        return functionTokens[_index];
    }

    @Override public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("QueryTokens:\n");

        buffer.append("- DB: ").append(db).append('\n');
        buffer.append("- Collection: ").append(collectionName).append('\n');
        buffer.append("- Functions:\n");

        for (var function : functionTokens) {
            buffer.append("  - name: ").append(function.name).append('\n');
            buffer.append("    - args: ").append(function.args.toString());
        }
        return buffer.toString();
    }

    public static class Builder {

        private String _db;
        private String _collectionName;
        private final List<FunctionItem> _functionTokens;

        public Builder() {
            _functionTokens = new ArrayList<>();
        }

        /** @param token string token from user */
        public void addToken(String token) {
            if (_db == null)
                _db = token;
            else if (_collectionName == null) {
                if (token.startsWith("getCollection(")) {
                    var item = parseToFunctionItem(token);
                    _collectionName = item.args.getString(0);
                } else {
                    _collectionName = token;
                }
            } else {
                _functionTokens.add(parseToFunctionItem(token));
            }
        }

        private static FunctionItem parseToFunctionItem(String token) {
            StringBuilder buffer = new StringBuilder();
            boolean inArgs = false;

            String name = null;
            String content = null;

            for (char ch : token.toCharArray()) {
                if (inArgs) {
                    if (ch == ')') {
                        content = buffer.toString().trim();
                        break;
                    } else {
                        buffer.append(ch);
                    }

                } else {
                    if (ch == '(') {
                        inArgs = true;
                        name = buffer.toString();
                        buffer.setLength(0);
                    } else {
                        buffer.append(ch);
                    }
                }
            }

            return new FunctionItem(name, ArgumentsArray.parseArguments(content));
        }

        public QueryTokens build() {
            return new QueryTokens(
                _db,
                _collectionName,
                _functionTokens.toArray(FunctionItem[]::new)
            );
        }

    }

}
