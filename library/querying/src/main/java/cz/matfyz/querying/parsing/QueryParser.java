package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.antlr4generated.QuerycatLexer;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatParser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public abstract class QueryParser {

    private QueryParser() {}

    /**
     * Given a MMQL query in the form of a `queryString`, parse the query into the internal representation.
     */
    public static Query run(String queryString) {
        var inputStream = CharStreams.fromString(queryString);
        var lexer = new QuerycatLexer(inputStream);
        var stream = new CommonTokenStream(lexer);
        var parser = new QuerycatParser(stream);
        var tree = parser.query();
        var visitor = new QueryVisitor();

        return (Query) visitor.visitQuery(tree);
    }

}
