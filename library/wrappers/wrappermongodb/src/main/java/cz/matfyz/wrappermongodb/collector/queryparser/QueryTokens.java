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
     * Method for iterating over functions
     * @return true if there is more functions
     */
    public boolean moveNext() {
        _index += 1;
        return _index < functionTokens.length;
    }

    /**
     * Method for getting actual function
     * @return instance of function
     */
    public FunctionItem getActualFunction() {
        return functionTokens[_index];
    }

    /**
     * Method for parsing QueryTokens to string, which can be printed to console.
     * Used mainly for debugging purposes
     * @return string representation of tokens
     */
    @Override
    public String toString() {
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

    /**
     * Class representing builder for QueryTokens
     */
    public static class Builder {
        private String _db;
        private String _collectionName;
        private final List<FunctionItem> _functionTokens;


        /**
         * Method for parsing string token to function token
         * @param token string representation of function as user wrote it to query
         * @return parsed function as FunctionItem
         */
        private static FunctionItem _parseToFunctionItem(String token) {
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

        public Builder() {
            _functionTokens = new ArrayList<>();
        }

        /**
         * Public Method for adding new token to QueryTokens
         * @param token string token from user
         */
        public void addToken(String token) {
            if (_db == null)
                _db = token;
            else if (_collectionName == null) {
                if (token.startsWith("getCollection(")) {
                    var item = _parseToFunctionItem(token);
                    _collectionName = item.args.getString(0);
                } else {
                    _collectionName = token;
                }
            } else {
                _functionTokens.add(_parseToFunctionItem(token));
            }
        }

        /**
         * Builder method which creates instance of QueryTokens from this builder
         * @return instance of QueryTokens
         */
        public QueryTokens toTokens() {
            return new QueryTokens(
                    _db,
                    _collectionName,
                    _functionTokens.toArray(FunctionItem[]::new)
            );
        }
    }
}
