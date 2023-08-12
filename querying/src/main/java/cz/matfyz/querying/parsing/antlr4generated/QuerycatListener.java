package cz.matfyz.querying.parsing.antlr4generated;
// Generated from grammars/Querycat.g4 by ANTLR 4.13.0
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link QuerycatParser}.
 */
public interface QuerycatListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(QuerycatParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(QuerycatParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#selectQuery}.
	 * @param ctx the parse tree
	 */
	void enterSelectQuery(QuerycatParser.SelectQueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#selectQuery}.
	 * @param ctx the parse tree
	 */
	void exitSelectQuery(QuerycatParser.SelectQueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#subSelect}.
	 * @param ctx the parse tree
	 */
	void enterSubSelect(QuerycatParser.SubSelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#subSelect}.
	 * @param ctx the parse tree
	 */
	void exitSubSelect(QuerycatParser.SubSelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(QuerycatParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(QuerycatParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#selectGraphPattern}.
	 * @param ctx the parse tree
	 */
	void enterSelectGraphPattern(QuerycatParser.SelectGraphPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#selectGraphPattern}.
	 * @param ctx the parse tree
	 */
	void exitSelectGraphPattern(QuerycatParser.SelectGraphPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(QuerycatParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(QuerycatParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(QuerycatParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(QuerycatParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#solutionModifier}.
	 * @param ctx the parse tree
	 */
	void enterSolutionModifier(QuerycatParser.SolutionModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#solutionModifier}.
	 * @param ctx the parse tree
	 */
	void exitSolutionModifier(QuerycatParser.SolutionModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#limitOffsetClauses}.
	 * @param ctx the parse tree
	 */
	void enterLimitOffsetClauses(QuerycatParser.LimitOffsetClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#limitOffsetClauses}.
	 * @param ctx the parse tree
	 */
	void exitLimitOffsetClauses(QuerycatParser.LimitOffsetClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#orderClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderClause(QuerycatParser.OrderClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#orderClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderClause(QuerycatParser.OrderClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#orderCondition}.
	 * @param ctx the parse tree
	 */
	void enterOrderCondition(QuerycatParser.OrderConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#orderCondition}.
	 * @param ctx the parse tree
	 */
	void exitOrderCondition(QuerycatParser.OrderConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(QuerycatParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(QuerycatParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#offsetClause}.
	 * @param ctx the parse tree
	 */
	void enterOffsetClause(QuerycatParser.OffsetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#offsetClause}.
	 * @param ctx the parse tree
	 */
	void exitOffsetClause(QuerycatParser.OffsetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#groupGraphPattern}.
	 * @param ctx the parse tree
	 */
	void enterGroupGraphPattern(QuerycatParser.GroupGraphPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#groupGraphPattern}.
	 * @param ctx the parse tree
	 */
	void exitGroupGraphPattern(QuerycatParser.GroupGraphPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#triplesBlock}.
	 * @param ctx the parse tree
	 */
	void enterTriplesBlock(QuerycatParser.TriplesBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#triplesBlock}.
	 * @param ctx the parse tree
	 */
	void exitTriplesBlock(QuerycatParser.TriplesBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#graphPatternNotTriples}.
	 * @param ctx the parse tree
	 */
	void enterGraphPatternNotTriples(QuerycatParser.GraphPatternNotTriplesContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#graphPatternNotTriples}.
	 * @param ctx the parse tree
	 */
	void exitGraphPatternNotTriples(QuerycatParser.GraphPatternNotTriplesContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#optionalGraphPattern}.
	 * @param ctx the parse tree
	 */
	void enterOptionalGraphPattern(QuerycatParser.OptionalGraphPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#optionalGraphPattern}.
	 * @param ctx the parse tree
	 */
	void exitOptionalGraphPattern(QuerycatParser.OptionalGraphPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#groupOrUnionGraphPattern}.
	 * @param ctx the parse tree
	 */
	void enterGroupOrUnionGraphPattern(QuerycatParser.GroupOrUnionGraphPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#groupOrUnionGraphPattern}.
	 * @param ctx the parse tree
	 */
	void exitGroupOrUnionGraphPattern(QuerycatParser.GroupOrUnionGraphPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#inlineData}.
	 * @param ctx the parse tree
	 */
	void enterInlineData(QuerycatParser.InlineDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#inlineData}.
	 * @param ctx the parse tree
	 */
	void exitInlineData(QuerycatParser.InlineDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#dataBlock}.
	 * @param ctx the parse tree
	 */
	void enterDataBlock(QuerycatParser.DataBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#dataBlock}.
	 * @param ctx the parse tree
	 */
	void exitDataBlock(QuerycatParser.DataBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#dataBlockValue}.
	 * @param ctx the parse tree
	 */
	void enterDataBlockValue(QuerycatParser.DataBlockValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#dataBlockValue}.
	 * @param ctx the parse tree
	 */
	void exitDataBlockValue(QuerycatParser.DataBlockValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#filter_}.
	 * @param ctx the parse tree
	 */
	void enterFilter_(QuerycatParser.Filter_Context ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#filter_}.
	 * @param ctx the parse tree
	 */
	void exitFilter_(QuerycatParser.Filter_Context ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#constraint}.
	 * @param ctx the parse tree
	 */
	void enterConstraint(QuerycatParser.ConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#constraint}.
	 * @param ctx the parse tree
	 */
	void exitConstraint(QuerycatParser.ConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#selectTriples}.
	 * @param ctx the parse tree
	 */
	void enterSelectTriples(QuerycatParser.SelectTriplesContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#selectTriples}.
	 * @param ctx the parse tree
	 */
	void exitSelectTriples(QuerycatParser.SelectTriplesContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#triplesSameSubject}.
	 * @param ctx the parse tree
	 */
	void enterTriplesSameSubject(QuerycatParser.TriplesSameSubjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#triplesSameSubject}.
	 * @param ctx the parse tree
	 */
	void exitTriplesSameSubject(QuerycatParser.TriplesSameSubjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#propertyListNotEmpty}.
	 * @param ctx the parse tree
	 */
	void enterPropertyListNotEmpty(QuerycatParser.PropertyListNotEmptyContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#propertyListNotEmpty}.
	 * @param ctx the parse tree
	 */
	void exitPropertyListNotEmpty(QuerycatParser.PropertyListNotEmptyContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#propertyList}.
	 * @param ctx the parse tree
	 */
	void enterPropertyList(QuerycatParser.PropertyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#propertyList}.
	 * @param ctx the parse tree
	 */
	void exitPropertyList(QuerycatParser.PropertyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#objectList}.
	 * @param ctx the parse tree
	 */
	void enterObjectList(QuerycatParser.ObjectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#objectList}.
	 * @param ctx the parse tree
	 */
	void exitObjectList(QuerycatParser.ObjectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#object_}.
	 * @param ctx the parse tree
	 */
	void enterObject_(QuerycatParser.Object_Context ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#object_}.
	 * @param ctx the parse tree
	 */
	void exitObject_(QuerycatParser.Object_Context ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#verb}.
	 * @param ctx the parse tree
	 */
	void enterVerb(QuerycatParser.VerbContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#verb}.
	 * @param ctx the parse tree
	 */
	void exitVerb(QuerycatParser.VerbContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#schemaMorphismOrPath}.
	 * @param ctx the parse tree
	 */
	void enterSchemaMorphismOrPath(QuerycatParser.SchemaMorphismOrPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#schemaMorphismOrPath}.
	 * @param ctx the parse tree
	 */
	void exitSchemaMorphismOrPath(QuerycatParser.SchemaMorphismOrPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#pathAlternative}.
	 * @param ctx the parse tree
	 */
	void enterPathAlternative(QuerycatParser.PathAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#pathAlternative}.
	 * @param ctx the parse tree
	 */
	void exitPathAlternative(QuerycatParser.PathAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#pathSequence}.
	 * @param ctx the parse tree
	 */
	void enterPathSequence(QuerycatParser.PathSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#pathSequence}.
	 * @param ctx the parse tree
	 */
	void exitPathSequence(QuerycatParser.PathSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#pathWithMod}.
	 * @param ctx the parse tree
	 */
	void enterPathWithMod(QuerycatParser.PathWithModContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#pathWithMod}.
	 * @param ctx the parse tree
	 */
	void exitPathWithMod(QuerycatParser.PathWithModContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#pathMod}.
	 * @param ctx the parse tree
	 */
	void enterPathMod(QuerycatParser.PathModContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#pathMod}.
	 * @param ctx the parse tree
	 */
	void exitPathMod(QuerycatParser.PathModContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#pathPrimary}.
	 * @param ctx the parse tree
	 */
	void enterPathPrimary(QuerycatParser.PathPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#pathPrimary}.
	 * @param ctx the parse tree
	 */
	void exitPathPrimary(QuerycatParser.PathPrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#schemaMorphism}.
	 * @param ctx the parse tree
	 */
	void enterSchemaMorphism(QuerycatParser.SchemaMorphismContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#schemaMorphism}.
	 * @param ctx the parse tree
	 */
	void exitSchemaMorphism(QuerycatParser.SchemaMorphismContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#primaryMorphism}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryMorphism(QuerycatParser.PrimaryMorphismContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#primaryMorphism}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryMorphism(QuerycatParser.PrimaryMorphismContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#dualMorphism}.
	 * @param ctx the parse tree
	 */
	void enterDualMorphism(QuerycatParser.DualMorphismContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#dualMorphism}.
	 * @param ctx the parse tree
	 */
	void exitDualMorphism(QuerycatParser.DualMorphismContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#graphNode}.
	 * @param ctx the parse tree
	 */
	void enterGraphNode(QuerycatParser.GraphNodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#graphNode}.
	 * @param ctx the parse tree
	 */
	void exitGraphNode(QuerycatParser.GraphNodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#varOrTerm}.
	 * @param ctx the parse tree
	 */
	void enterVarOrTerm(QuerycatParser.VarOrTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#varOrTerm}.
	 * @param ctx the parse tree
	 */
	void exitVarOrTerm(QuerycatParser.VarOrTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#var_}.
	 * @param ctx the parse tree
	 */
	void enterVar_(QuerycatParser.Var_Context ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#var_}.
	 * @param ctx the parse tree
	 */
	void exitVar_(QuerycatParser.Var_Context ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#constantTerm}.
	 * @param ctx the parse tree
	 */
	void enterConstantTerm(QuerycatParser.ConstantTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#constantTerm}.
	 * @param ctx the parse tree
	 */
	void exitConstantTerm(QuerycatParser.ConstantTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#aggregationTerm}.
	 * @param ctx the parse tree
	 */
	void enterAggregationTerm(QuerycatParser.AggregationTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#aggregationTerm}.
	 * @param ctx the parse tree
	 */
	void exitAggregationTerm(QuerycatParser.AggregationTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#distinctModifier}.
	 * @param ctx the parse tree
	 */
	void enterDistinctModifier(QuerycatParser.DistinctModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#distinctModifier}.
	 * @param ctx the parse tree
	 */
	void exitDistinctModifier(QuerycatParser.DistinctModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#aggregationFunc}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunc(QuerycatParser.AggregationFuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#aggregationFunc}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunc(QuerycatParser.AggregationFuncContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(QuerycatParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(QuerycatParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalOrExpression(QuerycatParser.ConditionalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#conditionalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalOrExpression(QuerycatParser.ConditionalOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalAndExpression(QuerycatParser.ConditionalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#conditionalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalAndExpression(QuerycatParser.ConditionalAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#valueLogical}.
	 * @param ctx the parse tree
	 */
	void enterValueLogical(QuerycatParser.ValueLogicalContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#valueLogical}.
	 * @param ctx the parse tree
	 */
	void exitValueLogical(QuerycatParser.ValueLogicalContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(QuerycatParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(QuerycatParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#expressionPart}.
	 * @param ctx the parse tree
	 */
	void enterExpressionPart(QuerycatParser.ExpressionPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#expressionPart}.
	 * @param ctx the parse tree
	 */
	void exitExpressionPart(QuerycatParser.ExpressionPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(QuerycatParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(QuerycatParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#brackettedExpression}.
	 * @param ctx the parse tree
	 */
	void enterBrackettedExpression(QuerycatParser.BrackettedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#brackettedExpression}.
	 * @param ctx the parse tree
	 */
	void exitBrackettedExpression(QuerycatParser.BrackettedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#numericLiteral}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteral(QuerycatParser.NumericLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#numericLiteral}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteral(QuerycatParser.NumericLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#numericLiteralUnsigned}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteralUnsigned(QuerycatParser.NumericLiteralUnsignedContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#numericLiteralUnsigned}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteralUnsigned(QuerycatParser.NumericLiteralUnsignedContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#numericLiteralPositive}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteralPositive(QuerycatParser.NumericLiteralPositiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#numericLiteralPositive}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteralPositive(QuerycatParser.NumericLiteralPositiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#numericLiteralNegative}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteralNegative(QuerycatParser.NumericLiteralNegativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#numericLiteralNegative}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteralNegative(QuerycatParser.NumericLiteralNegativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(QuerycatParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(QuerycatParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#string_}.
	 * @param ctx the parse tree
	 */
	void enterString_(QuerycatParser.String_Context ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#string_}.
	 * @param ctx the parse tree
	 */
	void exitString_(QuerycatParser.String_Context ctx);
	/**
	 * Enter a parse tree produced by {@link QuerycatParser#blankNode}.
	 * @param ctx the parse tree
	 */
	void enterBlankNode(QuerycatParser.BlankNodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link QuerycatParser#blankNode}.
	 * @param ctx the parse tree
	 */
	void exitBlankNode(QuerycatParser.BlankNodeContext ctx);
}