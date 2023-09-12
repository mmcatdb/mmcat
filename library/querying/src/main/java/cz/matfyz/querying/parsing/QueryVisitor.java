package cz.matfyz.querying.parsing;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.parsing.WhereClause.Type;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatBaseVisitor;
import cz.matfyz.querying.parsing.antlr4generated.QuerycatParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        var selectClause = ctx.selectClause().accept(this).asSelectClause();
        var whereClause = ctx.whereClause().accept(this).asWhereClause();

        return new Query(selectClause, whereClause);
    }

    @Override
    public SelectClause visitSelectClause(QuerycatParser.SelectClauseContext ctx) {
        var graphTriples = ctx.selectGraphPattern().selectTriples();
        List<SelectTriple> triples = graphTriples == null
            ? List.of()
            : graphTriples.accept(this).asSelectTriplesList().triples;
        
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

    @Override
    public GroupGraphPattern visitGroupGraphPattern(QuerycatParser.GroupGraphPatternContext ctx) {
        var triples = ctx.triplesBlock().stream().flatMap(tb -> visit(tb).asWhereTriplesList().triples.stream()).toList();
        var filters = ctx.filter_().stream().map(f -> visit(f).asFilter()).toList();
        var values = ctx.graphPatternNotTriples().stream().map(v -> visit(v).asValues()).toList();
        
        return new GroupGraphPattern(triples, filters, values);
    }

    @Override
    public WhereTriplesList visitTriplesBlock(QuerycatParser.TriplesBlockContext ctx) {
        // Almost identical to select triples, but this is necessary as the grammar definition for these constructs is slightly different.
        var sameSubjectTriples = ctx.triplesSameSubject().accept(this).asCommonTriplesList().triples;

        var moreTriplesNode = ctx.triplesBlock();
        List<WhereTriple> moreTriples = moreTriplesNode == null
            ? List.of()
            : moreTriplesNode.accept(this).asWhereTriplesList().triples;

        var allTriples = Stream.concat(
            sameSubjectTriples.stream().flatMap(commonTriple -> WhereTriple.fromCommonTriple(commonTriple).stream()),
            moreTriples.stream()
        ).toList();

        return new WhereTriplesList(allTriples);
    }

    @Override
    public SelectTriplesList visitSelectTriples(QuerycatParser.SelectTriplesContext ctx) {
        var sameSubjectTriples = ctx.triplesSameSubject().accept(this).asCommonTriplesList().triples;

        var moreTriplesNode = ctx.selectTriples();
        List<SelectTriple> moreTriples = moreTriplesNode == null
            ? List.of()
            : moreTriplesNode.accept(this).asSelectTriplesList().triples;

        var allTriples = Stream.concat(
            sameSubjectTriples.stream().map(SelectTriple::fromCommonTriple),
            moreTriples.stream()
        ).toList();

        return new SelectTriplesList(allTriples);
    }

    @Override
    public CommonTriplesList visitTriplesSameSubject(QuerycatParser.TriplesSameSubjectContext ctx) {
        var variableNode = ctx.varOrTerm().var_();
        if (variableNode == null)
            throw GeneralException.message("Variable expected in term " + ctx.varOrTerm().start);

        var subject = variableNode.accept(this).asVariable();
        var morphismsAndObjects = ctx.propertyListNotEmpty().accept(this).asMorphisms().morphisms;

        var triples = morphismsAndObjects.stream().map(mo -> new CommonTriple(subject, mo.name, mo.valueNode)).toList();
        return new CommonTriplesList(triples);
    }

    @Override
    public MorphismsList visitPropertyListNotEmpty(QuerycatParser.PropertyListNotEmptyContext ctx) {
        var verbNodes = ctx.verb();
        var objectNodes = ctx.objectList();

        var morphismsAndObjects = new ArrayList<MorphismNode>();

        int maxCommonLength = Math.min(verbNodes.size(), objectNodes.size());
        for (int i = 0; i < maxCommonLength; i++) {
            var morphism = verbNodes.get(i).accept(this).asStringValue().value;
            var objects = objectNodes.get(i).accept(this).asObjectsList().objects;

            objects.forEach(v -> morphismsAndObjects.add(new MorphismNode(morphism, v)));
        }

        return new MorphismsList(morphismsAndObjects);
    }

    @Override
    public StringValue visitSchemaMorphismOrPath(QuerycatParser.SchemaMorphismOrPathContext ctx) {
        // Should we do the compound morphism parsing here?
        return new StringValue(ctx.getText());
    }

    @Override
    public ObjectsList visitObjectList(QuerycatParser.ObjectListContext ctx) {
        var objectNodes = ctx.object_();
        
        var objects = new ArrayList<ValueNode>();
        for (var objectNode : objectNodes) {
            var object = objectNode.accept(this).asValueNode();
            objects.add(object);
        }

        return new ObjectsList(objects);
    }

    @Override
    public Variable visitVar_(QuerycatParser.Var_Context ctx) {
        var variableNameNode = ctx.VAR1() != null ? ctx.VAR1() : ctx.VAR2();
        var variableName = variableNameNode.getSymbol().getText().substring(1);

        return Variable.fromName(variableName);
    }

    @Override
    public StringValue visitString_(QuerycatParser.String_Context ctx) {
        // This regexp removes all " and ' characters from both the start and the end of the visited string.
        return new StringValue(ctx.getText().replaceAll("(^[\"']+)|([\"']+$)", ""));
    }

    @Override
    public ParserNode visitRelationalExpression(QuerycatParser.RelationalExpressionContext ctx) {
        var children = ctx.children;

        if (children.size() == 1)
            return visit(children.get(0));

        if (children.size() == 3) {
            var lhs = visit(children.get(0)).asValueNode();
            var rhs = visit(children.get(2)).asValueNode();
            var comparisonOperator = parseComparisonOperator(ctx.children.get(1).getText());

            return new Filter(lhs, comparisonOperator, rhs);
        }

        throw GeneralException.message("You done goofed");
    }

    @Override
    public Values visitDataBlock(QuerycatParser.DataBlockContext ctx) {
        var variable = visit(ctx.var_()).asVariable();
        var allowedValues = ctx.dataBlockValue().stream().map(v -> visit(v).asStringValue().value).toList();

        return new Values(variable, allowedValues);
    }

    private static Map<String, ComparisonOperator> defineComparisonOperators() {
        var output = new TreeMap<String, ComparisonOperator>();
        output.put("=", ComparisonOperator.Equal);
        output.put("!=", ComparisonOperator.NotEqual);
        output.put("<", ComparisonOperator.Less);
        output.put("<=", ComparisonOperator.LessOrEqual);
        output.put(">", ComparisonOperator.Greater);
        output.put(">=", ComparisonOperator.GreaterOrEqual);
        return output;
    }

    private static final Map<String, ComparisonOperator> operators = defineComparisonOperators();

    private static ComparisonOperator parseComparisonOperator(String value) {
        return operators.get(value);
    }

}