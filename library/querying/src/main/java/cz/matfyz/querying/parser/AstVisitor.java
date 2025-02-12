package cz.matfyz.querying.parser;

import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper.Operators;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.exception.ParsingException;
import cz.matfyz.querying.parser.WhereClause.ClauseType;
import cz.matfyz.querying.parser.antlr4generated.QuerycatBaseVisitor;
import cz.matfyz.querying.parser.antlr4generated.QuerycatParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Visitor class whose job is to traverse the AST parsed from each MMQL query, and construct the internal query representation which is subsequently processed by the rest of the algorithm.
 */
public class AstVisitor extends QuerycatBaseVisitor<ParserNode> {

    public static final String SIGNATURE_SEPARATOR = "/";

    @Override protected ParserNode aggregateResult(ParserNode aggregate, ParserNode nextResult) {
        return nextResult == null ? aggregate : nextResult;
    }

    private ExpressionScope scope;

    @Override public ParsedQuery visitSelectQuery(QuerycatParser.SelectQueryContext ctx) {
        scope = new ExpressionScope();

        final SelectClause selectClause = visitSelectClause(ctx.selectClause());
        final WhereClause whereClause = visitWhereClause(ctx.whereClause());

        return new ParsedQuery(selectClause, whereClause, scope);
    }

    @Override public SelectClause visitSelectClause(QuerycatParser.SelectClauseContext ctx) {
        final var graphTriples = ctx.selectGraphPattern().selectTriples();
        final List<TermTree<String>> triples = graphTriples == null
            ? List.of()
            : visitSelectTriples(graphTriples).triples;

        return new SelectClause(triples);
    }

    @Override public WhereClause visitWhereClause(QuerycatParser.WhereClauseContext ctx) {
        // TODO if the pattern is null (or empty? - basically just nested UNION), use the first group's pattern instead.
        // The antlr file probably needs an update tho ...
        final var pattern = visitGroupGraphPattern(ctx.groupGraphPattern());

        // TODO nested clauses
        return new WhereClause(
            ClauseType.Where,
            List.of(),
            pattern.termTrees,
            pattern.filters
        );
    }

    private record GroupGraphPattern(
        List<TermTree<Signature>> termTrees,
        List<Filter> filters
    ) implements ParserNode {}

    @Override public GroupGraphPattern visitGroupGraphPattern(QuerycatParser.GroupGraphPatternContext ctx) {
        final List<TermTree<Signature>> termTrees = ctx.triplesBlock().stream()
            .flatMap(tb -> visitTriplesBlock(tb).triples.stream())
            .toList();
        final List<Filter> filters = ctx.filter().stream()
            .map(f -> visit(f).asFilter())
            .toList();

        return new GroupGraphPattern(termTrees, filters);
    }

    private record WhereTermTrees(List<TermTree<Signature>> triples) implements ParserNode {}

    @Override public WhereTermTrees visitTriplesBlock(QuerycatParser.TriplesBlockContext ctx) {
        // Almost identical to select triples, but this is necessary as the grammar definition for these constructs is slightly different.
        final TermTree<String> sameSubjectTriplesRaw = visitTriplesSameSubject(ctx.triplesSameSubject());
        final TermTree<Signature> sameSubjectTriples = parseSignatures(sameSubjectTriplesRaw);

        final var moreTriplesNode = ctx.triplesBlock();
        final List<TermTree<Signature>> moreTriples = moreTriplesNode == null
            ? List.of()
            : visitTriplesBlock(moreTriplesNode).triples;

        final var allTriples = Stream.concat(
            Stream.of(sameSubjectTriples),
            moreTriples.stream()
        ).toList();

        return new WhereTermTrees(allTriples);
    }

    private TermTree<Signature> parseSignatures(TermTree<String> input) {
        final @Nullable Signature signature = input.edgeFromParent == null ? null : parseSignature(input.edgeFromParent);
        final TermTree<Signature> output = signature == null
            ? TermTree.createRoot(input.term)
            : TermTree.createChild(input.term, signature);

        for (final var child : input.children) {
            final var childTree = parseSignatures(child);
            output.addChild(childTree);
        }

        return output;
    }

    private Signature parseSignature(String edge) {
        try {
            final var bases = Arrays.stream(edge.split(SIGNATURE_SEPARATOR))
                .map(base -> Signature.createBase(Integer.parseInt(base)))
                .toList();

            return Signature.concatenate(bases);
        }
        catch (NumberFormatException e) {
            throw ParsingException.signature(edge);
        }
    }

    private record SelectTriplesList(List<TermTree<String>> triples) implements ParserNode {}

    @Override public SelectTriplesList visitSelectTriples(QuerycatParser.SelectTriplesContext ctx) {
        final TermTree<String> sameSubjectTriples = visitTriplesSameSubject(ctx.triplesSameSubject());

        final var moreTriplesNode = ctx.selectTriples();
        final List<TermTree<String>> moreTriples = moreTriplesNode == null
            ? List.of()
            : visitSelectTriples(moreTriplesNode).triples;

        final var allTriples = Stream.concat(
            Stream.of(sameSubjectTriples),
            moreTriples.stream()
        ).toList();

        return new SelectTriplesList(allTriples);
    }

    @Override public TermTree<String> visitTriplesSameSubject(QuerycatParser.TriplesSameSubjectContext ctx) {
        final var variableNode = ctx.term().variable();
        if (variableNode == null)
            throw GeneralException.message("Variable expected in term " + ctx.term().start);

        final Term subject = visitVariable(variableNode);
        final TermTree<String> output = TermTree.createRoot(subject);

        final PropertyList propertyList = visitPropertyListNotEmpty(ctx.propertyListNotEmpty());

        for (final var term : propertyList.terms)
            output.addChild(term);

        return output;
    }

    private record PropertyList(List<TermTree<String>> terms) implements ParserNode {}

    @Override public PropertyList visitPropertyListNotEmpty(QuerycatParser.PropertyListNotEmptyContext ctx) {
        final var verbNodes = ctx.verb();
        final var objectNodes = ctx.objectList();

        final var output = new ArrayList<TermTree<String>>();

        final int maxCommonLength = Math.min(verbNodes.size(), objectNodes.size());
        for (int i = 0; i < maxCommonLength; i++) {
            final String edge = visitSchemaMorphismOrPath(verbNodes.get(i).schemaMorphismOrPath()).value();
            final Term object = visitObjectList(objectNodes.get(i));

            output.add(TermTree.createChild(object, edge));
        }

        return new PropertyList(output);
    }

    /** Either a string name or a string representation of a morphism path. */
    private record StringEdge(String value) implements ParserNode {}

    @Override public StringEdge visitSchemaMorphismOrPath(QuerycatParser.SchemaMorphismOrPathContext ctx) {
        return new StringEdge(ctx.getText());
    }

    @Override public Term visitObjectList(QuerycatParser.ObjectListContext ctx) {
        // TODO There is always exactly one object in the list - fix it in the parser.
        final var firstObject = ctx.object().getFirst();
        return visitObject(firstObject).asTerm();
    }

    @Override public Term visitVariable(QuerycatParser.VariableContext ctx) {
        // The first character is always the question mark.
        final var name = ctx.VARIABLE().getText().substring(1);
        return new Term(scope.variable.createOriginal(name));
    }

    public Term visitAggregation(QuerycatParser.AggregationContext ctx) {
        final Term variableTerm = visitVariable(ctx.variable());
        var operator = operators.parse(ctx.aggregationFunction().getText());
        final var isDistinct = ctx.distinctModifier() != null;

        if (isDistinct) {
            if (operator != Operator.Count)
                throw GeneralException.message("DISTINCT modifier can only be used with COUNT aggregation");

            operator = Operator.CountDistinct;
        }

        final var expression = scope.computation.create(operator, variableTerm.asVariable());
        return new Term(expression);
    }

    // Both ' and " can be escaped by a backslash (i.e., \' and \" will work in both types of string).
    // However, unescaped ' is allowed only in the " strings and vice versa.
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\\\([\'\"tbnrf\\\\])");

    @Override public Term visitString(QuerycatParser.StringContext ctx) {
        final String text = ctx.getText();
        // Remove ' or " from the beginning and end of the string.
        final String inner = text.substring(1, text.length() - 1);

        final var matcher = ESCAPE_PATTERN.matcher(inner);
        final var builder = new StringBuilder();

        int lastEnd = 0;

        while (matcher.find()) {
            builder.append(inner, lastEnd, matcher.start());

            switch (matcher.group().charAt(1)) {
                case '\'': builder.append('\''); break;
                case '\"': builder.append('\"'); break;
                case 't': builder.append('\t'); break;
                case 'b': builder.append('\b'); break;
                case 'n': builder.append('\n'); break;
                case 'r': builder.append('\r'); break;
                case 'f': builder.append('\f'); break;
                case '\\': builder.append('\\'); break;
                default: throw GeneralException.message("Unknown escape sequence");
            }

            lastEnd = matcher.end();
        }

        builder.append(inner, lastEnd, inner.length());

        return new Term(new Constant(builder.toString()));
    }

    @Override public ParserNode visitFilter(QuerycatParser.FilterContext ctx) {
        final var computation = visit(ctx.constraint()).asTerm().asComputation();
        return new Filter(computation);
    }

    @Override public ParserNode visitRelationalExpression(QuerycatParser.RelationalExpressionContext ctx) {
        final var children = ctx.children;

        if (children.size() == 1)
            // TODO This shouldn't be possible, right?
            // Well, it might as well be a variable, like in FILTER(?a). But we don't support that yet.
            return visit(children.get(0));

        if (children.size() == 3) {
            final var lhs = visit(children.get(0));
            final var operator = operators.parse(children.get(1).getText());
            final var rhs = visit(children.get(2));
            final var lhsExpression = lhs.asTerm().asExpression();
            final var rhsExpression = rhs.asTerm().asExpression();

            return new Term(scope.computation.create(operator, lhsExpression, rhsExpression));
        }

        throw GeneralException.message("You done goofed");
    }

    @Override public Term visitDataBlock(QuerycatParser.DataBlockContext ctx) {
        final List<Expression> arguments = new ArrayList<>();

        final Term variableTerm = visitVariable(ctx.variable());
        arguments.add(variableTerm.asVariable());

        ctx.dataBlockValue().stream()
            .map(v -> visit(v).asTerm().asConstant())
            .forEach(arguments::add);;

        return new Term(scope.computation.create(Operator.In, arguments));
    }

    private static final Operators operators = new Operators();

    static {

        operators.define(Operator.Equal, "=");
        operators.define(Operator.NotEqual, "!=");
        operators.define(Operator.Less, "<");
        operators.define(Operator.LessOrEqual, "<=");
        operators.define(Operator.Greater, ">");
        operators.define(Operator.GreaterOrEqual, ">=");

        operators.define(Operator.Count, "COUNT");
        operators.define(Operator.Sum, "SUM");
        operators.define(Operator.Min, "MIN");
        operators.define(Operator.Max, "MAX");
        operators.define(Operator.Average, "AVG");

        // TODO Not yet part of the grammar.
        operators.define(Operator.In, "IN");
        operators.define(Operator.NotIn, "NOT IN");

    }

}
