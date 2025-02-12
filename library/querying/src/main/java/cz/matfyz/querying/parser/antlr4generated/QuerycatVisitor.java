// Generated from Querycat.g4 by ANTLR 4.13.2
package cz.matfyz.querying.parser.antlr4generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link QuerycatParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface QuerycatVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(QuerycatParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#selectQuery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectQuery(QuerycatParser.SelectQueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#subSelect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubSelect(QuerycatParser.SubSelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(QuerycatParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#selectGraphPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectGraphPattern(QuerycatParser.SelectGraphPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(QuerycatParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(QuerycatParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#solutionModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSolutionModifier(QuerycatParser.SolutionModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#orderClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderClause(QuerycatParser.OrderClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#orderCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderCondition(QuerycatParser.OrderConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#limitOffsetClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitOffsetClauses(QuerycatParser.LimitOffsetClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(QuerycatParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#offsetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffsetClause(QuerycatParser.OffsetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#groupGraphPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupGraphPattern(QuerycatParser.GroupGraphPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#triplesBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriplesBlock(QuerycatParser.TriplesBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#graphPatternNotTriples}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphPatternNotTriples(QuerycatParser.GraphPatternNotTriplesContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#optionalGraphPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionalGraphPattern(QuerycatParser.OptionalGraphPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#groupOrUnionGraphPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupOrUnionGraphPattern(QuerycatParser.GroupOrUnionGraphPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#inlineData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInlineData(QuerycatParser.InlineDataContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#dataBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataBlock(QuerycatParser.DataBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#filter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter(QuerycatParser.FilterContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint(QuerycatParser.ConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#selectTriples}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectTriples(QuerycatParser.SelectTriplesContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#triplesSameSubject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriplesSameSubject(QuerycatParser.TriplesSameSubjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#propertyListNotEmpty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyListNotEmpty(QuerycatParser.PropertyListNotEmptyContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#objectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectList(QuerycatParser.ObjectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(QuerycatParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#verb}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVerb(QuerycatParser.VerbContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#schemaMorphismOrPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaMorphismOrPath(QuerycatParser.SchemaMorphismOrPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#pathAlternative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathAlternative(QuerycatParser.PathAlternativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#pathSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathSequence(QuerycatParser.PathSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#pathWithMod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathWithMod(QuerycatParser.PathWithModContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#pathMod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathMod(QuerycatParser.PathModContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#pathPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathPrimary(QuerycatParser.PathPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#schemaMorphism}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaMorphism(QuerycatParser.SchemaMorphismContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#primaryMorphism}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryMorphism(QuerycatParser.PrimaryMorphismContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#dualMorphism}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDualMorphism(QuerycatParser.DualMorphismContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#graphNode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraphNode(QuerycatParser.GraphNodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(QuerycatParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(QuerycatParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(QuerycatParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#computation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComputation(QuerycatParser.ComputationContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#termList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermList(QuerycatParser.TermListContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#aggregation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregation(QuerycatParser.AggregationContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#distinctModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinctModifier(QuerycatParser.DistinctModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#referenceArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceArgument(QuerycatParser.ReferenceArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#aggregationFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunction(QuerycatParser.AggregationFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(QuerycatParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalOrExpression(QuerycatParser.ConditionalOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalAndExpression(QuerycatParser.ConditionalAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#valueLogical}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueLogical(QuerycatParser.ValueLogicalContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(QuerycatParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#expressionPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionPart(QuerycatParser.ExpressionPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(QuerycatParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#brackettedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBrackettedExpression(QuerycatParser.BrackettedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#numericLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(QuerycatParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#numericLiteralUnsigned}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteralUnsigned(QuerycatParser.NumericLiteralUnsignedContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#numericLiteralPositive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteralPositive(QuerycatParser.NumericLiteralPositiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#numericLiteralNegative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteralNegative(QuerycatParser.NumericLiteralNegativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(QuerycatParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link QuerycatParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(QuerycatParser.StringContext ctx);
}