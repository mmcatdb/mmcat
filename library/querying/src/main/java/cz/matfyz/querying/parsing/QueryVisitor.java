package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AggregationOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.exception.ParsingException;
import cz.matfyz.querying.parsing.Filter.ConditionFilter;
import cz.matfyz.querying.parsing.Filter.ValueFilter;
import cz.matfyz.querying.parsing.WhereClause.Type;
import cz.matfyz.querying.parsing.Term.Aggregation;
import cz.matfyz.querying.parsing.Term.StringValue;
import cz.matfyz.querying.parsing.Term.Variable;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatBaseVisitor;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatParser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Visitor class whose job is to traverse the AST parsed from each MMQL query, and construct the internal query representation which is subsequently processed by the rest of the algorithm.
 */
public class QueryVisitor extends QuerycatBaseVisitor<ParserNode> {

    public static final String SIGNATURE_SEPARATOR = "/";

    @Override protected ParserNode aggregateResult(ParserNode aggregate, ParserNode nextResult) {
        return nextResult == null ? aggregate : nextResult;
    }

    private QueryContext queryContext;
    private Deque<Term.Builder> termBuilders = new ArrayDeque<>();

    @Override public Query visitSelectQuery(QuerycatParser.SelectQueryContext ctx) {
        queryContext = new QueryContext();

        final SelectClause selectClause = visitSelectClause(ctx.selectClause());
        final WhereClause whereClause = visitWhereClause(ctx.whereClause());

        return new Query(selectClause, whereClause, queryContext);
    }

    @Override public SelectClause visitSelectClause(QuerycatParser.SelectClauseContext ctx) {
        termBuilders.push(new Term.Builder());

        final var graphTriples = ctx.selectGraphPattern().selectTriples();
        final List<TermTree<String>> triples = graphTriples == null
            ? List.of()
            : visitSelectTriples(graphTriples).triples;

        termBuilders.pop();

        return new SelectClause(triples);
    }

    @Override public WhereClause visitWhereClause(QuerycatParser.WhereClauseContext ctx) {
        // TODO if the pattern is null (or empty? - basically just nested UNION), use the first group's pattern instead.
        // The antlr file probably needs an update tho ...
        final var pattern = visitGroupGraphPattern(ctx.groupGraphPattern());

        // TODO nested clauses
        return new WhereClause(
            Type.Where,
            List.of(),
            pattern.termBuilder,
            pattern.termTrees,
            pattern.condigionFilters,
            pattern.valueFilters
        );
    }

    private record GroupGraphPattern(
        Term.Builder termBuilder,
        List<TermTree<Signature>> termTrees,
        List<ConditionFilter> condigionFilters,
        List<ValueFilter> valueFilters
    ) implements ParserNode {}

    @Override public GroupGraphPattern visitGroupGraphPattern(QuerycatParser.GroupGraphPatternContext ctx) {
        final var termBuilder = new Term.Builder();
        termBuilders.push(termBuilder);

        final List<TermTree<Signature>> termTrees = ctx.triplesBlock().stream()
            .flatMap(tb -> visitTriplesBlock(tb).triples.stream())
            .toList();
        final List<ConditionFilter> conditionFilters = ctx.filter_().stream()
            .map(f -> visit(f).asFilter().asConditionFilter())
            .toList();
        final List<ValueFilter> valueFilters = ctx.graphPatternNotTriples().stream()
            .map(v -> visit(v).asFilter().asValueFilter())
            .toList();

        termBuilders.pop();

        return new GroupGraphPattern(termBuilder, termTrees, conditionFilters, valueFilters);
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
            ? TermTree.root(input.term.asVariable())
            : TermTree.child(input.term, signature);

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
        final var variableNode = ctx.varOrTerm().var_();
        if (variableNode == null)
            throw GeneralException.message("Variable expected in term " + ctx.varOrTerm().start);

        final Variable subject = visitVar_(variableNode);
        final TermTree<String> output = TermTree.root(subject);

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

            output.add(TermTree.child(object, edge));
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
        final var firstObject = ctx.object_().getFirst();
        return visitObject_(firstObject).asTerm();
    }

    @Override public Variable visitVar_(QuerycatParser.Var_Context ctx) {
        final var variableNameNode = ctx.VAR1() != null ? ctx.VAR1() : ctx.VAR2();
        final var variableName = variableNameNode.getSymbol().getText().substring(1);

        return termBuilders.peek().variable(variableName);
    }

    public Aggregation visitAggregation(QuerycatParser.AggregationTermContext ctx) {
        final var operator = parseAggregationOperator(ctx.aggregationFunc().getText());
        final var variable = visitVar_(ctx.var_());
        final var isDistinct = ctx.distinctModifier() != null;

        return new Aggregation(operator, variable, isDistinct);
    }

    @Override public StringValue visitString_(QuerycatParser.String_Context ctx) {
        // This regexp removes all " and ' characters from both the start and the end of the visited string.
        final String value = ctx.getText().replaceAll("(^[\"']+)|([\"']+$)", "");
        return termBuilders.peek().stringValue(value);
    }

    @Override public ParserNode visitRelationalExpression(QuerycatParser.RelationalExpressionContext ctx) {
        final var children = ctx.children;

        if (children.size() == 1)
            return visit(children.get(0));

        if (children.size() == 3) {
            final var lhs = visit(children.get(0)).asTerm();
            final var rhs = visit(children.get(2)).asTerm();
            final var comparisonOperator = parseComparisonOperator(children.get(1).getText());

            return new ConditionFilter(lhs, comparisonOperator, rhs);
        }

        throw GeneralException.message("You done goofed");
    }

    @Override public ValueFilter visitDataBlock(QuerycatParser.DataBlockContext ctx) {
        final var variable = visitVar_(ctx.var_());
        final var allowedValues = ctx.dataBlockValue().stream()
            .map(v -> visit(v).asTerm().asStringValue().value())
            .toList();

        return new ValueFilter(variable, allowedValues);
    }

    private static Map<String, ComparisonOperator> defineComparisonOperators() {
        final var output = new TreeMap<String, ComparisonOperator>();
        output.put("=", ComparisonOperator.Equal);
        output.put("!=", ComparisonOperator.NotEqual);
        output.put("<", ComparisonOperator.Less);
        output.put("<=", ComparisonOperator.LessOrEqual);
        output.put(">", ComparisonOperator.Greater);
        output.put(">=", ComparisonOperator.GreaterOrEqual);
        return output;
    }

    private static final Map<String, ComparisonOperator> comparisonOperators = defineComparisonOperators();

    private static ComparisonOperator parseComparisonOperator(String value) {
        return comparisonOperators.get(value);
    }

    private static Map<String, AggregationOperator> defineAggregationOperators() {
        final var output = new TreeMap<String, AggregationOperator>();
        output.put("COUNT", AggregationOperator.Count);
        output.put("SUM", AggregationOperator.Sum);
        output.put("MIN", AggregationOperator.Min);
        output.put("MAX", AggregationOperator.Max);
        output.put("AVG", AggregationOperator.Average);
        return output;
    }

    private static final Map<String, AggregationOperator> aggregationOperators = defineAggregationOperators();

    private static AggregationOperator parseAggregationOperator(String value) {
        return aggregationOperators.get(value);
    }

}
