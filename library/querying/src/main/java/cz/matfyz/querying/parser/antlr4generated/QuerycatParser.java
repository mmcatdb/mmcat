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
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, SCHEMA_MORPHISM=45, 
		VARIABLE=46, INTEGER=47, DECIMAL=48, DOUBLE=49, INTEGER_POSITIVE=50, DECIMAL_POSITIVE=51, 
		DOUBLE_POSITIVE=52, INTEGER_NEGATIVE=53, DECIMAL_NEGATIVE=54, DOUBLE_NEGATIVE=55, 
		EXPONENT=56, STRING_LITERAL_SINGLE=57, STRING_LITERAL_DOUBLE=58, ESCAPE_CHAR=59, 
		VARNAME=60, PN_CHARS_U=61, WS=62, COMMENT=63;
	public static final int
		RULE_query = 0, RULE_selectQuery = 1, RULE_subSelect = 2, RULE_selectClause = 3, 
		RULE_whereClause = 4, RULE_solutionModifier = 5, RULE_orderClause = 6, 
		RULE_orderCondition = 7, RULE_limitOffsetClauses = 8, RULE_limitClause = 9, 
		RULE_offsetClause = 10, RULE_graphPattern = 11, RULE_graphPatternInner = 12, 
		RULE_nonTriples = 13, RULE_unionGraphPattern = 14, RULE_optionalGraphPattern = 15, 
		RULE_filter = 16, RULE_constraint = 17, RULE_inlineValues = 18, RULE_selectTriples = 19, 
		RULE_triplesBlock = 20, RULE_triplesSameSubject = 21, RULE_propertyListNotEmpty = 22, 
		RULE_objectList = 23, RULE_object = 24, RULE_verb = 25, RULE_schemaMorphismOrPath = 26, 
		RULE_pathAlternative = 27, RULE_pathSequence = 28, RULE_pathWithMod = 29, 
		RULE_pathMod = 30, RULE_pathPrimary = 31, RULE_schemaMorphism = 32, RULE_primaryMorphism = 33, 
		RULE_dualMorphism = 34, RULE_graphNode = 35, RULE_term = 36, RULE_variable = 37, 
		RULE_constant = 38, RULE_computation = 39, RULE_termList = 40, RULE_aggregation = 41, 
		RULE_distinctModifier = 42, RULE_referenceArgument = 43, RULE_aggregationFunction = 44, 
		RULE_expression = 45, RULE_conditionalOrExpression = 46, RULE_conditionalAndExpression = 47, 
		RULE_valueLogical = 48, RULE_relationalExpression = 49, RULE_expressionPart = 50, 
		RULE_primaryExpression = 51, RULE_brackettedExpression = 52, RULE_numericLiteral = 53, 
		RULE_numericLiteralUnsigned = 54, RULE_numericLiteralPositive = 55, RULE_numericLiteralNegative = 56, 
		RULE_booleanLiteral = 57, RULE_string = 58;
	private static String[] makeRuleNames() {
		return new String[] {
			"query", "selectQuery", "subSelect", "selectClause", "whereClause", "solutionModifier", 
			"orderClause", "orderCondition", "limitOffsetClauses", "limitClause", 
			"offsetClause", "graphPattern", "graphPatternInner", "nonTriples", "unionGraphPattern", 
			"optionalGraphPattern", "filter", "constraint", "inlineValues", "selectTriples", 
			"triplesBlock", "triplesSameSubject", "propertyListNotEmpty", "objectList", 
			"object", "verb", "schemaMorphismOrPath", "pathAlternative", "pathSequence", 
			"pathWithMod", "pathMod", "pathPrimary", "schemaMorphism", "primaryMorphism", 
			"dualMorphism", "graphNode", "term", "variable", "constant", "computation", 
			"termList", "aggregation", "distinctModifier", "referenceArgument", "aggregationFunction", 
			"expression", "conditionalOrExpression", "conditionalAndExpression", 
			"valueLogical", "relationalExpression", "expressionPart", "primaryExpression", 
			"brackettedExpression", "numericLiteral", "numericLiteralUnsigned", "numericLiteralPositive", 
			"numericLiteralNegative", "booleanLiteral", "string"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'SELECT'", "'{'", "'}'", "'WHERE'", "'ORDER'", "'BY'", "'ASC'", 
			"'DESC'", "'LIMIT'", "'OFFSET'", "'UNION'", "'MINUS'", "'OPTIONAL'", 
			"'FILTER'", "'VALUES'", "'.'", "';'", "','", "'|'", "'/'", "'?'", "'*'", 
			"'+'", "'('", "')'", "'-'", "'AS'", "'CONCAT'", "'DISTINCT'", "'COUNT'", 
			"'SUM'", "'AVG'", "'MIN'", "'MAX'", "'||'", "'&&'", "'='", "'!='", "'<'", 
			"'>'", "'<='", "'>='", "'true'", "'false'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, "SCHEMA_MORPHISM", 
			"VARIABLE", "INTEGER", "DECIMAL", "DOUBLE", "INTEGER_POSITIVE", "DECIMAL_POSITIVE", 
			"DOUBLE_POSITIVE", "INTEGER_NEGATIVE", "DECIMAL_NEGATIVE", "DOUBLE_NEGATIVE", 
			"EXPONENT", "STRING_LITERAL_SINGLE", "STRING_LITERAL_DOUBLE", "ESCAPE_CHAR", 
			"VARNAME", "PN_CHARS_U", "WS", "COMMENT"
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
			setState(118);
			selectQuery();
			setState(119);
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121);
			selectClause();
			setState(122);
			whereClause();
			setState(123);
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
			setState(125);
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
		public SelectTriplesContext selectTriples() {
			return getRuleContext(SelectTriplesContext.class,0);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(T__0);
			setState(128);
			match(T__1);
			setState(130);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 504359211354816512L) != 0)) {
				{
				setState(129);
				selectTriples();
				}
			}

			setState(132);
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
	public static class WhereClauseContext extends ParserRuleContext {
		public GraphPatternContext graphPattern() {
			return getRuleContext(GraphPatternContext.class,0);
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
		enterRule(_localctx, 8, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(T__3);
			setState(135);
			graphPattern();
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
		enterRule(_localctx, 10, RULE_solutionModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(137);
				orderClause();
				}
			}

			setState(141);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8 || _la==T__9) {
				{
				setState(140);
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
		enterRule(_localctx, 12, RULE_orderClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			match(T__4);
			setState(144);
			match(T__5);
			setState(146); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(145);
				orderCondition();
				}
				}
				setState(148); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 70368760955264L) != 0) );
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
		enterRule(_localctx, 14, RULE_orderCondition);
		int _la;
		try {
			setState(156);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__6:
			case T__7:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(150);
				_la = _input.LA(1);
				if ( !(_la==T__6 || _la==T__7) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(151);
				brackettedExpression();
				}
				}
				break;
			case T__23:
			case VARIABLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(154);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__23:
					{
					setState(152);
					constraint();
					}
					break;
				case VARIABLE:
					{
					setState(153);
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
		enterRule(_localctx, 16, RULE_limitOffsetClauses);
		int _la;
		try {
			setState(166);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__8:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(158);
				limitClause();
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__9) {
					{
					setState(159);
					offsetClause();
					}
				}

				}
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(162);
				offsetClause();
				setState(164);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__8) {
					{
					setState(163);
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
		enterRule(_localctx, 18, RULE_limitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			match(T__8);
			setState(169);
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
		enterRule(_localctx, 20, RULE_offsetClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			match(T__9);
			setState(172);
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
	public static class GraphPatternContext extends ParserRuleContext {
		public SubSelectContext subSelect() {
			return getRuleContext(SubSelectContext.class,0);
		}
		public GraphPatternInnerContext graphPatternInner() {
			return getRuleContext(GraphPatternInnerContext.class,0);
		}
		public GraphPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graphPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGraphPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGraphPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitGraphPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GraphPatternContext graphPattern() throws RecognitionException {
		GraphPatternContext _localctx = new GraphPatternContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_graphPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(174);
			match(T__1);
			setState(177);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				{
				setState(175);
				subSelect();
				}
				break;
			case T__1:
			case T__2:
			case T__12:
			case T__13:
			case T__14:
			case T__27:
			case T__29:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__42:
			case T__43:
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
				setState(176);
				graphPatternInner();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(179);
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
	public static class GraphPatternInnerContext extends ParserRuleContext {
		public List<TriplesBlockContext> triplesBlock() {
			return getRuleContexts(TriplesBlockContext.class);
		}
		public TriplesBlockContext triplesBlock(int i) {
			return getRuleContext(TriplesBlockContext.class,i);
		}
		public List<NonTriplesContext> nonTriples() {
			return getRuleContexts(NonTriplesContext.class);
		}
		public NonTriplesContext nonTriples(int i) {
			return getRuleContext(NonTriplesContext.class,i);
		}
		public GraphPatternInnerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_graphPatternInner; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGraphPatternInner(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGraphPatternInner(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitGraphPatternInner(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GraphPatternInnerContext graphPatternInner() throws RecognitionException {
		GraphPatternInnerContext _localctx = new GraphPatternInnerContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_graphPatternInner);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 504359211354816512L) != 0)) {
				{
				setState(181);
				triplesBlock();
				}
			}

			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 57348L) != 0)) {
				{
				{
				setState(184);
				nonTriples();
				setState(186);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 504359211354816512L) != 0)) {
					{
					setState(185);
					triplesBlock();
					}
				}

				}
				}
				setState(192);
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
	public static class NonTriplesContext extends ParserRuleContext {
		public UnionGraphPatternContext unionGraphPattern() {
			return getRuleContext(UnionGraphPatternContext.class,0);
		}
		public OptionalGraphPatternContext optionalGraphPattern() {
			return getRuleContext(OptionalGraphPatternContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public InlineValuesContext inlineValues() {
			return getRuleContext(InlineValuesContext.class,0);
		}
		public NonTriplesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonTriples; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNonTriples(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNonTriples(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNonTriples(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonTriplesContext nonTriples() throws RecognitionException {
		NonTriplesContext _localctx = new NonTriplesContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_nonTriples);
		try {
			setState(197);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(193);
				unionGraphPattern();
				}
				break;
			case T__12:
				enterOuterAlt(_localctx, 2);
				{
				setState(194);
				optionalGraphPattern();
				}
				break;
			case T__13:
				enterOuterAlt(_localctx, 3);
				{
				setState(195);
				filter();
				}
				break;
			case T__14:
				enterOuterAlt(_localctx, 4);
				{
				setState(196);
				inlineValues();
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
	public static class UnionGraphPatternContext extends ParserRuleContext {
		public List<GraphPatternContext> graphPattern() {
			return getRuleContexts(GraphPatternContext.class);
		}
		public GraphPatternContext graphPattern(int i) {
			return getRuleContext(GraphPatternContext.class,i);
		}
		public UnionGraphPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionGraphPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterUnionGraphPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitUnionGraphPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitUnionGraphPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionGraphPatternContext unionGraphPattern() throws RecognitionException {
		UnionGraphPatternContext _localctx = new UnionGraphPatternContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_unionGraphPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(199);
			graphPattern();
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__10 || _la==T__11) {
				{
				{
				setState(200);
				_la = _input.LA(1);
				if ( !(_la==T__10 || _la==T__11) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(201);
				graphPattern();
				}
				}
				setState(206);
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
	public static class OptionalGraphPatternContext extends ParserRuleContext {
		public GraphPatternContext graphPattern() {
			return getRuleContext(GraphPatternContext.class,0);
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
		enterRule(_localctx, 30, RULE_optionalGraphPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			match(T__12);
			setState(208);
			graphPattern();
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
		enterRule(_localctx, 32, RULE_filter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(210);
			match(T__13);
			setState(211);
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
		enterRule(_localctx, 34, RULE_constraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213);
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
	public static class InlineValuesContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
		}
		public InlineValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineValues; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterInlineValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitInlineValues(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitInlineValues(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InlineValuesContext inlineValues() throws RecognitionException {
		InlineValuesContext _localctx = new InlineValuesContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_inlineValues);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			match(T__14);
			setState(216);
			variable();
			setState(217);
			match(T__1);
			setState(221);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 504288809056206848L) != 0)) {
				{
				{
				setState(218);
				constant();
				}
				}
				setState(223);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(224);
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
		enterRule(_localctx, 38, RULE_selectTriples);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			triplesSameSubject();
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__15) {
				{
				setState(227);
				match(T__15);
				setState(229);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 504359211354816512L) != 0)) {
					{
					setState(228);
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
		enterRule(_localctx, 40, RULE_triplesBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			triplesSameSubject();
			setState(238);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__15) {
				{
				setState(234);
				match(T__15);
				setState(236);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 504359211354816512L) != 0)) {
					{
					setState(235);
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
		enterRule(_localctx, 42, RULE_triplesSameSubject);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(240);
			term();
			setState(241);
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
		enterRule(_localctx, 44, RULE_propertyListNotEmpty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(243);
			verb();
			setState(244);
			objectList();
			setState(253);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__16) {
				{
				{
				setState(245);
				match(T__16);
				setState(249);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35184455974912L) != 0)) {
					{
					setState(246);
					verb();
					setState(247);
					objectList();
					}
				}

				}
				}
				setState(255);
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
		enterRule(_localctx, 46, RULE_objectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(256);
			object();
			setState(261);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__17) {
				{
				{
				setState(257);
				match(T__17);
				setState(258);
				object();
				}
				}
				setState(263);
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
		enterRule(_localctx, 48, RULE_object);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264);
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
		enterRule(_localctx, 50, RULE_verb);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
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
		enterRule(_localctx, 52, RULE_schemaMorphismOrPath);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
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
		enterRule(_localctx, 54, RULE_pathAlternative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
			pathSequence();
			setState(275);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__18) {
				{
				{
				setState(271);
				match(T__18);
				setState(272);
				pathSequence();
				}
				}
				setState(277);
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
		enterRule(_localctx, 56, RULE_pathSequence);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
			pathWithMod();
			setState(283);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__19) {
				{
				{
				setState(279);
				match(T__19);
				setState(280);
				pathWithMod();
				}
				}
				setState(285);
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
		enterRule(_localctx, 58, RULE_pathWithMod);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			pathPrimary();
			setState(288);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 14680064L) != 0)) {
				{
				setState(287);
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
		enterRule(_localctx, 60, RULE_pathMod);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(290);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 14680064L) != 0)) ) {
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
		enterRule(_localctx, 62, RULE_pathPrimary);
		try {
			setState(297);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__25:
			case SCHEMA_MORPHISM:
				enterOuterAlt(_localctx, 1);
				{
				setState(292);
				schemaMorphism();
				}
				break;
			case T__23:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(293);
				match(T__23);
				setState(294);
				schemaMorphismOrPath();
				setState(295);
				match(T__24);
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
		enterRule(_localctx, 64, RULE_schemaMorphism);
		try {
			setState(301);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SCHEMA_MORPHISM:
				enterOuterAlt(_localctx, 1);
				{
				setState(299);
				primaryMorphism();
				}
				break;
			case T__25:
				enterOuterAlt(_localctx, 2);
				{
				setState(300);
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
		enterRule(_localctx, 66, RULE_primaryMorphism);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(303);
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
		enterRule(_localctx, 68, RULE_dualMorphism);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(305);
			match(T__25);
			setState(306);
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
		enterRule(_localctx, 70, RULE_graphNode);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			term();
			setState(311);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__26) {
				{
				setState(309);
				match(T__26);
				setState(310);
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
		enterRule(_localctx, 72, RULE_term);
		try {
			setState(316);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VARIABLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(313);
				variable();
				}
				break;
			case T__42:
			case T__43:
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
				setState(314);
				constant();
				}
				break;
			case T__27:
			case T__29:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
				enterOuterAlt(_localctx, 3);
				{
				setState(315);
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
		enterRule(_localctx, 74, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
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
		enterRule(_localctx, 76, RULE_constant);
		try {
			setState(323);
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
				setState(320);
				numericLiteral();
				}
				break;
			case T__42:
			case T__43:
				enterOuterAlt(_localctx, 2);
				{
				setState(321);
				booleanLiteral();
				}
				break;
			case STRING_LITERAL_SINGLE:
			case STRING_LITERAL_DOUBLE:
				enterOuterAlt(_localctx, 3);
				{
				setState(322);
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
		enterRule(_localctx, 78, RULE_computation);
		try {
			setState(331);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__29:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
				enterOuterAlt(_localctx, 1);
				{
				setState(325);
				aggregation();
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 2);
				{
				setState(326);
				match(T__27);
				setState(327);
				match(T__23);
				setState(328);
				termList();
				setState(329);
				match(T__24);
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
		enterRule(_localctx, 80, RULE_termList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(333);
			term();
			setState(338);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__17) {
				{
				{
				setState(334);
				match(T__17);
				setState(335);
				term();
				}
				}
				setState(340);
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
		enterRule(_localctx, 82, RULE_aggregation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(341);
			aggregationFunction();
			setState(342);
			match(T__23);
			setState(344);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__28) {
				{
				setState(343);
				distinctModifier();
				}
			}

			setState(346);
			expression();
			setState(348);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__17) {
				{
				setState(347);
				referenceArgument();
				}
			}

			setState(350);
			match(T__24);
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
		enterRule(_localctx, 84, RULE_distinctModifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(352);
			match(T__28);
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
		enterRule(_localctx, 86, RULE_referenceArgument);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			match(T__17);
			setState(355);
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
		enterRule(_localctx, 88, RULE_aggregationFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(357);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 33285996544L) != 0)) ) {
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
		enterRule(_localctx, 90, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
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
		enterRule(_localctx, 92, RULE_conditionalOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			conditionalAndExpression();
			setState(366);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__34) {
				{
				{
				setState(362);
				match(T__34);
				setState(363);
				conditionalAndExpression();
				}
				}
				setState(368);
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
		enterRule(_localctx, 94, RULE_conditionalAndExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(369);
			valueLogical();
			setState(374);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__35) {
				{
				{
				setState(370);
				match(T__35);
				setState(371);
				valueLogical();
				}
				}
				setState(376);
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
		enterRule(_localctx, 96, RULE_valueLogical);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(377);
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
		enterRule(_localctx, 98, RULE_relationalExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			expressionPart();
			setState(392);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__36:
				{
				setState(380);
				match(T__36);
				setState(381);
				expressionPart();
				}
				break;
			case T__37:
				{
				setState(382);
				match(T__37);
				setState(383);
				expressionPart();
				}
				break;
			case T__38:
				{
				setState(384);
				match(T__38);
				setState(385);
				expressionPart();
				}
				break;
			case T__39:
				{
				setState(386);
				match(T__39);
				setState(387);
				expressionPart();
				}
				break;
			case T__40:
				{
				setState(388);
				match(T__40);
				setState(389);
				expressionPart();
				}
				break;
			case T__41:
				{
				setState(390);
				match(T__41);
				setState(391);
				expressionPart();
				}
				break;
			case T__17:
			case T__24:
			case T__34:
			case T__35:
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
		enterRule(_localctx, 100, RULE_expressionPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(394);
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
		enterRule(_localctx, 102, RULE_primaryExpression);
		try {
			setState(398);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__23:
				enterOuterAlt(_localctx, 1);
				{
				setState(396);
				brackettedExpression();
				}
				break;
			case T__27:
			case T__29:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__42:
			case T__43:
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
				setState(397);
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
		enterRule(_localctx, 104, RULE_brackettedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(400);
			match(T__23);
			setState(401);
			expression();
			setState(402);
			match(T__24);
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
		enterRule(_localctx, 106, RULE_numericLiteral);
		try {
			setState(407);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
			case DECIMAL:
			case DOUBLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(404);
				numericLiteralUnsigned();
				}
				break;
			case INTEGER_POSITIVE:
			case DECIMAL_POSITIVE:
			case DOUBLE_POSITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(405);
				numericLiteralPositive();
				}
				break;
			case INTEGER_NEGATIVE:
			case DECIMAL_NEGATIVE:
			case DOUBLE_NEGATIVE:
				enterOuterAlt(_localctx, 3);
				{
				setState(406);
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
		enterRule(_localctx, 108, RULE_numericLiteralUnsigned);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(409);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 985162418487296L) != 0)) ) {
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
		enterRule(_localctx, 110, RULE_numericLiteralPositive);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(411);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7881299347898368L) != 0)) ) {
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
		enterRule(_localctx, 112, RULE_numericLiteralNegative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(413);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 63050394783186944L) != 0)) ) {
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
		enterRule(_localctx, 114, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(415);
			_la = _input.LA(1);
			if ( !(_la==T__42 || _la==T__43) ) {
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
		enterRule(_localctx, 116, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(417);
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
		"\u0004\u0001?\u01a4\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"7\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u0083\b\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0003\u0005\u008b\b\u0005\u0001\u0005\u0003\u0005\u008e\b\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0004\u0006\u0093\b\u0006\u000b\u0006\f"+
		"\u0006\u0094\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u009b\b\u0007\u0003\u0007\u009d\b\u0007\u0001\b\u0001\b\u0003\b\u00a1"+
		"\b\b\u0001\b\u0001\b\u0003\b\u00a5\b\b\u0003\b\u00a7\b\b\u0001\t\u0001"+
		"\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0003\u000b\u00b2\b\u000b\u0001\u000b\u0001\u000b\u0001\f\u0003\f\u00b7"+
		"\b\f\u0001\f\u0001\f\u0003\f\u00bb\b\f\u0005\f\u00bd\b\f\n\f\f\f\u00c0"+
		"\t\f\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00c6\b\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0005\u000e\u00cb\b\u000e\n\u000e\f\u000e\u00ce\t\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0005\u0012\u00dc\b\u0012\n\u0012\f\u0012\u00df\t\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u00e6\b\u0013\u0003"+
		"\u0013\u00e8\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u00ed"+
		"\b\u0014\u0003\u0014\u00ef\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u00fa\b\u0016\u0005\u0016\u00fc\b\u0016\n\u0016\f\u0016\u00ff"+
		"\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u0104\b\u0017"+
		"\n\u0017\f\u0017\u0107\t\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001"+
		"\u0019\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0005"+
		"\u001b\u0112\b\u001b\n\u001b\f\u001b\u0115\t\u001b\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0005\u001c\u011a\b\u001c\n\u001c\f\u001c\u011d\t\u001c\u0001"+
		"\u001d\u0001\u001d\u0003\u001d\u0121\b\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u012a"+
		"\b\u001f\u0001 \u0001 \u0003 \u012e\b \u0001!\u0001!\u0001\"\u0001\"\u0001"+
		"\"\u0001#\u0001#\u0001#\u0003#\u0138\b#\u0001$\u0001$\u0001$\u0003$\u013d"+
		"\b$\u0001%\u0001%\u0001&\u0001&\u0001&\u0003&\u0144\b&\u0001\'\u0001\'"+
		"\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u014c\b\'\u0001(\u0001(\u0001"+
		"(\u0005(\u0151\b(\n(\f(\u0154\t(\u0001)\u0001)\u0001)\u0003)\u0159\b)"+
		"\u0001)\u0001)\u0003)\u015d\b)\u0001)\u0001)\u0001*\u0001*\u0001+\u0001"+
		"+\u0001+\u0001,\u0001,\u0001-\u0001-\u0001.\u0001.\u0001.\u0005.\u016d"+
		"\b.\n.\f.\u0170\t.\u0001/\u0001/\u0001/\u0005/\u0175\b/\n/\f/\u0178\t"+
		"/\u00010\u00010\u00011\u00011\u00011\u00011\u00011\u00011\u00011\u0001"+
		"1\u00011\u00011\u00011\u00011\u00011\u00031\u0189\b1\u00012\u00012\u0001"+
		"3\u00013\u00033\u018f\b3\u00014\u00014\u00014\u00014\u00015\u00015\u0001"+
		"5\u00035\u0198\b5\u00016\u00016\u00017\u00017\u00018\u00018\u00019\u0001"+
		"9\u0001:\u0001:\u0001:\u0000\u0000;\u0000\u0002\u0004\u0006\b\n\f\u000e"+
		"\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDF"+
		"HJLNPRTVXZ\\^`bdfhjlnprt\u0000\t\u0001\u0000\u0007\b\u0001\u0000\u000b"+
		"\f\u0001\u0000\u0015\u0017\u0001\u0000\u001e\"\u0001\u0000/1\u0001\u0000"+
		"24\u0001\u000057\u0001\u0000+,\u0001\u00009:\u019a\u0000v\u0001\u0000"+
		"\u0000\u0000\u0002y\u0001\u0000\u0000\u0000\u0004}\u0001\u0000\u0000\u0000"+
		"\u0006\u007f\u0001\u0000\u0000\u0000\b\u0086\u0001\u0000\u0000\u0000\n"+
		"\u008a\u0001\u0000\u0000\u0000\f\u008f\u0001\u0000\u0000\u0000\u000e\u009c"+
		"\u0001\u0000\u0000\u0000\u0010\u00a6\u0001\u0000\u0000\u0000\u0012\u00a8"+
		"\u0001\u0000\u0000\u0000\u0014\u00ab\u0001\u0000\u0000\u0000\u0016\u00ae"+
		"\u0001\u0000\u0000\u0000\u0018\u00b6\u0001\u0000\u0000\u0000\u001a\u00c5"+
		"\u0001\u0000\u0000\u0000\u001c\u00c7\u0001\u0000\u0000\u0000\u001e\u00cf"+
		"\u0001\u0000\u0000\u0000 \u00d2\u0001\u0000\u0000\u0000\"\u00d5\u0001"+
		"\u0000\u0000\u0000$\u00d7\u0001\u0000\u0000\u0000&\u00e2\u0001\u0000\u0000"+
		"\u0000(\u00e9\u0001\u0000\u0000\u0000*\u00f0\u0001\u0000\u0000\u0000,"+
		"\u00f3\u0001\u0000\u0000\u0000.\u0100\u0001\u0000\u0000\u00000\u0108\u0001"+
		"\u0000\u0000\u00002\u010a\u0001\u0000\u0000\u00004\u010c\u0001\u0000\u0000"+
		"\u00006\u010e\u0001\u0000\u0000\u00008\u0116\u0001\u0000\u0000\u0000:"+
		"\u011e\u0001\u0000\u0000\u0000<\u0122\u0001\u0000\u0000\u0000>\u0129\u0001"+
		"\u0000\u0000\u0000@\u012d\u0001\u0000\u0000\u0000B\u012f\u0001\u0000\u0000"+
		"\u0000D\u0131\u0001\u0000\u0000\u0000F\u0134\u0001\u0000\u0000\u0000H"+
		"\u013c\u0001\u0000\u0000\u0000J\u013e\u0001\u0000\u0000\u0000L\u0143\u0001"+
		"\u0000\u0000\u0000N\u014b\u0001\u0000\u0000\u0000P\u014d\u0001\u0000\u0000"+
		"\u0000R\u0155\u0001\u0000\u0000\u0000T\u0160\u0001\u0000\u0000\u0000V"+
		"\u0162\u0001\u0000\u0000\u0000X\u0165\u0001\u0000\u0000\u0000Z\u0167\u0001"+
		"\u0000\u0000\u0000\\\u0169\u0001\u0000\u0000\u0000^\u0171\u0001\u0000"+
		"\u0000\u0000`\u0179\u0001\u0000\u0000\u0000b\u017b\u0001\u0000\u0000\u0000"+
		"d\u018a\u0001\u0000\u0000\u0000f\u018e\u0001\u0000\u0000\u0000h\u0190"+
		"\u0001\u0000\u0000\u0000j\u0197\u0001\u0000\u0000\u0000l\u0199\u0001\u0000"+
		"\u0000\u0000n\u019b\u0001\u0000\u0000\u0000p\u019d\u0001\u0000\u0000\u0000"+
		"r\u019f\u0001\u0000\u0000\u0000t\u01a1\u0001\u0000\u0000\u0000vw\u0003"+
		"\u0002\u0001\u0000wx\u0005\u0000\u0000\u0001x\u0001\u0001\u0000\u0000"+
		"\u0000yz\u0003\u0006\u0003\u0000z{\u0003\b\u0004\u0000{|\u0003\n\u0005"+
		"\u0000|\u0003\u0001\u0000\u0000\u0000}~\u0003\u0002\u0001\u0000~\u0005"+
		"\u0001\u0000\u0000\u0000\u007f\u0080\u0005\u0001\u0000\u0000\u0080\u0082"+
		"\u0005\u0002\u0000\u0000\u0081\u0083\u0003&\u0013\u0000\u0082\u0081\u0001"+
		"\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u0084\u0001"+
		"\u0000\u0000\u0000\u0084\u0085\u0005\u0003\u0000\u0000\u0085\u0007\u0001"+
		"\u0000\u0000\u0000\u0086\u0087\u0005\u0004\u0000\u0000\u0087\u0088\u0003"+
		"\u0016\u000b\u0000\u0088\t\u0001\u0000\u0000\u0000\u0089\u008b\u0003\f"+
		"\u0006\u0000\u008a\u0089\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000"+
		"\u0000\u0000\u008b\u008d\u0001\u0000\u0000\u0000\u008c\u008e\u0003\u0010"+
		"\b\u0000\u008d\u008c\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000\u0000"+
		"\u0000\u008e\u000b\u0001\u0000\u0000\u0000\u008f\u0090\u0005\u0005\u0000"+
		"\u0000\u0090\u0092\u0005\u0006\u0000\u0000\u0091\u0093\u0003\u000e\u0007"+
		"\u0000\u0092\u0091\u0001\u0000\u0000\u0000\u0093\u0094\u0001\u0000\u0000"+
		"\u0000\u0094\u0092\u0001\u0000\u0000\u0000\u0094\u0095\u0001\u0000\u0000"+
		"\u0000\u0095\r\u0001\u0000\u0000\u0000\u0096\u0097\u0007\u0000\u0000\u0000"+
		"\u0097\u009d\u0003h4\u0000\u0098\u009b\u0003\"\u0011\u0000\u0099\u009b"+
		"\u0003J%\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u009a\u0099\u0001\u0000"+
		"\u0000\u0000\u009b\u009d\u0001\u0000\u0000\u0000\u009c\u0096\u0001\u0000"+
		"\u0000\u0000\u009c\u009a\u0001\u0000\u0000\u0000\u009d\u000f\u0001\u0000"+
		"\u0000\u0000\u009e\u00a0\u0003\u0012\t\u0000\u009f\u00a1\u0003\u0014\n"+
		"\u0000\u00a0\u009f\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001\u0000\u0000"+
		"\u0000\u00a1\u00a7\u0001\u0000\u0000\u0000\u00a2\u00a4\u0003\u0014\n\u0000"+
		"\u00a3\u00a5\u0003\u0012\t\u0000\u00a4\u00a3\u0001\u0000\u0000\u0000\u00a4"+
		"\u00a5\u0001\u0000\u0000\u0000\u00a5\u00a7\u0001\u0000\u0000\u0000\u00a6"+
		"\u009e\u0001\u0000\u0000\u0000\u00a6\u00a2\u0001\u0000\u0000\u0000\u00a7"+
		"\u0011\u0001\u0000\u0000\u0000\u00a8\u00a9\u0005\t\u0000\u0000\u00a9\u00aa"+
		"\u0005/\u0000\u0000\u00aa\u0013\u0001\u0000\u0000\u0000\u00ab\u00ac\u0005"+
		"\n\u0000\u0000\u00ac\u00ad\u0005/\u0000\u0000\u00ad\u0015\u0001\u0000"+
		"\u0000\u0000\u00ae\u00b1\u0005\u0002\u0000\u0000\u00af\u00b2\u0003\u0004"+
		"\u0002\u0000\u00b0\u00b2\u0003\u0018\f\u0000\u00b1\u00af\u0001\u0000\u0000"+
		"\u0000\u00b1\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000"+
		"\u0000\u00b3\u00b4\u0005\u0003\u0000\u0000\u00b4\u0017\u0001\u0000\u0000"+
		"\u0000\u00b5\u00b7\u0003(\u0014\u0000\u00b6\u00b5\u0001\u0000\u0000\u0000"+
		"\u00b6\u00b7\u0001\u0000\u0000\u0000\u00b7\u00be\u0001\u0000\u0000\u0000"+
		"\u00b8\u00ba\u0003\u001a\r\u0000\u00b9\u00bb\u0003(\u0014\u0000\u00ba"+
		"\u00b9\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb"+
		"\u00bd\u0001\u0000\u0000\u0000\u00bc\u00b8\u0001\u0000\u0000\u0000\u00bd"+
		"\u00c0\u0001\u0000\u0000\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00be"+
		"\u00bf\u0001\u0000\u0000\u0000\u00bf\u0019\u0001\u0000\u0000\u0000\u00c0"+
		"\u00be\u0001\u0000\u0000\u0000\u00c1\u00c6\u0003\u001c\u000e\u0000\u00c2"+
		"\u00c6\u0003\u001e\u000f\u0000\u00c3\u00c6\u0003 \u0010\u0000\u00c4\u00c6"+
		"\u0003$\u0012\u0000\u00c5\u00c1\u0001\u0000\u0000\u0000\u00c5\u00c2\u0001"+
		"\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c4\u0001"+
		"\u0000\u0000\u0000\u00c6\u001b\u0001\u0000\u0000\u0000\u00c7\u00cc\u0003"+
		"\u0016\u000b\u0000\u00c8\u00c9\u0007\u0001\u0000\u0000\u00c9\u00cb\u0003"+
		"\u0016\u000b\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00cb\u00ce\u0001"+
		"\u0000\u0000\u0000\u00cc\u00ca\u0001\u0000\u0000\u0000\u00cc\u00cd\u0001"+
		"\u0000\u0000\u0000\u00cd\u001d\u0001\u0000\u0000\u0000\u00ce\u00cc\u0001"+
		"\u0000\u0000\u0000\u00cf\u00d0\u0005\r\u0000\u0000\u00d0\u00d1\u0003\u0016"+
		"\u000b\u0000\u00d1\u001f\u0001\u0000\u0000\u0000\u00d2\u00d3\u0005\u000e"+
		"\u0000\u0000\u00d3\u00d4\u0003\"\u0011\u0000\u00d4!\u0001\u0000\u0000"+
		"\u0000\u00d5\u00d6\u0003h4\u0000\u00d6#\u0001\u0000\u0000\u0000\u00d7"+
		"\u00d8\u0005\u000f\u0000\u0000\u00d8\u00d9\u0003J%\u0000\u00d9\u00dd\u0005"+
		"\u0002\u0000\u0000\u00da\u00dc\u0003L&\u0000\u00db\u00da\u0001\u0000\u0000"+
		"\u0000\u00dc\u00df\u0001\u0000\u0000\u0000\u00dd\u00db\u0001\u0000\u0000"+
		"\u0000\u00dd\u00de\u0001\u0000\u0000\u0000\u00de\u00e0\u0001\u0000\u0000"+
		"\u0000\u00df\u00dd\u0001\u0000\u0000\u0000\u00e0\u00e1\u0005\u0003\u0000"+
		"\u0000\u00e1%\u0001\u0000\u0000\u0000\u00e2\u00e7\u0003*\u0015\u0000\u00e3"+
		"\u00e5\u0005\u0010\u0000\u0000\u00e4\u00e6\u0003&\u0013\u0000\u00e5\u00e4"+
		"\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001\u0000\u0000\u0000\u00e6\u00e8"+
		"\u0001\u0000\u0000\u0000\u00e7\u00e3\u0001\u0000\u0000\u0000\u00e7\u00e8"+
		"\u0001\u0000\u0000\u0000\u00e8\'\u0001\u0000\u0000\u0000\u00e9\u00ee\u0003"+
		"*\u0015\u0000\u00ea\u00ec\u0005\u0010\u0000\u0000\u00eb\u00ed\u0003(\u0014"+
		"\u0000\u00ec\u00eb\u0001\u0000\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000"+
		"\u0000\u00ed\u00ef\u0001\u0000\u0000\u0000\u00ee\u00ea\u0001\u0000\u0000"+
		"\u0000\u00ee\u00ef\u0001\u0000\u0000\u0000\u00ef)\u0001\u0000\u0000\u0000"+
		"\u00f0\u00f1\u0003H$\u0000\u00f1\u00f2\u0003,\u0016\u0000\u00f2+\u0001"+
		"\u0000\u0000\u0000\u00f3\u00f4\u00032\u0019\u0000\u00f4\u00fd\u0003.\u0017"+
		"\u0000\u00f5\u00f9\u0005\u0011\u0000\u0000\u00f6\u00f7\u00032\u0019\u0000"+
		"\u00f7\u00f8\u0003.\u0017\u0000\u00f8\u00fa\u0001\u0000\u0000\u0000\u00f9"+
		"\u00f6\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa"+
		"\u00fc\u0001\u0000\u0000\u0000\u00fb\u00f5\u0001\u0000\u0000\u0000\u00fc"+
		"\u00ff\u0001\u0000\u0000\u0000\u00fd\u00fb\u0001\u0000\u0000\u0000\u00fd"+
		"\u00fe\u0001\u0000\u0000\u0000\u00fe-\u0001\u0000\u0000\u0000\u00ff\u00fd"+
		"\u0001\u0000\u0000\u0000\u0100\u0105\u00030\u0018\u0000\u0101\u0102\u0005"+
		"\u0012\u0000\u0000\u0102\u0104\u00030\u0018\u0000\u0103\u0101\u0001\u0000"+
		"\u0000\u0000\u0104\u0107\u0001\u0000\u0000\u0000\u0105\u0103\u0001\u0000"+
		"\u0000\u0000\u0105\u0106\u0001\u0000\u0000\u0000\u0106/\u0001\u0000\u0000"+
		"\u0000\u0107\u0105\u0001\u0000\u0000\u0000\u0108\u0109\u0003F#\u0000\u0109"+
		"1\u0001\u0000\u0000\u0000\u010a\u010b\u00034\u001a\u0000\u010b3\u0001"+
		"\u0000\u0000\u0000\u010c\u010d\u00036\u001b\u0000\u010d5\u0001\u0000\u0000"+
		"\u0000\u010e\u0113\u00038\u001c\u0000\u010f\u0110\u0005\u0013\u0000\u0000"+
		"\u0110\u0112\u00038\u001c\u0000\u0111\u010f\u0001\u0000\u0000\u0000\u0112"+
		"\u0115\u0001\u0000\u0000\u0000\u0113\u0111\u0001\u0000\u0000\u0000\u0113"+
		"\u0114\u0001\u0000\u0000\u0000\u01147\u0001\u0000\u0000\u0000\u0115\u0113"+
		"\u0001\u0000\u0000\u0000\u0116\u011b\u0003:\u001d\u0000\u0117\u0118\u0005"+
		"\u0014\u0000\u0000\u0118\u011a\u0003:\u001d\u0000\u0119\u0117\u0001\u0000"+
		"\u0000\u0000\u011a\u011d\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000"+
		"\u0000\u0000\u011b\u011c\u0001\u0000\u0000\u0000\u011c9\u0001\u0000\u0000"+
		"\u0000\u011d\u011b\u0001\u0000\u0000\u0000\u011e\u0120\u0003>\u001f\u0000"+
		"\u011f\u0121\u0003<\u001e\u0000\u0120\u011f\u0001\u0000\u0000\u0000\u0120"+
		"\u0121\u0001\u0000\u0000\u0000\u0121;\u0001\u0000\u0000\u0000\u0122\u0123"+
		"\u0007\u0002\u0000\u0000\u0123=\u0001\u0000\u0000\u0000\u0124\u012a\u0003"+
		"@ \u0000\u0125\u0126\u0005\u0018\u0000\u0000\u0126\u0127\u00034\u001a"+
		"\u0000\u0127\u0128\u0005\u0019\u0000\u0000\u0128\u012a\u0001\u0000\u0000"+
		"\u0000\u0129\u0124\u0001\u0000\u0000\u0000\u0129\u0125\u0001\u0000\u0000"+
		"\u0000\u012a?\u0001\u0000\u0000\u0000\u012b\u012e\u0003B!\u0000\u012c"+
		"\u012e\u0003D\"\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012d\u012c"+
		"\u0001\u0000\u0000\u0000\u012eA\u0001\u0000\u0000\u0000\u012f\u0130\u0005"+
		"-\u0000\u0000\u0130C\u0001\u0000\u0000\u0000\u0131\u0132\u0005\u001a\u0000"+
		"\u0000\u0132\u0133\u0003B!\u0000\u0133E\u0001\u0000\u0000\u0000\u0134"+
		"\u0137\u0003H$\u0000\u0135\u0136\u0005\u001b\u0000\u0000\u0136\u0138\u0003"+
		"J%\u0000\u0137\u0135\u0001\u0000\u0000\u0000\u0137\u0138\u0001\u0000\u0000"+
		"\u0000\u0138G\u0001\u0000\u0000\u0000\u0139\u013d\u0003J%\u0000\u013a"+
		"\u013d\u0003L&\u0000\u013b\u013d\u0003N\'\u0000\u013c\u0139\u0001\u0000"+
		"\u0000\u0000\u013c\u013a\u0001\u0000\u0000\u0000\u013c\u013b\u0001\u0000"+
		"\u0000\u0000\u013dI\u0001\u0000\u0000\u0000\u013e\u013f\u0005.\u0000\u0000"+
		"\u013fK\u0001\u0000\u0000\u0000\u0140\u0144\u0003j5\u0000\u0141\u0144"+
		"\u0003r9\u0000\u0142\u0144\u0003t:\u0000\u0143\u0140\u0001\u0000\u0000"+
		"\u0000\u0143\u0141\u0001\u0000\u0000\u0000\u0143\u0142\u0001\u0000\u0000"+
		"\u0000\u0144M\u0001\u0000\u0000\u0000\u0145\u014c\u0003R)\u0000\u0146"+
		"\u0147\u0005\u001c\u0000\u0000\u0147\u0148\u0005\u0018\u0000\u0000\u0148"+
		"\u0149\u0003P(\u0000\u0149\u014a\u0005\u0019\u0000\u0000\u014a\u014c\u0001"+
		"\u0000\u0000\u0000\u014b\u0145\u0001\u0000\u0000\u0000\u014b\u0146\u0001"+
		"\u0000\u0000\u0000\u014cO\u0001\u0000\u0000\u0000\u014d\u0152\u0003H$"+
		"\u0000\u014e\u014f\u0005\u0012\u0000\u0000\u014f\u0151\u0003H$\u0000\u0150"+
		"\u014e\u0001\u0000\u0000\u0000\u0151\u0154\u0001\u0000\u0000\u0000\u0152"+
		"\u0150\u0001\u0000\u0000\u0000\u0152\u0153\u0001\u0000\u0000\u0000\u0153"+
		"Q\u0001\u0000\u0000\u0000\u0154\u0152\u0001\u0000\u0000\u0000\u0155\u0156"+
		"\u0003X,\u0000\u0156\u0158\u0005\u0018\u0000\u0000\u0157\u0159\u0003T"+
		"*\u0000\u0158\u0157\u0001\u0000\u0000\u0000\u0158\u0159\u0001\u0000\u0000"+
		"\u0000\u0159\u015a\u0001\u0000\u0000\u0000\u015a\u015c\u0003Z-\u0000\u015b"+
		"\u015d\u0003V+\u0000\u015c\u015b\u0001\u0000\u0000\u0000\u015c\u015d\u0001"+
		"\u0000\u0000\u0000\u015d\u015e\u0001\u0000\u0000\u0000\u015e\u015f\u0005"+
		"\u0019\u0000\u0000\u015fS\u0001\u0000\u0000\u0000\u0160\u0161\u0005\u001d"+
		"\u0000\u0000\u0161U\u0001\u0000\u0000\u0000\u0162\u0163\u0005\u0012\u0000"+
		"\u0000\u0163\u0164\u0003J%\u0000\u0164W\u0001\u0000\u0000\u0000\u0165"+
		"\u0166\u0007\u0003\u0000\u0000\u0166Y\u0001\u0000\u0000\u0000\u0167\u0168"+
		"\u0003\\.\u0000\u0168[\u0001\u0000\u0000\u0000\u0169\u016e\u0003^/\u0000"+
		"\u016a\u016b\u0005#\u0000\u0000\u016b\u016d\u0003^/\u0000\u016c\u016a"+
		"\u0001\u0000\u0000\u0000\u016d\u0170\u0001\u0000\u0000\u0000\u016e\u016c"+
		"\u0001\u0000\u0000\u0000\u016e\u016f\u0001\u0000\u0000\u0000\u016f]\u0001"+
		"\u0000\u0000\u0000\u0170\u016e\u0001\u0000\u0000\u0000\u0171\u0176\u0003"+
		"`0\u0000\u0172\u0173\u0005$\u0000\u0000\u0173\u0175\u0003`0\u0000\u0174"+
		"\u0172\u0001\u0000\u0000\u0000\u0175\u0178\u0001\u0000\u0000\u0000\u0176"+
		"\u0174\u0001\u0000\u0000\u0000\u0176\u0177\u0001\u0000\u0000\u0000\u0177"+
		"_\u0001\u0000\u0000\u0000\u0178\u0176\u0001\u0000\u0000\u0000\u0179\u017a"+
		"\u0003b1\u0000\u017aa\u0001\u0000\u0000\u0000\u017b\u0188\u0003d2\u0000"+
		"\u017c\u017d\u0005%\u0000\u0000\u017d\u0189\u0003d2\u0000\u017e\u017f"+
		"\u0005&\u0000\u0000\u017f\u0189\u0003d2\u0000\u0180\u0181\u0005\'\u0000"+
		"\u0000\u0181\u0189\u0003d2\u0000\u0182\u0183\u0005(\u0000\u0000\u0183"+
		"\u0189\u0003d2\u0000\u0184\u0185\u0005)\u0000\u0000\u0185\u0189\u0003"+
		"d2\u0000\u0186\u0187\u0005*\u0000\u0000\u0187\u0189\u0003d2\u0000\u0188"+
		"\u017c\u0001\u0000\u0000\u0000\u0188\u017e\u0001\u0000\u0000\u0000\u0188"+
		"\u0180\u0001\u0000\u0000\u0000\u0188\u0182\u0001\u0000\u0000\u0000\u0188"+
		"\u0184\u0001\u0000\u0000\u0000\u0188\u0186\u0001\u0000\u0000\u0000\u0188"+
		"\u0189\u0001\u0000\u0000\u0000\u0189c\u0001\u0000\u0000\u0000\u018a\u018b"+
		"\u0003f3\u0000\u018be\u0001\u0000\u0000\u0000\u018c\u018f\u0003h4\u0000"+
		"\u018d\u018f\u0003H$\u0000\u018e\u018c\u0001\u0000\u0000\u0000\u018e\u018d"+
		"\u0001\u0000\u0000\u0000\u018fg\u0001\u0000\u0000\u0000\u0190\u0191\u0005"+
		"\u0018\u0000\u0000\u0191\u0192\u0003Z-\u0000\u0192\u0193\u0005\u0019\u0000"+
		"\u0000\u0193i\u0001\u0000\u0000\u0000\u0194\u0198\u0003l6\u0000\u0195"+
		"\u0198\u0003n7\u0000\u0196\u0198\u0003p8\u0000\u0197\u0194\u0001\u0000"+
		"\u0000\u0000\u0197\u0195\u0001\u0000\u0000\u0000\u0197\u0196\u0001\u0000"+
		"\u0000\u0000\u0198k\u0001\u0000\u0000\u0000\u0199\u019a\u0007\u0004\u0000"+
		"\u0000\u019am\u0001\u0000\u0000\u0000\u019b\u019c\u0007\u0005\u0000\u0000"+
		"\u019co\u0001\u0000\u0000\u0000\u019d\u019e\u0007\u0006\u0000\u0000\u019e"+
		"q\u0001\u0000\u0000\u0000\u019f\u01a0\u0007\u0007\u0000\u0000\u01a0s\u0001"+
		"\u0000\u0000\u0000\u01a1\u01a2\u0007\b\u0000\u0000\u01a2u\u0001\u0000"+
		"\u0000\u0000(\u0082\u008a\u008d\u0094\u009a\u009c\u00a0\u00a4\u00a6\u00b1"+
		"\u00b6\u00ba\u00be\u00c5\u00cc\u00dd\u00e5\u00e7\u00ec\u00ee\u00f9\u00fd"+
		"\u0105\u0113\u011b\u0120\u0129\u012d\u0137\u013c\u0143\u014b\u0152\u0158"+
		"\u015c\u016e\u0176\u0188\u018e\u0197";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}