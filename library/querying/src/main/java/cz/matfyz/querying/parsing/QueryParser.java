package cz.matfyz.querying.parsing;

import cz.matfyz.core.utils.printable.Printer;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatLexer;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatParser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public abstract class QueryParser {

    private QueryParser() {}

    /**
     * Given a MMQL query in the form of a `queryString`, parse the query into the internal representation.
     */
    public static Query parse(String queryString) {
        final var inputStream = CharStreams.fromString(queryString);
        final var lexer = new QuerycatLexer(inputStream);
        final var stream = new CommonTokenStream(lexer);
        final var parser = new QuerycatParser(stream);
        final var tree = parser.query();
        final var visitor = new QueryVisitor();

        return (Query) visitor.visitQuery(tree);
    }

    public static String write(Query query) {
        final var printer = Printer.create();

        printer.append("SELECT {").down().nextLine();

        for (final var termTree : query.select.originalTermTrees)
            printer.append(termTree).nextLine();

        printer
            .remove().up().nextLine()
            .append("}").nextLine()
            .append("WHERE {").down().nextLine();

        for (final var termTree : query.where.originalTermTrees)
            printer.append(termTree).nextLine();

        printer.remove().up().nextLine().append("}");

        return printer.toString();
    }

}
