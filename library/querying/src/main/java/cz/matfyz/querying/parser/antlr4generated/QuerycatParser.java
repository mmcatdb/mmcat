// Generated from Querycat.g4 by ANTLR 4.13.2
package cz.matfyz.querying.parser.antlr4generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class QuerycatParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, T__44=45, 
		SCHEMA_MORPHISM=46, SCHEMA_IDENTIFIER=47, VARIABLE=48, INTEGER=49, DECIMAL=50, 
		DOUBLE=51, INTEGER_POSITIVE=52, DECIMAL_POSITIVE=53, DOUBLE_POSITIVE=54, 
		INTEGER_NEGATIVE=55, DECIMAL_NEGATIVE=56, DOUBLE_NEGATIVE=57, EXPONENT=58, 
		STRING_LITERAL_SINGLE=59, STRING_LITERAL_DOUBLE=60, ESCAPE_CHAR=61, VARNAME=62, 
		PN_CHARS_U=63, WS=64, COMMENT=65;
	public static final int
		RULE_query = 0, RULE_selectQuery = 1, RULE_subSelect = 2, RULE_selectClause = 3, 
		RULE_selectGraphPattern = 4, RULE_fromClause = 5, RULE_whereClause = 6, 
		RULE_solutionModifier = 7, RULE_orderClause = 8, RULE_orderCondition = 9, 
		RULE_limitOffsetClauses = 10, RULE_limitClause = 11, RULE_offsetClause = 12, 
		RULE_groupGraphPattern = 13, RULE_triplesBlock = 14, RULE_graphPatternNotTriples = 15, 
		RULE_optionalGraphPattern = 16, RULE_groupOrUnionGraphPattern = 17, RULE_inlineData = 18, 
		RULE_dataBlock = 19, RULE_filter = 20, RULE_constraint = 21, RULE_selectTriples = 22, 
		RULE_triplesSameSubject = 23, RULE_propertyListNotEmpty = 24, RULE_objectList = 25, 
		RULE_object = 26, RULE_verb = 27, RULE_schemaMorphismOrPath = 28, RULE_pathAlternative = 29, 
		RULE_pathSequence = 30, RULE_pathWithMod = 31, RULE_pathMod = 32, RULE_pathPrimary = 33, 
		RULE_schemaMorphism = 34, RULE_primaryMorphism = 35, RULE_dualMorphism = 36, 
		RULE_graphNode = 37, RULE_term = 38, RULE_variable = 39, RULE_constant = 40, 
		RULE_computation = 41, RULE_termList = 42, RULE_aggregation = 43, RULE_distinctModifier = 44, 
		RULE_referenceArgument = 45, RULE_aggregationFunction = 46, RULE_expression = 47, 
		RULE_conditionalOrExpression = 48, RULE_conditionalAndExpression = 49, 
		RULE_valueLogical = 50, RULE_relationalExpression = 51, RULE_expressionPart = 52, 
		RULE_primaryExpression = 53, RULE_brackettedExpression = 54, RULE_numericLiteral = 55, 
		RULE_numericLiteralUnsigned = 56, RULE_numericLiteralPositive = 57, RULE_numericLiteralNegative = 58, 
		RULE_booleanLiteral = 59, RULE_string = 60;
	private static String[] makeRuleNames() {
		return new String[] {
			"query", "selectQuery", "subSelect", "selectClause", "selectGraphPattern", 
			"fromClause", "whereClause", "solutionModifier", "orderClause", "orderCondition", 
			"limitOffsetClauses", "limitClause", "offsetClause", "groupGraphPattern", 
			"triplesBlock", "graphPatternNotTriples", "optionalGraphPattern", "groupOrUnionGraphPattern", 
			"inlineData", "dataBlock", "filter", "constraint", "selectTriples", "triplesSameSubject", 
			"propertyListNotEmpty", "objectList", "object", "verb", "schemaMorphismOrPath", 
			"pathAlternative", "pathSequence", "pathWithMod", "pathMod", "pathPrimary", 
			"schemaMorphism", "primaryMorphism", "dualMorphism", "graphNode", "term", 
			"variable", "constant", "computation", "termList", "aggregation", "distinctModifier", 
			"referenceArgument", "aggregationFunction", "expression", "conditionalOrExpression", 
			"conditionalAndExpression", "valueLogical", "relationalExpression", "expressionPart", 
			"primaryExpression", "brackettedExpression", "numericLiteral", "numericLiteralUnsigned", 
			"numericLiteralPositive", "numericLiteralNegative", "booleanLiteral", 
			"string"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'SELECT'", "'{'", "'}'", "'FROM'", "'WHERE'", "'ORDER'", "'BY'", 
			"'ASC'", "'DESC'", "'LIMIT'", "'OFFSET'", "'.'", "'OPTIONAL'", "'UNION'", 
			"'MINUS'", "'VALUES'", "'FILTER'", "';'", "','", "'|'", "'/'", "'?'", 
			"'*'", "'+'", "'('", "')'", "'-'", "'AS'", "'CONCAT'", "'DISTINCT'", 
			"'COUNT'", "'SUM'", "'AVG'", "'MIN'", "'MAX'", "'||'", "'&&'", "'='", 
			"'!='", "'<'", "'>'", "'<='", "'>='", "'true'", "'false'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, "SCHEMA_MORPHISM", 
			"SCHEMA_IDENTIFIER", "VARIABLE", "INTEGER", "DECIMAL", "DOUBLE", "INTEGER_POSITIVE", 
			"DECIMAL_POSITIVE", "DOUBLE_POSITIVE", "INTEGER_NEGATIVE", "DECIMAL_NEGATIVE", 
			"DOUBLE_NEGATIVE", "EXPONENT", "STRING_LITERAL_SINGLE", "STRING_LITERAL_DOUBLE", 
			"ESCAPE_CHAR", "VARNAME", "PN_CHARS_U", "WS", "COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Querycat.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public QuerycatParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QueryContext extends ParserRuleContext {
		public SelectQueryContext selectQuery() {
			return getRuleContext(SelectQueryContext.class,0);
		}
		public TerminalNode EOF() { return getToken(QuerycatParser.EOF, 0); }
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_query);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			selectQuery();
			setState(123);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectQueryContext extends ParserRuleContext {
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public SolutionModifierContext solutionModifier() {
			return getRuleContext(SolutionModifierContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public SelectQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectQuery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSelectQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectQueryContext selectQuery() throws RecognitionException {
		SelectQueryContext _localctx = new SelectQueryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_selectQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			selectClause();
			setState(127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(126);
				fromClause();
				}
			}

			setState(129);
			whereClause();
			setState(130);
			solutionModifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SubSelectContext extends ParserRuleContext {
		public SelectQueryContext selectQuery() {
			return getRuleContext(SelectQueryContext.class,0);
		}
		public SubSelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subSelect; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSubSelect(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSubSelect(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSubSelect(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubSelectContext subSelect() throws RecognitionException {
		SubSelectContext _localctx = new SubSelectContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_subSelect);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			selectQuery();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectClauseContext extends ParserRuleContext {
		public SelectGraphPatternContext selectGraphPattern() {
			return getRuleContext(SelectGraphPatternContext.class,0);
		}
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSelectClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_selectClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(T__0);
			setState(135);
			selectGraphPattern();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectGraphPatternContext extends ParserRuleContext {
		public SelectTriplesContext selectTriples() {
			return getRuleContext(SelectTriplesContext.class,0);
		}
		public SelectGraphPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectGraphPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectGraphPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectGraphPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSelectGraphPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectGraphPatternContext selectGraphPattern() throws RecognitionException {
		SelectGraphPatternContext _localctx = new SelectGraphPatternContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_selectGraphPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			match(T__1);
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2017384001752268800L) != 0)) {
				{
				setState(138);
				selectTriples();
				}
			}

			setState(141);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode SCHEMA_IDENTIFIER() { return getToken(QuerycatParser.SCHEMA_IDENTIFIER, 0); }
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_fromClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			match(T__3);
			setState(144);
			match(SCHEMA_IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhereClauseContext extends ParserRuleContext {
		public GroupGraphPatternContext groupGraphPattern() {
			return getRuleContext(GroupGraphPatternContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_whereClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(146);
				match(T__4);
				}
			}

			setState(149);
			groupGraphPattern();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SolutionModifierContext extends ParserRuleContext {
		public OrderClauseContext orderClause() {
			return getRuleContext(OrderClauseContext.class,0);
		}
		public LimitOffsetClausesContext limitOffsetClauses() {
			return getRuleContext(LimitOffsetClausesContext.class,0);
		}
		public SolutionModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_solutionModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSolutionModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSolutionModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSolutionModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SolutionModifierContext solutionModifier() throws RecognitionException {
		SolutionModifierContext _localctx = new SolutionModifierContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_solutionModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__5) {
				{
				setState(151);
				orderClause();
				}
			}

			setState(155);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9 || _la==T__10) {
				{
				setState(154);
				limitOffsetClauses();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderClauseContext extends ParserRuleContext {
		public List<OrderConditionContext> orderCondition() {
			return getRuleContexts(OrderConditionContext.class);
		}
		public OrderConditionContext orderCondition(int i) {
			return getRuleContext(OrderConditionContext.class,i);
		}
		public OrderClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOrderClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOrderClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitOrderClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderClauseContext orderClause() throws RecognitionException {
		OrderClauseContext _localctx = new OrderClauseContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_orderClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(T__5);
			setState(158);
			match(T__6);
			setState(160); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(159);
				orderCondition();
				}
				}
				setState(162); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 281475010265856L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderConditionContext extends ParserRuleContext {
		public BrackettedExpressionContext brackettedExpression() {
			return getRuleContext(BrackettedExpressionContext.class,0);
		}
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public OrderConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOrderCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOrderCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitOrderCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderConditionContext orderCondition() throws RecognitionException {
		OrderConditionContext _localctx = new OrderConditionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_orderCondition);
		int _la;
		try {
			setState(170);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__7:
			case T__8:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(164);
				_la = _input.LA(1);
				if ( !(_la==T__7 || _la==T__8) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(165);
				brackettedExpression();
				}
				}
				break;
			case T__24:
			case VARIABLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(168);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__24:
					{
					setState(166);
					constraint();
					}
					break;
				case VARIABLE:
					{
					setState(167);
					variable();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LimitOffsetClausesContext extends ParserRuleContext {
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public OffsetClauseContext offsetClause() {
			return getRuleContext(OffsetClauseContext.class,0);
		}
		public LimitOffsetClausesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitOffsetClauses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterLimitOffsetClauses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitLimitOffsetClauses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitLimitOffsetClauses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitOffsetClausesContext limitOffsetClauses() throws RecognitionException {
		LimitOffsetClausesContext _localctx = new LimitOffsetClausesContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_limitOffsetClauses);
		int _la;
		try {
			setState(180);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__9:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(172);
				limitClause();
				setState(174);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__10) {
					{
					setState(173);
					offsetClause();
					}
				}

				}
				}
				break;
			case T__10:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(176);
				offsetClause();
				setState(178);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__9) {
					{
					setState(177);
					limitClause();
					}
				}

				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LimitClauseContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(QuerycatParser.INTEGER, 0); }
		public LimitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterLimitClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitLimitClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitLimitClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitClauseContext limitClause() throws RecognitionException {
		LimitClauseContext _localctx = new LimitClauseContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_limitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			match(T__9);
			setState(183);
			match(INTEGER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OffsetClauseContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(QuerycatParser.INTEGER, 0); }
		public OffsetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_offsetClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOffsetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOffsetClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitOffsetClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OffsetClauseContext offsetClause() throws RecognitionException {
		OffsetClauseContext _localctx = new OffsetClauseContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_offsetClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			match(T__10);
			setState(186);
			match(INTEGER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupGraphPatternContext extends ParserRuleContext {
		public SubSelectContext subSelect() {
			return getRuleContext(SubSelectContext.class,0);
		}
		public List<TriplesBlockContext> triplesBlock() {
			return getRuleContexts(TriplesBlockContext.class);
		}
		public TriplesBlockContext triplesBlock(int i) {
			return getRuleContext(TriplesBlockContext.class,i);
		}
		public List<GraphPatternNotTriplesContext> graphPatternNotTriples() {
			return getRuleContexts(GraphPatternNotTriplesContext.class);
		}
		public GraphPatternNotTriplesContext graphPatternNotTriples(int i) {
			return getRuleContext(GraphPatternNotTriplesContext.class,i);
		}
		public List<FilterContext> filter() {
			return getRuleContexts(FilterContext.class);
		}
		public FilterContext filter(int i) {
			return getRuleContext(FilterContext.class,i);
		}
		public GroupGraphPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupGraphPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGroupGraphPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGroupGraphPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitGroupGraphPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupGraphPatternContext groupGraphPattern() throws RecognitionException {
		GroupGraphPatternContext _localctx = new GroupGraphPatternContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_groupGraphPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			match(T__1);
			setState(208);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				{
				setState(189);
				subSelect();
				}
				break;
			case T__1:
			case T__2:
			case T__12:
			case T__15:
			case T__16:
			case T__28:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__34:
			case T__43:
			case T__44:
			case VARIABLE:
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case INTEGER_POSITIVE:
			case DECIMAL_POSITIVE:
			case DOUBLE_POSITIVE:
			case INTEGER_NEGATIVE:
			case DECIMAL_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case STRING_LITERAL_SINGLE:
			case STRING_LITERAL_DOUBLE:
				{
				{
				setState(191);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2017384001752268800L) != 0)) {
					{
					setState(190);
					triplesBlock();
					}
				}

				setState(205);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 204804L) != 0)) {
					{
					{
					setState(195);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case T__1:
					case T__12:
					case T__15:
						{
						setState(193);
						graphPatternNotTriples();
						}
						break;
					case T__16:
						{
						setState(194);
						filter();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(198);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==T__11) {
						{
						setState(197);
						match(T__11);
						}
					}

					setState(201);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2017384001752268800L) != 0)) {
						{
						setState(200);
						triplesBlock();
						}
					}

					}
					}
					setState(207);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(210);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TriplesBlockContext extends ParserRuleContext {
		public TriplesSameSubjectContext triplesSameSubject() {
			return getRuleContext(TriplesSameSubjectContext.class,0);
		}
		public TriplesBlockContext triplesBlock() {
			return getRuleContext(TriplesBlockContext.class,0);
		}
		public TriplesBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_triplesBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterTriplesBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitTriplesBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitTriplesBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TriplesBlockContext triplesBlock() throws RecognitionException {
		TriplesBlockContext _localctx = new TriplesBlockContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_triplesBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			triplesSameSubject();
			setState(217);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__11) {
				{
				setState(213);
				match(T__11);
				setState(215);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2017384001752268800L) != 0)) {
					{
					setState(214);
					triplesBlock();
					}
				}

				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GraphPatternNotTriplesContext extends ParserRuleContext {
		public OptionalGraphPatternContext optionalGraphPattern() {
			return getRuleContext(OptionalGraphPatternContext.class,0);
		}
		public GroupOrUnionGraphPatternContext groupOrUnionGraphPattern() {
			return getRuleContext(GroupOrUnionGraphPatternContext.class,0);
		}
		public InlineDataContext inlineData() {
			return getRuleContext(InlineDataContext.class,0);
		}
		public GraphPatternNotTriplesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graphPatternNotTriples; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGraphPatternNotTriples(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGraphPatternNotTriples(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitGraphPatternNotTriples(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GraphPatternNotTriplesContext graphPatternNotTriples() throws RecognitionException {
		GraphPatternNotTriplesContext _localctx = new GraphPatternNotTriplesContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_graphPatternNotTriples);
		try {
			setState(222);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__12:
				enterOuterAlt(_localctx, 1);
				{
				setState(219);
				optionalGraphPattern();
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(220);
				groupOrUnionGraphPattern();
				}
				break;
			case T__15:
				enterOuterAlt(_localctx, 3);
				{
				setState(221);
				inlineData();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OptionalGraphPatternContext extends ParserRuleContext {
		public GroupGraphPatternContext groupGraphPattern() {
			return getRuleContext(GroupGraphPatternContext.class,0);
		}
		public OptionalGraphPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optionalGraphPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOptionalGraphPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOptionalGraphPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitOptionalGraphPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OptionalGraphPatternContext optionalGraphPattern() throws RecognitionException {
		OptionalGraphPatternContext _localctx = new OptionalGraphPatternContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_optionalGraphPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224);
			match(T__12);
			setState(225);
			groupGraphPattern();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupOrUnionGraphPatternContext extends ParserRuleContext {
		public List<GroupGraphPatternContext> groupGraphPattern() {
			return getRuleContexts(GroupGraphPatternContext.class);
		}
		public GroupGraphPatternContext groupGraphPattern(int i) {
			return getRuleContext(GroupGraphPatternContext.class,i);
		}
		public GroupOrUnionGraphPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupOrUnionGraphPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGroupOrUnionGraphPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGroupOrUnionGraphPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitGroupOrUnionGraphPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupOrUnionGraphPatternContext groupOrUnionGraphPattern() throws RecognitionException {
		GroupOrUnionGraphPatternContext _localctx = new GroupOrUnionGraphPatternContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_groupOrUnionGraphPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			groupGraphPattern();
			setState(232);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13 || _la==T__14) {
				{
				{
				setState(228);
				_la = _input.LA(1);
				if ( !(_la==T__13 || _la==T__14) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(229);
				groupGraphPattern();
				}
				}
				setState(234);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InlineDataContext extends ParserRuleContext {
		public DataBlockContext dataBlock() {
			return getRuleContext(DataBlockContext.class,0);
		}
		public InlineDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterInlineData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitInlineData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitInlineData(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InlineDataContext inlineData() throws RecognitionException {
		InlineDataContext _localctx = new InlineDataContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_inlineData);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(235);
			match(T__15);
			setState(236);
			dataBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DataBlockContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
		}
		public DataBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterDataBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitDataBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitDataBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataBlockContext dataBlock() throws RecognitionException {
		DataBlockContext _localctx = new DataBlockContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_dataBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			variable();
			setState(239);
			match(T__1);
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2017102459666694144L) != 0)) {
				{
				{
				setState(240);
				constant();
				}
				}
				setState(245);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(246);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FilterContext extends ParserRuleContext {
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public FilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterFilter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitFilter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitFilter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterContext filter() throws RecognitionException {
		FilterContext _localctx = new FilterContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_filter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			match(T__16);
			setState(249);
			constraint();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstraintContext extends ParserRuleContext {
		public BrackettedExpressionContext brackettedExpression() {
			return getRuleContext(BrackettedExpressionContext.class,0);
		}
		public ConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConstraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_constraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251);
			brackettedExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectTriplesContext extends ParserRuleContext {
		public TriplesSameSubjectContext triplesSameSubject() {
			return getRuleContext(TriplesSameSubjectContext.class,0);
		}
		public SelectTriplesContext selectTriples() {
			return getRuleContext(SelectTriplesContext.class,0);
		}
		public SelectTriplesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectTriples; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectTriples(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectTriples(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSelectTriples(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectTriplesContext selectTriples() throws RecognitionException {
		SelectTriplesContext _localctx = new SelectTriplesContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_selectTriples);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(253);
			triplesSameSubject();
			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__11) {
				{
				setState(254);
				match(T__11);
				setState(256);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2017384001752268800L) != 0)) {
					{
					setState(255);
					selectTriples();
					}
				}

				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TriplesSameSubjectContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public PropertyListNotEmptyContext propertyListNotEmpty() {
			return getRuleContext(PropertyListNotEmptyContext.class,0);
		}
		public TriplesSameSubjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_triplesSameSubject; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterTriplesSameSubject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitTriplesSameSubject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitTriplesSameSubject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TriplesSameSubjectContext triplesSameSubject() throws RecognitionException {
		TriplesSameSubjectContext _localctx = new TriplesSameSubjectContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_triplesSameSubject);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(260);
			term();
			setState(261);
			propertyListNotEmpty();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PropertyListNotEmptyContext extends ParserRuleContext {
		public List<VerbContext> verb() {
			return getRuleContexts(VerbContext.class);
		}
		public VerbContext verb(int i) {
			return getRuleContext(VerbContext.class,i);
		}
		public List<ObjectListContext> objectList() {
			return getRuleContexts(ObjectListContext.class);
		}
		public ObjectListContext objectList(int i) {
			return getRuleContext(ObjectListContext.class,i);
		}
		public PropertyListNotEmptyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyListNotEmpty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPropertyListNotEmpty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPropertyListNotEmpty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPropertyListNotEmpty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyListNotEmptyContext propertyListNotEmpty() throws RecognitionException {
		PropertyListNotEmptyContext _localctx = new PropertyListNotEmptyContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_propertyListNotEmpty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			verb();
			setState(264);
			objectList();
			setState(273);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__17) {
				{
				{
				setState(265);
				match(T__17);
				setState(269);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 70368911949824L) != 0)) {
					{
					setState(266);
					verb();
					setState(267);
					objectList();
					}
				}

				}
				}
				setState(275);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ObjectListContext extends ParserRuleContext {
		public List<ObjectContext> object() {
			return getRuleContexts(ObjectContext.class);
		}
		public ObjectContext object(int i) {
			return getRuleContext(ObjectContext.class,i);
		}
		public ObjectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterObjectList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitObjectList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitObjectList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectListContext objectList() throws RecognitionException {
		ObjectListContext _localctx = new ObjectListContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_objectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276);
			object();
			setState(281);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__18) {
				{
				{
				setState(277);
				match(T__18);
				setState(278);
				object();
				}
				}
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ObjectContext extends ParserRuleContext {
		public GraphNodeContext graphNode() {
			return getRuleContext(GraphNodeContext.class,0);
		}
		public ObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitObject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_object);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
			graphNode();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VerbContext extends ParserRuleContext {
		public SchemaMorphismOrPathContext schemaMorphismOrPath() {
			return getRuleContext(SchemaMorphismOrPathContext.class,0);
		}
		public VerbContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_verb; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterVerb(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitVerb(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitVerb(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VerbContext verb() throws RecognitionException {
		VerbContext _localctx = new VerbContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_verb);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			schemaMorphismOrPath();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SchemaMorphismOrPathContext extends ParserRuleContext {
		public PathAlternativeContext pathAlternative() {
			return getRuleContext(PathAlternativeContext.class,0);
		}
		public SchemaMorphismOrPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaMorphismOrPath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSchemaMorphismOrPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSchemaMorphismOrPath(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSchemaMorphismOrPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaMorphismOrPathContext schemaMorphismOrPath() throws RecognitionException {
		SchemaMorphismOrPathContext _localctx = new SchemaMorphismOrPathContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_schemaMorphismOrPath);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(288);
			pathAlternative();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PathAlternativeContext extends ParserRuleContext {
		public List<PathSequenceContext> pathSequence() {
			return getRuleContexts(PathSequenceContext.class);
		}
		public PathSequenceContext pathSequence(int i) {
			return getRuleContext(PathSequenceContext.class,i);
		}
		public PathAlternativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathAlternative; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathAlternative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathAlternative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathAlternative(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathAlternativeContext pathAlternative() throws RecognitionException {
		PathAlternativeContext _localctx = new PathAlternativeContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_pathAlternative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(290);
			pathSequence();
			setState(295);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__19) {
				{
				{
				setState(291);
				match(T__19);
				setState(292);
				pathSequence();
				}
				}
				setState(297);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PathSequenceContext extends ParserRuleContext {
		public List<PathWithModContext> pathWithMod() {
			return getRuleContexts(PathWithModContext.class);
		}
		public PathWithModContext pathWithMod(int i) {
			return getRuleContext(PathWithModContext.class,i);
		}
		public PathSequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathSequence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathSequence(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathSequenceContext pathSequence() throws RecognitionException {
		PathSequenceContext _localctx = new PathSequenceContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_pathSequence);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			pathWithMod();
			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__20) {
				{
				{
				setState(299);
				match(T__20);
				setState(300);
				pathWithMod();
				}
				}
				setState(305);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PathWithModContext extends ParserRuleContext {
		public PathPrimaryContext pathPrimary() {
			return getRuleContext(PathPrimaryContext.class,0);
		}
		public PathModContext pathMod() {
			return getRuleContext(PathModContext.class,0);
		}
		public PathWithModContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathWithMod; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathWithMod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathWithMod(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathWithMod(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathWithModContext pathWithMod() throws RecognitionException {
		PathWithModContext _localctx = new PathWithModContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_pathWithMod);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			pathPrimary();
			setState(308);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 29360128L) != 0)) {
				{
				setState(307);
				pathMod();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PathModContext extends ParserRuleContext {
		public PathModContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathMod; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathMod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathMod(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathMod(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathModContext pathMod() throws RecognitionException {
		PathModContext _localctx = new PathModContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_pathMod);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 29360128L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PathPrimaryContext extends ParserRuleContext {
		public SchemaMorphismContext schemaMorphism() {
			return getRuleContext(SchemaMorphismContext.class,0);
		}
		public SchemaMorphismOrPathContext schemaMorphismOrPath() {
			return getRuleContext(SchemaMorphismOrPathContext.class,0);
		}
		public PathPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathPrimary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathPrimaryContext pathPrimary() throws RecognitionException {
		PathPrimaryContext _localctx = new PathPrimaryContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_pathPrimary);
		try {
			setState(317);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__26:
			case SCHEMA_MORPHISM:
				enterOuterAlt(_localctx, 1);
				{
				setState(312);
				schemaMorphism();
				}
				break;
			case T__24:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(313);
				match(T__24);
				setState(314);
				schemaMorphismOrPath();
				setState(315);
				match(T__25);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SchemaMorphismContext extends ParserRuleContext {
		public PrimaryMorphismContext primaryMorphism() {
			return getRuleContext(PrimaryMorphismContext.class,0);
		}
		public DualMorphismContext dualMorphism() {
			return getRuleContext(DualMorphismContext.class,0);
		}
		public SchemaMorphismContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaMorphism; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSchemaMorphism(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSchemaMorphism(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSchemaMorphism(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaMorphismContext schemaMorphism() throws RecognitionException {
		SchemaMorphismContext _localctx = new SchemaMorphismContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_schemaMorphism);
		try {
			setState(321);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SCHEMA_MORPHISM:
				enterOuterAlt(_localctx, 1);
				{
				setState(319);
				primaryMorphism();
				}
				break;
			case T__26:
				enterOuterAlt(_localctx, 2);
				{
				setState(320);
				dualMorphism();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryMorphismContext extends ParserRuleContext {
		public TerminalNode SCHEMA_MORPHISM() { return getToken(QuerycatParser.SCHEMA_MORPHISM, 0); }
		public PrimaryMorphismContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryMorphism; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPrimaryMorphism(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPrimaryMorphism(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPrimaryMorphism(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryMorphismContext primaryMorphism() throws RecognitionException {
		PrimaryMorphismContext _localctx = new PrimaryMorphismContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_primaryMorphism);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			match(SCHEMA_MORPHISM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DualMorphismContext extends ParserRuleContext {
		public PrimaryMorphismContext primaryMorphism() {
			return getRuleContext(PrimaryMorphismContext.class,0);
		}
		public DualMorphismContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dualMorphism; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterDualMorphism(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitDualMorphism(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitDualMorphism(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DualMorphismContext dualMorphism() throws RecognitionException {
		DualMorphismContext _localctx = new DualMorphismContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_dualMorphism);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(325);
			match(T__26);
			setState(326);
			primaryMorphism();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GraphNodeContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public GraphNodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graphNode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGraphNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGraphNode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitGraphNode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GraphNodeContext graphNode() throws RecognitionException {
		GraphNodeContext _localctx = new GraphNodeContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_graphNode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(328);
			term();
			setState(331);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__27) {
				{
				setState(329);
				match(T__27);
				setState(330);
				variable();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TermContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public ComputationContext computation() {
			return getRuleContext(ComputationContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_term);
		try {
			setState(336);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VARIABLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(333);
				variable();
				}
				break;
			case T__43:
			case T__44:
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case INTEGER_POSITIVE:
			case DECIMAL_POSITIVE:
			case DOUBLE_POSITIVE:
			case INTEGER_NEGATIVE:
			case DECIMAL_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case STRING_LITERAL_SINGLE:
			case STRING_LITERAL_DOUBLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(334);
				constant();
				}
				break;
			case T__28:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__34:
				enterOuterAlt(_localctx, 3);
				{
				setState(335);
				computation();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableContext extends ParserRuleContext {
		public TerminalNode VARIABLE() { return getToken(QuerycatParser.VARIABLE, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			match(VARIABLE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstantContext extends ParserRuleContext {
		public NumericLiteralContext numericLiteral() {
			return getRuleContext(NumericLiteralContext.class,0);
		}
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_constant);
		try {
			setState(343);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case INTEGER_POSITIVE:
			case DECIMAL_POSITIVE:
			case DOUBLE_POSITIVE:
			case INTEGER_NEGATIVE:
			case DECIMAL_NEGATIVE:
			case DOUBLE_NEGATIVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(340);
				numericLiteral();
				}
				break;
			case T__43:
			case T__44:
				enterOuterAlt(_localctx, 2);
				{
				setState(341);
				booleanLiteral();
				}
				break;
			case STRING_LITERAL_SINGLE:
			case STRING_LITERAL_DOUBLE:
				enterOuterAlt(_localctx, 3);
				{
				setState(342);
				string();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComputationContext extends ParserRuleContext {
		public AggregationContext aggregation() {
			return getRuleContext(AggregationContext.class,0);
		}
		public TermListContext termList() {
			return getRuleContext(TermListContext.class,0);
		}
		public ComputationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_computation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterComputation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitComputation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitComputation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComputationContext computation() throws RecognitionException {
		ComputationContext _localctx = new ComputationContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_computation);
		try {
			setState(351);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__34:
				enterOuterAlt(_localctx, 1);
				{
				setState(345);
				aggregation();
				}
				break;
			case T__28:
				enterOuterAlt(_localctx, 2);
				{
				setState(346);
				match(T__28);
				setState(347);
				match(T__24);
				setState(348);
				termList();
				setState(349);
				match(T__25);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TermListContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TermListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterTermList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitTermList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitTermList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermListContext termList() throws RecognitionException {
		TermListContext _localctx = new TermListContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_termList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(353);
			term();
			setState(358);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__18) {
				{
				{
				setState(354);
				match(T__18);
				setState(355);
				term();
				}
				}
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AggregationContext extends ParserRuleContext {
		public AggregationFunctionContext aggregationFunction() {
			return getRuleContext(AggregationFunctionContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public DistinctModifierContext distinctModifier() {
			return getRuleContext(DistinctModifierContext.class,0);
		}
		public ReferenceArgumentContext referenceArgument() {
			return getRuleContext(ReferenceArgumentContext.class,0);
		}
		public AggregationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterAggregation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitAggregation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitAggregation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationContext aggregation() throws RecognitionException {
		AggregationContext _localctx = new AggregationContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_aggregation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			aggregationFunction();
			setState(362);
			match(T__24);
			setState(364);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__29) {
				{
				setState(363);
				distinctModifier();
				}
			}

			setState(366);
			expression();
			setState(368);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__18) {
				{
				setState(367);
				referenceArgument();
				}
			}

			setState(370);
			match(T__25);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DistinctModifierContext extends ParserRuleContext {
		public DistinctModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distinctModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterDistinctModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitDistinctModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitDistinctModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistinctModifierContext distinctModifier() throws RecognitionException {
		DistinctModifierContext _localctx = new DistinctModifierContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_distinctModifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(372);
			match(T__29);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReferenceArgumentContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public ReferenceArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterReferenceArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitReferenceArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitReferenceArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceArgumentContext referenceArgument() throws RecognitionException {
		ReferenceArgumentContext _localctx = new ReferenceArgumentContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_referenceArgument);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(374);
			match(T__18);
			setState(375);
			variable();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AggregationFunctionContext extends ParserRuleContext {
		public AggregationFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterAggregationFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitAggregationFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitAggregationFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationFunctionContext aggregationFunction() throws RecognitionException {
		AggregationFunctionContext _localctx = new AggregationFunctionContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_aggregationFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(377);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 66571993088L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ConditionalOrExpressionContext conditionalOrExpression() {
			return getRuleContext(ConditionalOrExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			conditionalOrExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConditionalOrExpressionContext extends ParserRuleContext {
		public List<ConditionalAndExpressionContext> conditionalAndExpression() {
			return getRuleContexts(ConditionalAndExpressionContext.class);
		}
		public ConditionalAndExpressionContext conditionalAndExpression(int i) {
			return getRuleContext(ConditionalAndExpressionContext.class,i);
		}
		public ConditionalOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalOrExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConditionalOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConditionalOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConditionalOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionalOrExpressionContext conditionalOrExpression() throws RecognitionException {
		ConditionalOrExpressionContext _localctx = new ConditionalOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_conditionalOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(381);
			conditionalAndExpression();
			setState(386);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__35) {
				{
				{
				setState(382);
				match(T__35);
				setState(383);
				conditionalAndExpression();
				}
				}
				setState(388);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConditionalAndExpressionContext extends ParserRuleContext {
		public List<ValueLogicalContext> valueLogical() {
			return getRuleContexts(ValueLogicalContext.class);
		}
		public ValueLogicalContext valueLogical(int i) {
			return getRuleContext(ValueLogicalContext.class,i);
		}
		public ConditionalAndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalAndExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConditionalAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConditionalAndExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConditionalAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionalAndExpressionContext conditionalAndExpression() throws RecognitionException {
		ConditionalAndExpressionContext _localctx = new ConditionalAndExpressionContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_conditionalAndExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(389);
			valueLogical();
			setState(394);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__36) {
				{
				{
				setState(390);
				match(T__36);
				setState(391);
				valueLogical();
				}
				}
				setState(396);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueLogicalContext extends ParserRuleContext {
		public RelationalExpressionContext relationalExpression() {
			return getRuleContext(RelationalExpressionContext.class,0);
		}
		public ValueLogicalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueLogical; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterValueLogical(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitValueLogical(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitValueLogical(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueLogicalContext valueLogical() throws RecognitionException {
		ValueLogicalContext _localctx = new ValueLogicalContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_valueLogical);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(397);
			relationalExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RelationalExpressionContext extends ParserRuleContext {
		public List<ExpressionPartContext> expressionPart() {
			return getRuleContexts(ExpressionPartContext.class);
		}
		public ExpressionPartContext expressionPart(int i) {
			return getRuleContext(ExpressionPartContext.class,i);
		}
		public RelationalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitRelationalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitRelationalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationalExpressionContext relationalExpression() throws RecognitionException {
		RelationalExpressionContext _localctx = new RelationalExpressionContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_relationalExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(399);
			expressionPart();
			setState(412);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__37:
				{
				setState(400);
				match(T__37);
				setState(401);
				expressionPart();
				}
				break;
			case T__38:
				{
				setState(402);
				match(T__38);
				setState(403);
				expressionPart();
				}
				break;
			case T__39:
				{
				setState(404);
				match(T__39);
				setState(405);
				expressionPart();
				}
				break;
			case T__40:
				{
				setState(406);
				match(T__40);
				setState(407);
				expressionPart();
				}
				break;
			case T__41:
				{
				setState(408);
				match(T__41);
				setState(409);
				expressionPart();
				}
				break;
			case T__42:
				{
				setState(410);
				match(T__42);
				setState(411);
				expressionPart();
				}
				break;
			case T__18:
			case T__25:
			case T__35:
			case T__36:
				break;
			default:
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionPartContext extends ParserRuleContext {
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public ExpressionPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionPart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterExpressionPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitExpressionPart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitExpressionPart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionPartContext expressionPart() throws RecognitionException {
		ExpressionPartContext _localctx = new ExpressionPartContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_expressionPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(414);
			primaryExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExpressionContext extends ParserRuleContext {
		public BrackettedExpressionContext brackettedExpression() {
			return getRuleContext(BrackettedExpressionContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPrimaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPrimaryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPrimaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_primaryExpression);
		try {
			setState(418);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__24:
				enterOuterAlt(_localctx, 1);
				{
				setState(416);
				brackettedExpression();
				}
				break;
			case T__28:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__34:
			case T__43:
			case T__44:
			case VARIABLE:
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
			case INTEGER_POSITIVE:
			case DECIMAL_POSITIVE:
			case DOUBLE_POSITIVE:
			case INTEGER_NEGATIVE:
			case DECIMAL_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case STRING_LITERAL_SINGLE:
			case STRING_LITERAL_DOUBLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(417);
				term();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BrackettedExpressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BrackettedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_brackettedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterBrackettedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitBrackettedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitBrackettedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BrackettedExpressionContext brackettedExpression() throws RecognitionException {
		BrackettedExpressionContext _localctx = new BrackettedExpressionContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_brackettedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(420);
			match(T__24);
			setState(421);
			expression();
			setState(422);
			match(T__25);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumericLiteralContext extends ParserRuleContext {
		public NumericLiteralUnsignedContext numericLiteralUnsigned() {
			return getRuleContext(NumericLiteralUnsignedContext.class,0);
		}
		public NumericLiteralPositiveContext numericLiteralPositive() {
			return getRuleContext(NumericLiteralPositiveContext.class,0);
		}
		public NumericLiteralNegativeContext numericLiteralNegative() {
			return getRuleContext(NumericLiteralNegativeContext.class,0);
		}
		public NumericLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_numericLiteral);
		try {
			setState(427);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(424);
				numericLiteralUnsigned();
				}
				break;
			case INTEGER_POSITIVE:
			case DECIMAL_POSITIVE:
			case DOUBLE_POSITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(425);
				numericLiteralPositive();
				}
				break;
			case INTEGER_NEGATIVE:
			case DECIMAL_NEGATIVE:
			case DOUBLE_NEGATIVE:
				enterOuterAlt(_localctx, 3);
				{
				setState(426);
				numericLiteralNegative();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumericLiteralUnsignedContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(QuerycatParser.INTEGER, 0); }
		public TerminalNode DECIMAL() { return getToken(QuerycatParser.DECIMAL, 0); }
		public TerminalNode DOUBLE() { return getToken(QuerycatParser.DOUBLE, 0); }
		public NumericLiteralUnsignedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteralUnsigned; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteralUnsigned(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteralUnsigned(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteralUnsigned(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralUnsignedContext numericLiteralUnsigned() throws RecognitionException {
		NumericLiteralUnsignedContext _localctx = new NumericLiteralUnsignedContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_numericLiteralUnsigned);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(429);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3940649673949184L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumericLiteralPositiveContext extends ParserRuleContext {
		public TerminalNode INTEGER_POSITIVE() { return getToken(QuerycatParser.INTEGER_POSITIVE, 0); }
		public TerminalNode DECIMAL_POSITIVE() { return getToken(QuerycatParser.DECIMAL_POSITIVE, 0); }
		public TerminalNode DOUBLE_POSITIVE() { return getToken(QuerycatParser.DOUBLE_POSITIVE, 0); }
		public NumericLiteralPositiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteralPositive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteralPositive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteralPositive(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteralPositive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralPositiveContext numericLiteralPositive() throws RecognitionException {
		NumericLiteralPositiveContext _localctx = new NumericLiteralPositiveContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_numericLiteralPositive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(431);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 31525197391593472L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumericLiteralNegativeContext extends ParserRuleContext {
		public TerminalNode INTEGER_NEGATIVE() { return getToken(QuerycatParser.INTEGER_NEGATIVE, 0); }
		public TerminalNode DECIMAL_NEGATIVE() { return getToken(QuerycatParser.DECIMAL_NEGATIVE, 0); }
		public TerminalNode DOUBLE_NEGATIVE() { return getToken(QuerycatParser.DOUBLE_NEGATIVE, 0); }
		public NumericLiteralNegativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteralNegative; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteralNegative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteralNegative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteralNegative(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericLiteralNegativeContext numericLiteralNegative() throws RecognitionException {
		NumericLiteralNegativeContext _localctx = new NumericLiteralNegativeContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_numericLiteralNegative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(433);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 252201579132747776L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BooleanLiteralContext extends ParserRuleContext {
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(435);
			_la = _input.LA(1);
			if ( !(_la==T__43 || _la==T__44) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL_SINGLE() { return getToken(QuerycatParser.STRING_LITERAL_SINGLE, 0); }
		public TerminalNode STRING_LITERAL_DOUBLE() { return getToken(QuerycatParser.STRING_LITERAL_DOUBLE, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(437);
			_la = _input.LA(1);
			if ( !(_la==STRING_LITERAL_SINGLE || _la==STRING_LITERAL_DOUBLE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001A\u01b8\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u00076\u0002"+
		"7\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007;\u0002"+
		"<\u0007<\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0003"+
		"\u0001\u0080\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0003"+
		"\u0004\u008c\b\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0003\u0006\u0094\b\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0003\u0007\u0099\b\u0007\u0001\u0007\u0003\u0007\u009c\b\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0004\b\u00a1\b\b\u000b\b\f\b\u00a2\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0003\t\u00a9\b\t\u0003\t\u00ab\b\t\u0001\n\u0001\n"+
		"\u0003\n\u00af\b\n\u0001\n\u0001\n\u0003\n\u00b3\b\n\u0003\n\u00b5\b\n"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\r\u0001"+
		"\r\u0001\r\u0003\r\u00c0\b\r\u0001\r\u0001\r\u0003\r\u00c4\b\r\u0001\r"+
		"\u0003\r\u00c7\b\r\u0001\r\u0003\r\u00ca\b\r\u0005\r\u00cc\b\r\n\r\f\r"+
		"\u00cf\t\r\u0003\r\u00d1\b\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0003\u000e\u00d8\b\u000e\u0003\u000e\u00da\b\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0003\u000f\u00df\b\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u00e7\b\u0011"+
		"\n\u0011\f\u0011\u00ea\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00f2\b\u0013\n\u0013\f\u0013"+
		"\u00f5\t\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016"+
		"\u0101\b\u0016\u0003\u0016\u0103\b\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0003\u0018\u010e\b\u0018\u0005\u0018\u0110\b\u0018\n\u0018\f\u0018"+
		"\u0113\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u0118\b"+
		"\u0019\n\u0019\f\u0019\u011b\t\u0019\u0001\u001a\u0001\u001a\u0001\u001b"+
		"\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0005\u001d\u0126\b\u001d\n\u001d\f\u001d\u0129\t\u001d\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0005\u001e\u012e\b\u001e\n\u001e\f\u001e\u0131\t\u001e"+
		"\u0001\u001f\u0001\u001f\u0003\u001f\u0135\b\u001f\u0001 \u0001 \u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0003!\u013e\b!\u0001\"\u0001\"\u0003\""+
		"\u0142\b\"\u0001#\u0001#\u0001$\u0001$\u0001$\u0001%\u0001%\u0001%\u0003"+
		"%\u014c\b%\u0001&\u0001&\u0001&\u0003&\u0151\b&\u0001\'\u0001\'\u0001"+
		"(\u0001(\u0001(\u0003(\u0158\b(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001"+
		")\u0003)\u0160\b)\u0001*\u0001*\u0001*\u0005*\u0165\b*\n*\f*\u0168\t*"+
		"\u0001+\u0001+\u0001+\u0003+\u016d\b+\u0001+\u0001+\u0003+\u0171\b+\u0001"+
		"+\u0001+\u0001,\u0001,\u0001-\u0001-\u0001-\u0001.\u0001.\u0001/\u0001"+
		"/\u00010\u00010\u00010\u00050\u0181\b0\n0\f0\u0184\t0\u00011\u00011\u0001"+
		"1\u00051\u0189\b1\n1\f1\u018c\t1\u00012\u00012\u00013\u00013\u00013\u0001"+
		"3\u00013\u00013\u00013\u00013\u00013\u00013\u00013\u00013\u00013\u0003"+
		"3\u019d\b3\u00014\u00014\u00015\u00015\u00035\u01a3\b5\u00016\u00016\u0001"+
		"6\u00016\u00017\u00017\u00017\u00037\u01ac\b7\u00018\u00018\u00019\u0001"+
		"9\u0001:\u0001:\u0001;\u0001;\u0001<\u0001<\u0001<\u0000\u0000=\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvx\u0000\t\u0001\u0000"+
		"\b\t\u0001\u0000\u000e\u000f\u0001\u0000\u0016\u0018\u0001\u0000\u001f"+
		"#\u0001\u000013\u0001\u000046\u0001\u000079\u0001\u0000,-\u0001\u0000"+
		";<\u01af\u0000z\u0001\u0000\u0000\u0000\u0002}\u0001\u0000\u0000\u0000"+
		"\u0004\u0084\u0001\u0000\u0000\u0000\u0006\u0086\u0001\u0000\u0000\u0000"+
		"\b\u0089\u0001\u0000\u0000\u0000\n\u008f\u0001\u0000\u0000\u0000\f\u0093"+
		"\u0001\u0000\u0000\u0000\u000e\u0098\u0001\u0000\u0000\u0000\u0010\u009d"+
		"\u0001\u0000\u0000\u0000\u0012\u00aa\u0001\u0000\u0000\u0000\u0014\u00b4"+
		"\u0001\u0000\u0000\u0000\u0016\u00b6\u0001\u0000\u0000\u0000\u0018\u00b9"+
		"\u0001\u0000\u0000\u0000\u001a\u00bc\u0001\u0000\u0000\u0000\u001c\u00d4"+
		"\u0001\u0000\u0000\u0000\u001e\u00de\u0001\u0000\u0000\u0000 \u00e0\u0001"+
		"\u0000\u0000\u0000\"\u00e3\u0001\u0000\u0000\u0000$\u00eb\u0001\u0000"+
		"\u0000\u0000&\u00ee\u0001\u0000\u0000\u0000(\u00f8\u0001\u0000\u0000\u0000"+
		"*\u00fb\u0001\u0000\u0000\u0000,\u00fd\u0001\u0000\u0000\u0000.\u0104"+
		"\u0001\u0000\u0000\u00000\u0107\u0001\u0000\u0000\u00002\u0114\u0001\u0000"+
		"\u0000\u00004\u011c\u0001\u0000\u0000\u00006\u011e\u0001\u0000\u0000\u0000"+
		"8\u0120\u0001\u0000\u0000\u0000:\u0122\u0001\u0000\u0000\u0000<\u012a"+
		"\u0001\u0000\u0000\u0000>\u0132\u0001\u0000\u0000\u0000@\u0136\u0001\u0000"+
		"\u0000\u0000B\u013d\u0001\u0000\u0000\u0000D\u0141\u0001\u0000\u0000\u0000"+
		"F\u0143\u0001\u0000\u0000\u0000H\u0145\u0001\u0000\u0000\u0000J\u0148"+
		"\u0001\u0000\u0000\u0000L\u0150\u0001\u0000\u0000\u0000N\u0152\u0001\u0000"+
		"\u0000\u0000P\u0157\u0001\u0000\u0000\u0000R\u015f\u0001\u0000\u0000\u0000"+
		"T\u0161\u0001\u0000\u0000\u0000V\u0169\u0001\u0000\u0000\u0000X\u0174"+
		"\u0001\u0000\u0000\u0000Z\u0176\u0001\u0000\u0000\u0000\\\u0179\u0001"+
		"\u0000\u0000\u0000^\u017b\u0001\u0000\u0000\u0000`\u017d\u0001\u0000\u0000"+
		"\u0000b\u0185\u0001\u0000\u0000\u0000d\u018d\u0001\u0000\u0000\u0000f"+
		"\u018f\u0001\u0000\u0000\u0000h\u019e\u0001\u0000\u0000\u0000j\u01a2\u0001"+
		"\u0000\u0000\u0000l\u01a4\u0001\u0000\u0000\u0000n\u01ab\u0001\u0000\u0000"+
		"\u0000p\u01ad\u0001\u0000\u0000\u0000r\u01af\u0001\u0000\u0000\u0000t"+
		"\u01b1\u0001\u0000\u0000\u0000v\u01b3\u0001\u0000\u0000\u0000x\u01b5\u0001"+
		"\u0000\u0000\u0000z{\u0003\u0002\u0001\u0000{|\u0005\u0000\u0000\u0001"+
		"|\u0001\u0001\u0000\u0000\u0000}\u007f\u0003\u0006\u0003\u0000~\u0080"+
		"\u0003\n\u0005\u0000\u007f~\u0001\u0000\u0000\u0000\u007f\u0080\u0001"+
		"\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000\u0000\u0081\u0082\u0003"+
		"\f\u0006\u0000\u0082\u0083\u0003\u000e\u0007\u0000\u0083\u0003\u0001\u0000"+
		"\u0000\u0000\u0084\u0085\u0003\u0002\u0001\u0000\u0085\u0005\u0001\u0000"+
		"\u0000\u0000\u0086\u0087\u0005\u0001\u0000\u0000\u0087\u0088\u0003\b\u0004"+
		"\u0000\u0088\u0007\u0001\u0000\u0000\u0000\u0089\u008b\u0005\u0002\u0000"+
		"\u0000\u008a\u008c\u0003,\u0016\u0000\u008b\u008a\u0001\u0000\u0000\u0000"+
		"\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000\u0000"+
		"\u008d\u008e\u0005\u0003\u0000\u0000\u008e\t\u0001\u0000\u0000\u0000\u008f"+
		"\u0090\u0005\u0004\u0000\u0000\u0090\u0091\u0005/\u0000\u0000\u0091\u000b"+
		"\u0001\u0000\u0000\u0000\u0092\u0094\u0005\u0005\u0000\u0000\u0093\u0092"+
		"\u0001\u0000\u0000\u0000\u0093\u0094\u0001\u0000\u0000\u0000\u0094\u0095"+
		"\u0001\u0000\u0000\u0000\u0095\u0096\u0003\u001a\r\u0000\u0096\r\u0001"+
		"\u0000\u0000\u0000\u0097\u0099\u0003\u0010\b\u0000\u0098\u0097\u0001\u0000"+
		"\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u009b\u0001\u0000"+
		"\u0000\u0000\u009a\u009c\u0003\u0014\n\u0000\u009b\u009a\u0001\u0000\u0000"+
		"\u0000\u009b\u009c\u0001\u0000\u0000\u0000\u009c\u000f\u0001\u0000\u0000"+
		"\u0000\u009d\u009e\u0005\u0006\u0000\u0000\u009e\u00a0\u0005\u0007\u0000"+
		"\u0000\u009f\u00a1\u0003\u0012\t\u0000\u00a0\u009f\u0001\u0000\u0000\u0000"+
		"\u00a1\u00a2\u0001\u0000\u0000\u0000\u00a2\u00a0\u0001\u0000\u0000\u0000"+
		"\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u0011\u0001\u0000\u0000\u0000"+
		"\u00a4\u00a5\u0007\u0000\u0000\u0000\u00a5\u00ab\u0003l6\u0000\u00a6\u00a9"+
		"\u0003*\u0015\u0000\u00a7\u00a9\u0003N\'\u0000\u00a8\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a8\u00a7\u0001\u0000\u0000\u0000\u00a9\u00ab\u0001\u0000"+
		"\u0000\u0000\u00aa\u00a4\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000"+
		"\u0000\u0000\u00ab\u0013\u0001\u0000\u0000\u0000\u00ac\u00ae\u0003\u0016"+
		"\u000b\u0000\u00ad\u00af\u0003\u0018\f\u0000\u00ae\u00ad\u0001\u0000\u0000"+
		"\u0000\u00ae\u00af\u0001\u0000\u0000\u0000\u00af\u00b5\u0001\u0000\u0000"+
		"\u0000\u00b0\u00b2\u0003\u0018\f\u0000\u00b1\u00b3\u0003\u0016\u000b\u0000"+
		"\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000"+
		"\u00b3\u00b5\u0001\u0000\u0000\u0000\u00b4\u00ac\u0001\u0000\u0000\u0000"+
		"\u00b4\u00b0\u0001\u0000\u0000\u0000\u00b5\u0015\u0001\u0000\u0000\u0000"+
		"\u00b6\u00b7\u0005\n\u0000\u0000\u00b7\u00b8\u00051\u0000\u0000\u00b8"+
		"\u0017\u0001\u0000\u0000\u0000\u00b9\u00ba\u0005\u000b\u0000\u0000\u00ba"+
		"\u00bb\u00051\u0000\u0000\u00bb\u0019\u0001\u0000\u0000\u0000\u00bc\u00d0"+
		"\u0005\u0002\u0000\u0000\u00bd\u00d1\u0003\u0004\u0002\u0000\u00be\u00c0"+
		"\u0003\u001c\u000e\u0000\u00bf\u00be\u0001\u0000\u0000\u0000\u00bf\u00c0"+
		"\u0001\u0000\u0000\u0000\u00c0\u00cd\u0001\u0000\u0000\u0000\u00c1\u00c4"+
		"\u0003\u001e\u000f\u0000\u00c2\u00c4\u0003(\u0014\u0000\u00c3\u00c1\u0001"+
		"\u0000\u0000\u0000\u00c3\u00c2\u0001\u0000\u0000\u0000\u00c4\u00c6\u0001"+
		"\u0000\u0000\u0000\u00c5\u00c7\u0005\f\u0000\u0000\u00c6\u00c5\u0001\u0000"+
		"\u0000\u0000\u00c6\u00c7\u0001\u0000\u0000\u0000\u00c7\u00c9\u0001\u0000"+
		"\u0000\u0000\u00c8\u00ca\u0003\u001c\u000e\u0000\u00c9\u00c8\u0001\u0000"+
		"\u0000\u0000\u00c9\u00ca\u0001\u0000\u0000\u0000\u00ca\u00cc\u0001\u0000"+
		"\u0000\u0000\u00cb\u00c3\u0001\u0000\u0000\u0000\u00cc\u00cf\u0001\u0000"+
		"\u0000\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00cd\u00ce\u0001\u0000"+
		"\u0000\u0000\u00ce\u00d1\u0001\u0000\u0000\u0000\u00cf\u00cd\u0001\u0000"+
		"\u0000\u0000\u00d0\u00bd\u0001\u0000\u0000\u0000\u00d0\u00bf\u0001\u0000"+
		"\u0000\u0000\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2\u00d3\u0005\u0003"+
		"\u0000\u0000\u00d3\u001b\u0001\u0000\u0000\u0000\u00d4\u00d9\u0003.\u0017"+
		"\u0000\u00d5\u00d7\u0005\f\u0000\u0000\u00d6\u00d8\u0003\u001c\u000e\u0000"+
		"\u00d7\u00d6\u0001\u0000\u0000\u0000\u00d7\u00d8\u0001\u0000\u0000\u0000"+
		"\u00d8\u00da\u0001\u0000\u0000\u0000\u00d9\u00d5\u0001\u0000\u0000\u0000"+
		"\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u001d\u0001\u0000\u0000\u0000"+
		"\u00db\u00df\u0003 \u0010\u0000\u00dc\u00df\u0003\"\u0011\u0000\u00dd"+
		"\u00df\u0003$\u0012\u0000\u00de\u00db\u0001\u0000\u0000\u0000\u00de\u00dc"+
		"\u0001\u0000\u0000\u0000\u00de\u00dd\u0001\u0000\u0000\u0000\u00df\u001f"+
		"\u0001\u0000\u0000\u0000\u00e0\u00e1\u0005\r\u0000\u0000\u00e1\u00e2\u0003"+
		"\u001a\r\u0000\u00e2!\u0001\u0000\u0000\u0000\u00e3\u00e8\u0003\u001a"+
		"\r\u0000\u00e4\u00e5\u0007\u0001\u0000\u0000\u00e5\u00e7\u0003\u001a\r"+
		"\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e7\u00ea\u0001\u0000\u0000"+
		"\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e8\u00e9\u0001\u0000\u0000"+
		"\u0000\u00e9#\u0001\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000\u0000\u0000"+
		"\u00eb\u00ec\u0005\u0010\u0000\u0000\u00ec\u00ed\u0003&\u0013\u0000\u00ed"+
		"%\u0001\u0000\u0000\u0000\u00ee\u00ef\u0003N\'\u0000\u00ef\u00f3\u0005"+
		"\u0002\u0000\u0000\u00f0\u00f2\u0003P(\u0000\u00f1\u00f0\u0001\u0000\u0000"+
		"\u0000\u00f2\u00f5\u0001\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000"+
		"\u0000\u00f3\u00f4\u0001\u0000\u0000\u0000\u00f4\u00f6\u0001\u0000\u0000"+
		"\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000\u00f6\u00f7\u0005\u0003\u0000"+
		"\u0000\u00f7\'\u0001\u0000\u0000\u0000\u00f8\u00f9\u0005\u0011\u0000\u0000"+
		"\u00f9\u00fa\u0003*\u0015\u0000\u00fa)\u0001\u0000\u0000\u0000\u00fb\u00fc"+
		"\u0003l6\u0000\u00fc+\u0001\u0000\u0000\u0000\u00fd\u0102\u0003.\u0017"+
		"\u0000\u00fe\u0100\u0005\f\u0000\u0000\u00ff\u0101\u0003,\u0016\u0000"+
		"\u0100\u00ff\u0001\u0000\u0000\u0000\u0100\u0101\u0001\u0000\u0000\u0000"+
		"\u0101\u0103\u0001\u0000\u0000\u0000\u0102\u00fe\u0001\u0000\u0000\u0000"+
		"\u0102\u0103\u0001\u0000\u0000\u0000\u0103-\u0001\u0000\u0000\u0000\u0104"+
		"\u0105\u0003L&\u0000\u0105\u0106\u00030\u0018\u0000\u0106/\u0001\u0000"+
		"\u0000\u0000\u0107\u0108\u00036\u001b\u0000\u0108\u0111\u00032\u0019\u0000"+
		"\u0109\u010d\u0005\u0012\u0000\u0000\u010a\u010b\u00036\u001b\u0000\u010b"+
		"\u010c\u00032\u0019\u0000\u010c\u010e\u0001\u0000\u0000\u0000\u010d\u010a"+
		"\u0001\u0000\u0000\u0000\u010d\u010e\u0001\u0000\u0000\u0000\u010e\u0110"+
		"\u0001\u0000\u0000\u0000\u010f\u0109\u0001\u0000\u0000\u0000\u0110\u0113"+
		"\u0001\u0000\u0000\u0000\u0111\u010f\u0001\u0000\u0000\u0000\u0111\u0112"+
		"\u0001\u0000\u0000\u0000\u01121\u0001\u0000\u0000\u0000\u0113\u0111\u0001"+
		"\u0000\u0000\u0000\u0114\u0119\u00034\u001a\u0000\u0115\u0116\u0005\u0013"+
		"\u0000\u0000\u0116\u0118\u00034\u001a\u0000\u0117\u0115\u0001\u0000\u0000"+
		"\u0000\u0118\u011b\u0001\u0000\u0000\u0000\u0119\u0117\u0001\u0000\u0000"+
		"\u0000\u0119\u011a\u0001\u0000\u0000\u0000\u011a3\u0001\u0000\u0000\u0000"+
		"\u011b\u0119\u0001\u0000\u0000\u0000\u011c\u011d\u0003J%\u0000\u011d5"+
		"\u0001\u0000\u0000\u0000\u011e\u011f\u00038\u001c\u0000\u011f7\u0001\u0000"+
		"\u0000\u0000\u0120\u0121\u0003:\u001d\u0000\u01219\u0001\u0000\u0000\u0000"+
		"\u0122\u0127\u0003<\u001e\u0000\u0123\u0124\u0005\u0014\u0000\u0000\u0124"+
		"\u0126\u0003<\u001e\u0000\u0125\u0123\u0001\u0000\u0000\u0000\u0126\u0129"+
		"\u0001\u0000\u0000\u0000\u0127\u0125\u0001\u0000\u0000\u0000\u0127\u0128"+
		"\u0001\u0000\u0000\u0000\u0128;\u0001\u0000\u0000\u0000\u0129\u0127\u0001"+
		"\u0000\u0000\u0000\u012a\u012f\u0003>\u001f\u0000\u012b\u012c\u0005\u0015"+
		"\u0000\u0000\u012c\u012e\u0003>\u001f\u0000\u012d\u012b\u0001\u0000\u0000"+
		"\u0000\u012e\u0131\u0001\u0000\u0000\u0000\u012f\u012d\u0001\u0000\u0000"+
		"\u0000\u012f\u0130\u0001\u0000\u0000\u0000\u0130=\u0001\u0000\u0000\u0000"+
		"\u0131\u012f\u0001\u0000\u0000\u0000\u0132\u0134\u0003B!\u0000\u0133\u0135"+
		"\u0003@ \u0000\u0134\u0133\u0001\u0000\u0000\u0000\u0134\u0135\u0001\u0000"+
		"\u0000\u0000\u0135?\u0001\u0000\u0000\u0000\u0136\u0137\u0007\u0002\u0000"+
		"\u0000\u0137A\u0001\u0000\u0000\u0000\u0138\u013e\u0003D\"\u0000\u0139"+
		"\u013a\u0005\u0019\u0000\u0000\u013a\u013b\u00038\u001c\u0000\u013b\u013c"+
		"\u0005\u001a\u0000\u0000\u013c\u013e\u0001\u0000\u0000\u0000\u013d\u0138"+
		"\u0001\u0000\u0000\u0000\u013d\u0139\u0001\u0000\u0000\u0000\u013eC\u0001"+
		"\u0000\u0000\u0000\u013f\u0142\u0003F#\u0000\u0140\u0142\u0003H$\u0000"+
		"\u0141\u013f\u0001\u0000\u0000\u0000\u0141\u0140\u0001\u0000\u0000\u0000"+
		"\u0142E\u0001\u0000\u0000\u0000\u0143\u0144\u0005.\u0000\u0000\u0144G"+
		"\u0001\u0000\u0000\u0000\u0145\u0146\u0005\u001b\u0000\u0000\u0146\u0147"+
		"\u0003F#\u0000\u0147I\u0001\u0000\u0000\u0000\u0148\u014b\u0003L&\u0000"+
		"\u0149\u014a\u0005\u001c\u0000\u0000\u014a\u014c\u0003N\'\u0000\u014b"+
		"\u0149\u0001\u0000\u0000\u0000\u014b\u014c\u0001\u0000\u0000\u0000\u014c"+
		"K\u0001\u0000\u0000\u0000\u014d\u0151\u0003N\'\u0000\u014e\u0151\u0003"+
		"P(\u0000\u014f\u0151\u0003R)\u0000\u0150\u014d\u0001\u0000\u0000\u0000"+
		"\u0150\u014e\u0001\u0000\u0000\u0000\u0150\u014f\u0001\u0000\u0000\u0000"+
		"\u0151M\u0001\u0000\u0000\u0000\u0152\u0153\u00050\u0000\u0000\u0153O"+
		"\u0001\u0000\u0000\u0000\u0154\u0158\u0003n7\u0000\u0155\u0158\u0003v"+
		";\u0000\u0156\u0158\u0003x<\u0000\u0157\u0154\u0001\u0000\u0000\u0000"+
		"\u0157\u0155\u0001\u0000\u0000\u0000\u0157\u0156\u0001\u0000\u0000\u0000"+
		"\u0158Q\u0001\u0000\u0000\u0000\u0159\u0160\u0003V+\u0000\u015a\u015b"+
		"\u0005\u001d\u0000\u0000\u015b\u015c\u0005\u0019\u0000\u0000\u015c\u015d"+
		"\u0003T*\u0000\u015d\u015e\u0005\u001a\u0000\u0000\u015e\u0160\u0001\u0000"+
		"\u0000\u0000\u015f\u0159\u0001\u0000\u0000\u0000\u015f\u015a\u0001\u0000"+
		"\u0000\u0000\u0160S\u0001\u0000\u0000\u0000\u0161\u0166\u0003L&\u0000"+
		"\u0162\u0163\u0005\u0013\u0000\u0000\u0163\u0165\u0003L&\u0000\u0164\u0162"+
		"\u0001\u0000\u0000\u0000\u0165\u0168\u0001\u0000\u0000\u0000\u0166\u0164"+
		"\u0001\u0000\u0000\u0000\u0166\u0167\u0001\u0000\u0000\u0000\u0167U\u0001"+
		"\u0000\u0000\u0000\u0168\u0166\u0001\u0000\u0000\u0000\u0169\u016a\u0003"+
		"\\.\u0000\u016a\u016c\u0005\u0019\u0000\u0000\u016b\u016d\u0003X,\u0000"+
		"\u016c\u016b\u0001\u0000\u0000\u0000\u016c\u016d\u0001\u0000\u0000\u0000"+
		"\u016d\u016e\u0001\u0000\u0000\u0000\u016e\u0170\u0003^/\u0000\u016f\u0171"+
		"\u0003Z-\u0000\u0170\u016f\u0001\u0000\u0000\u0000\u0170\u0171\u0001\u0000"+
		"\u0000\u0000\u0171\u0172\u0001\u0000\u0000\u0000\u0172\u0173\u0005\u001a"+
		"\u0000\u0000\u0173W\u0001\u0000\u0000\u0000\u0174\u0175\u0005\u001e\u0000"+
		"\u0000\u0175Y\u0001\u0000\u0000\u0000\u0176\u0177\u0005\u0013\u0000\u0000"+
		"\u0177\u0178\u0003N\'\u0000\u0178[\u0001\u0000\u0000\u0000\u0179\u017a"+
		"\u0007\u0003\u0000\u0000\u017a]\u0001\u0000\u0000\u0000\u017b\u017c\u0003"+
		"`0\u0000\u017c_\u0001\u0000\u0000\u0000\u017d\u0182\u0003b1\u0000\u017e"+
		"\u017f\u0005$\u0000\u0000\u017f\u0181\u0003b1\u0000\u0180\u017e\u0001"+
		"\u0000\u0000\u0000\u0181\u0184\u0001\u0000\u0000\u0000\u0182\u0180\u0001"+
		"\u0000\u0000\u0000\u0182\u0183\u0001\u0000\u0000\u0000\u0183a\u0001\u0000"+
		"\u0000\u0000\u0184\u0182\u0001\u0000\u0000\u0000\u0185\u018a\u0003d2\u0000"+
		"\u0186\u0187\u0005%\u0000\u0000\u0187\u0189\u0003d2\u0000\u0188\u0186"+
		"\u0001\u0000\u0000\u0000\u0189\u018c\u0001\u0000\u0000\u0000\u018a\u0188"+
		"\u0001\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000\u0000\u018bc\u0001"+
		"\u0000\u0000\u0000\u018c\u018a\u0001\u0000\u0000\u0000\u018d\u018e\u0003"+
		"f3\u0000\u018ee\u0001\u0000\u0000\u0000\u018f\u019c\u0003h4\u0000\u0190"+
		"\u0191\u0005&\u0000\u0000\u0191\u019d\u0003h4\u0000\u0192\u0193\u0005"+
		"\'\u0000\u0000\u0193\u019d\u0003h4\u0000\u0194\u0195\u0005(\u0000\u0000"+
		"\u0195\u019d\u0003h4\u0000\u0196\u0197\u0005)\u0000\u0000\u0197\u019d"+
		"\u0003h4\u0000\u0198\u0199\u0005*\u0000\u0000\u0199\u019d\u0003h4\u0000"+
		"\u019a\u019b\u0005+\u0000\u0000\u019b\u019d\u0003h4\u0000\u019c\u0190"+
		"\u0001\u0000\u0000\u0000\u019c\u0192\u0001\u0000\u0000\u0000\u019c\u0194"+
		"\u0001\u0000\u0000\u0000\u019c\u0196\u0001\u0000\u0000\u0000\u019c\u0198"+
		"\u0001\u0000\u0000\u0000\u019c\u019a\u0001\u0000\u0000\u0000\u019c\u019d"+
		"\u0001\u0000\u0000\u0000\u019dg\u0001\u0000\u0000\u0000\u019e\u019f\u0003"+
		"j5\u0000\u019fi\u0001\u0000\u0000\u0000\u01a0\u01a3\u0003l6\u0000\u01a1"+
		"\u01a3\u0003L&\u0000\u01a2\u01a0\u0001\u0000\u0000\u0000\u01a2\u01a1\u0001"+
		"\u0000\u0000\u0000\u01a3k\u0001\u0000\u0000\u0000\u01a4\u01a5\u0005\u0019"+
		"\u0000\u0000\u01a5\u01a6\u0003^/\u0000\u01a6\u01a7\u0005\u001a\u0000\u0000"+
		"\u01a7m\u0001\u0000\u0000\u0000\u01a8\u01ac\u0003p8\u0000\u01a9\u01ac"+
		"\u0003r9\u0000\u01aa\u01ac\u0003t:\u0000\u01ab\u01a8\u0001\u0000\u0000"+
		"\u0000\u01ab\u01a9\u0001\u0000\u0000\u0000\u01ab\u01aa\u0001\u0000\u0000"+
		"\u0000\u01aco\u0001\u0000\u0000\u0000\u01ad\u01ae\u0007\u0004\u0000\u0000"+
		"\u01aeq\u0001\u0000\u0000\u0000\u01af\u01b0\u0007\u0005\u0000\u0000\u01b0"+
		"s\u0001\u0000\u0000\u0000\u01b1\u01b2\u0007\u0006\u0000\u0000\u01b2u\u0001"+
		"\u0000\u0000\u0000\u01b3\u01b4\u0007\u0007\u0000\u0000\u01b4w\u0001\u0000"+
		"\u0000\u0000\u01b5\u01b6\u0007\b\u0000\u0000\u01b6y\u0001\u0000\u0000"+
		"\u0000,\u007f\u008b\u0093\u0098\u009b\u00a2\u00a8\u00aa\u00ae\u00b2\u00b4"+
		"\u00bf\u00c3\u00c6\u00c9\u00cd\u00d0\u00d7\u00d9\u00de\u00e8\u00f3\u0100"+
		"\u0102\u010d\u0111\u0119\u0127\u012f\u0134\u013d\u0141\u014b\u0150\u0157"+
		"\u015f\u0166\u016c\u0170\u0182\u018a\u019c\u01a2\u01ab";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}