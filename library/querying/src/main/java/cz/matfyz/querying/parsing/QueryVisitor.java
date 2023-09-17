package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AggregationOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.parsing.ParserNode.Term;
import cz.matfyz.querying.parsing.Variable.VariableBuilder;
import cz.matfyz.querying.parsing.WhereClause.Type;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatBaseVisitor;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Visitor class whose job is to traverse the AST parsed from each MMQL query, and construct the internal query representation which is subsequently processed by the rest of the algorithm.
 */
public class QueryVisitor extends QuerycatBaseVisitor<ParserNode> {

    @Override
    protected ParserNode aggregateResult(ParserNode aggregate, ParserNode nextResult) {
        return nextResult == null ? aggregate : nextResult;
    }

    @Override
    public Query visitSelectQuery(QuerycatParser.SelectQueryContext ctx) {
        final SelectClause selectClause = visitSelectClause(ctx.selectClause());
        final WhereClause whereClause = visitWhereClause(ctx.whereClause());

        return new Query(selectClause, whereClause);
    }

    @Override
    public SelectClause visitSelectClause(QuerycatParser.SelectClauseContext ctx) {
        final var graphTriples = ctx.selectGraphPattern().selectTriples();
        final List<SelectTriple> triples = graphTriples == null
            ? List.of()
            : visitSelectTriples(graphTriples).triples;
        
        return new SelectClause(triples, List.of());
    }

    @Override
    public WhereClause visitWhereClause(QuerycatParser.WhereClauseContext ctx) {
        // TODO if the pattern is null (or empty? - basically just nested UNION), use the first group's pattern instead.
        // The antlr file probably needs an update tho ...
        final var pattern = visitGroupGraphPattern(ctx.groupGraphPattern());

        // TODO nested clauses
        return new WhereClause(Type.Where, pattern, List.of());
    }

    private Stack<VariableBuilder> variableBuilders = new Stack<>();

    @Override
    public GroupGraphPattern visitGroupGraphPattern(QuerycatParser.GroupGraphPatternContext ctx) {
        variableBuilders.push(new VariableBuilder());

        final var triples = ctx.triplesBlock().stream()
            .flatMap(tb -> visitTriplesBlock(tb).triples.stream())
            .toList();
        final var filters = ctx.filter_().stream()
            .map(f -> visit(f).asFilter().asConditionFilter())
            .toList();
        final var values = ctx.graphPatternNotTriples().stream()
            .map(v -> visit(v).asFilter().asValueFilter())
            .toList();

        variableBuilders.pop();
        
        return new GroupGraphPattern(triples, filters, values);
    }

    private static record WhereTriplesList(List<WhereTriple> triples) implements ParserNode {}

    @Override
    public WhereTriplesList visitTriplesBlock(QuerycatParser.TriplesBlockContext ctx) {
        // Almost identical to select triples, but this is necessary as the grammar definition for these constructs is slightly different.
        final var sameSubjectTriples = visitTriplesSameSubject(ctx.triplesSameSubject()).triples;

        final var moreTriplesNode = ctx.triplesBlock();
        final List<WhereTriple> moreTriples = moreTriplesNode == null
            ? List.of()
            : visitTriplesBlock(moreTriplesNode).triples;

        final var allTriples = Stream.concat(
            sameSubjectTriples.stream().flatMap(commonTriple -> WhereTriple.fromCommonTriple(commonTriple, variableBuilders.peek()).stream()),
            moreTriples.stream()
        ).toList();

        return new WhereTriplesList(allTriples);
    }

    private static record SelectTriplesList(List<SelectTriple> triples) implements ParserNode {}

    @Override
    public SelectTriplesList visitSelectTriples(QuerycatParser.SelectTriplesContext ctx) {
        final var sameSubjectTriples = visitTriplesSameSubject(ctx.triplesSameSubject()).triples;

        final var moreTriplesNode = ctx.selectTriples();
        final List<SelectTriple> moreTriples = moreTriplesNode == null
            ? List.of()
            : visitSelectTriples(moreTriplesNode).triples;

        final var allTriples = Stream.concat(
            sameSubjectTriples.stream().map(SelectTriple::fromCommonTriple),
            moreTriples.stream()
        ).toList();

        return new SelectTriplesList(allTriples);
    }

    private static record CommonTriplesList(List<CommonTriple> triples) implements ParserNode {}

    @Override
    public CommonTriplesList visitTriplesSameSubject(QuerycatParser.TriplesSameSubjectContext ctx) {
        var variableNode = ctx.varOrTerm().var_();
        if (variableNode == null)
            throw GeneralException.message("Variable expected in term " + ctx.varOrTerm().start);

        final Variable subject = visitVar_(variableNode);
        final MorphismsList propertyList = visitPropertyListNotEmpty(ctx.propertyListNotEmpty());;

        final var triples = propertyList.morphisms.stream().map(m -> new CommonTriple(subject, m.morphism(), m.term())).toList();

        return new CommonTriplesList(triples);
    }

    private static record MorphismsList(List<MorphismWithTerm> morphisms) implements ParserNode {}

    private static record MorphismWithTerm(String morphism, Term term) {}

    @Override
    public MorphismsList visitPropertyListNotEmpty(QuerycatParser.PropertyListNotEmptyContext ctx) {
        final var verbNodes = ctx.verb();
        final var objectNodes = ctx.objectList();

        final var morphismsAndObjects = new ArrayList<MorphismWithTerm>();

        final int maxCommonLength = Math.min(verbNodes.size(), objectNodes.size());
        for (int i = 0; i < maxCommonLength; i++) {
            final var morphism = visitSchemaMorphismOrPath(verbNodes.get(i).schemaMorphismOrPath()).value;
            final var objects = visitObjectList(objectNodes.get(i)).objects;

            objects.forEach(object -> morphismsAndObjects.add(new MorphismWithTerm(morphism, object)));
        }

        return new MorphismsList(morphismsAndObjects);
    }

    @Override
    public StringValue visitSchemaMorphismOrPath(QuerycatParser.SchemaMorphismOrPathContext ctx) {
        // Should we do the compound morphism parsing here?
        return new StringValue(ctx.getText());
    }
    
    private static record ObjectsList(List<Term> objects) implements ParserNode {}

    @Override
    public ObjectsList visitObjectList(QuerycatParser.ObjectListContext ctx) {
        final var objects = ctx.object_().stream()
            .map(objectCtx -> visitObject_(objectCtx).asTerm())
            .toList();

        return new ObjectsList(objects);
    }

    @Override
    public Variable visitVar_(QuerycatParser.Var_Context ctx) {
        final var variableNameNode = ctx.VAR1() != null ? ctx.VAR1() : ctx.VAR2();
        final var variableName = variableNameNode.getSymbol().getText().substring(1);

        return variableBuilders.peek().fromName(variableName);
    }

    public Aggregation visitAggregation(QuerycatParser.AggregationTermContext ctx) {
        final var operator = parseAggregationOperator(ctx.aggregationFunc().getText());
        final var variable = visitVar_(ctx.var_());
        final var isDistinct = ctx.distinctModifier() != null;

        return new Aggregation(operator, variable, isDistinct);
    }

    @Override
    public StringValue visitString_(QuerycatParser.String_Context ctx) {
        // This regexp removes all " and ' characters from both the start and the end of the visited string.
        return new StringValue(ctx.getText().replaceAll("(^[\"']+)|([\"']+$)", ""));
    }

    @Override
    public ParserNode visitRelationalExpression(QuerycatParser.RelationalExpressionContext ctx) {
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

    @Override
    public ValueFilter visitDataBlock(QuerycatParser.DataBlockContext ctx) {
        final var variable = visitVar_(ctx.var_());
        final var allowedValues = ctx.dataBlockValue().stream()
            .map(v -> visit(v).asTerm().asStringValue().value)
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