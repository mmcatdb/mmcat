package cz.matfyz.querying.parser;

import cz.matfyz.core.utils.printable.Printer;
import cz.matfyz.querying.parser.antlr4generated.QuerycatLexer;
import cz.matfyz.querying.parser.antlr4generated.QuerycatParser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public abstract class QueryParser {

    private QueryParser() {}

    /**
     * Given a MMQL query in the form of a <code>queryString</code>, parse the query into AST.
     */
    public static ParsedQuery parse(String queryString) {
        final var inputStream = CharStreams.fromString(queryString);
        final var lexer = new QuerycatLexer(inputStream);
        final var stream = new CommonTokenStream(lexer);
        final var parser = new QuerycatParser(stream);
        final var tree = parser.query();
        final var visitor = new AstVisitor();

        return (ParsedQuery) visitor.visitQuery(tree);
    }

    public static String write(ParsedQuery query) {
        final var printer = Printer.create();

        printer.append("SELECT {").down().nextLine();

        for (final var termTree : query.select.termTrees)
            printer.append(termTree).nextLine();

        printer
            .remove().up().nextLine()
            .append("}").nextLine()
            .append("WHERE {").down().nextLine();

        for (final var termTree : query.where.termTrees)
            printer.append(termTree).nextLine();

        printer.remove().up().nextLine().append("}");

        return printer.toString();
    }

}
