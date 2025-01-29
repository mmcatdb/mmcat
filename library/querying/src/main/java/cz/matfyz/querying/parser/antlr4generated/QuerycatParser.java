package cz.matfyz.querying.parser.antlr4generated;
// Generated from grammars/Querycat.g4 by ANTLR 4.13.0
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class QuerycatParser extends Parser {
    static { RuntimeMetaData.checkVersion("4.13.0", RuntimeMetaData.VERSION); }

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
        SCHEMA_IDENTIFIER=46, BLANK_NODE_LABEL=47, VAR1=48, VAR2=49, INTEGER=50,
        DECIMAL=51, DOUBLE=52, INTEGER_POSITIVE=53, DECIMAL_POSITIVE=54, DOUBLE_POSITIVE=55,
        INTEGER_NEGATIVE=56, DECIMAL_NEGATIVE=57, DOUBLE_NEGATIVE=58, EXPONENT=59,
        STRING_LITERAL1=60, STRING_LITERAL2=61, STRING_LITERAL_LONG1=62, STRING_LITERAL_LONG2=63,
        ECHAR=64, NIL=65, ANON=66, PN_CHARS_U=67, VARNAME=68, PN_PREFIX=69, PN_LOCAL=70,
        WS=71;
    public static final int
        RULE_query = 0, RULE_selectQuery = 1, RULE_subSelect = 2, RULE_selectClause = 3,
        RULE_selectGraphPattern = 4, RULE_fromClause = 5, RULE_whereClause = 6,
        RULE_solutionModifier = 7, RULE_limitOffsetClauses = 8, RULE_orderClause = 9,
        RULE_orderCondition = 10, RULE_limitClause = 11, RULE_offsetClause = 12,
        RULE_groupGraphPattern = 13, RULE_triplesBlock = 14, RULE_graphPatternNotTriples = 15,
        RULE_optionalGraphPattern = 16, RULE_groupOrUnionGraphPattern = 17, RULE_inlineData = 18,
        RULE_dataBlock = 19, RULE_dataBlockValue = 20, RULE_filter_ = 21, RULE_constraint = 22,
        RULE_selectTriples = 23, RULE_triplesSameSubject = 24, RULE_propertyListNotEmpty = 25,
        RULE_propertyList = 26, RULE_objectList = 27, RULE_object_ = 28, RULE_verb = 29,
        RULE_schemaMorphismOrPath = 30, RULE_pathAlternative = 31, RULE_pathSequence = 32,
        RULE_pathWithMod = 33, RULE_pathMod = 34, RULE_pathPrimary = 35, RULE_schemaMorphism = 36,
        RULE_primaryMorphism = 37, RULE_dualMorphism = 38, RULE_graphNode = 39,
        RULE_varOrTerm = 40, RULE_var_ = 41, RULE_constantTerm = 42, RULE_aggregationTerm = 43,
        RULE_distinctModifier = 44, RULE_aggregationFunc = 45, RULE_expression = 46,
        RULE_conditionalOrExpression = 47, RULE_conditionalAndExpression = 48,
        RULE_valueLogical = 49, RULE_relationalExpression = 50, RULE_expressionPart = 51,
        RULE_primaryExpression = 52, RULE_brackettedExpression = 53, RULE_numericLiteral = 54,
        RULE_numericLiteralUnsigned = 55, RULE_numericLiteralPositive = 56, RULE_numericLiteralNegative = 57,
        RULE_booleanLiteral = 58, RULE_string_ = 59, RULE_blankNode = 60;
    private static String[] makeRuleNames() {
        return new String[] {
            "query", "selectQuery", "subSelect", "selectClause", "selectGraphPattern",
            "fromClause", "whereClause", "solutionModifier", "limitOffsetClauses",
            "orderClause", "orderCondition", "limitClause", "offsetClause", "groupGraphPattern",
            "triplesBlock", "graphPatternNotTriples", "optionalGraphPattern", "groupOrUnionGraphPattern",
            "inlineData", "dataBlock", "dataBlockValue", "filter_", "constraint",
            "selectTriples", "triplesSameSubject", "propertyListNotEmpty", "propertyList",
            "objectList", "object_", "verb", "schemaMorphismOrPath", "pathAlternative",
            "pathSequence", "pathWithMod", "pathMod", "pathPrimary", "schemaMorphism",
            "primaryMorphism", "dualMorphism", "graphNode", "varOrTerm", "var_",
            "constantTerm", "aggregationTerm", "distinctModifier", "aggregationFunc",
            "expression", "conditionalOrExpression", "conditionalAndExpression",
            "valueLogical", "relationalExpression", "expressionPart", "primaryExpression",
            "brackettedExpression", "numericLiteral", "numericLiteralUnsigned", "numericLiteralPositive",
            "numericLiteralNegative", "booleanLiteral", "string_", "blankNode"
        };
    }
    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] {
            null, "'SELECT'", "'{'", "'}'", "'FROM'", "'WHERE'", "'ORDER'", "'BY'",
            "'ASC'", "'DESC'", "'LIMIT'", "'OFFSET'", "'.'", "'OPTIONAL'", "'UNION'",
            "'MINUS'", "'VALUES'", "'FILTER'", "';'", "','", "'|'", "'/'", "'?'",
            "'*'", "'+'", "'('", "')'", "'-'", "'AS'", "'DISTINCT'", "'COUNT'", "'SUM'",
            "'AVG'", "'MIN'", "'MAX'", "'||'", "'&&'", "'='", "'!='", "'<'", "'>'",
            "'<='", "'>='", "'true'", "'false'"
        };
    }
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static String[] makeSymbolicNames() {
        return new String[] {
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, "SCHEMA_MORPHISM",
            "SCHEMA_IDENTIFIER", "BLANK_NODE_LABEL", "VAR1", "VAR2", "INTEGER", "DECIMAL",
            "DOUBLE", "INTEGER_POSITIVE", "DECIMAL_POSITIVE", "DOUBLE_POSITIVE",
            "INTEGER_NEGATIVE", "DECIMAL_NEGATIVE", "DOUBLE_NEGATIVE", "EXPONENT",
            "STRING_LITERAL1", "STRING_LITERAL2", "STRING_LITERAL_LONG1", "STRING_LITERAL_LONG2",
            "ECHAR", "NIL", "ANON", "PN_CHARS_U", "VARNAME", "PN_PREFIX", "PN_LOCAL",
            "WS"
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

    @Override @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override public String getGrammarFileName() { return "Querycat.g4"; }

    @Override public String[] getRuleNames() { return ruleNames; }

    @Override public String getSerializedATN() { return _serializedATN; }

    @Override public ATN getATN() { return _ATN; }

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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterQuery(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitQuery(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectQuery(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectQuery(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSubSelect(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSubSelect(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectClause(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectClause(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectGraphPattern(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectGraphPattern(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
            if (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 106837205023L) != 0)) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterFromClause(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitFromClause(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterWhereClause(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitWhereClause(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSolutionModifier(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSolutionModifier(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterLimitOffsetClauses(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitLimitOffsetClauses(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitLimitOffsetClauses(this);
            else return visitor.visitChildren(this);
        }
    }

    public final LimitOffsetClausesContext limitOffsetClauses() throws RecognitionException {
        LimitOffsetClausesContext _localctx = new LimitOffsetClausesContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_limitOffsetClauses);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(165);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
            case T__9:
                {
                setState(157);
                limitClause();
                setState(159);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la==T__10) {
                    {
                    setState(158);
                    offsetClause();
                    }
                }

                }
                break;
            case T__10:
                {
                setState(161);
                offsetClause();
                setState(163);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la==T__9) {
                    {
                    setState(162);
                    limitClause();
                    }
                }

                }
                break;
            default:
                throw new NoViableAltException(this);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOrderClause(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOrderClause(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitOrderClause(this);
            else return visitor.visitChildren(this);
        }
    }

    public final OrderClauseContext orderClause() throws RecognitionException {
        OrderClauseContext _localctx = new OrderClauseContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_orderClause);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(167);
            match(T__5);
            setState(168);
            match(T__6);
            setState(170);
            _errHandler.sync(this);
            _la = _input.LA(1);
            do {
                {
                {
                setState(169);
                orderCondition();
                }
                }
                setState(172);
                _errHandler.sync(this);
                _la = _input.LA(1);
            } while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 844424963687168L) != 0) );
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
        public Var_Context var_() {
            return getRuleContext(Var_Context.class,0);
        }
        public OrderConditionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_orderCondition; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOrderCondition(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOrderCondition(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitOrderCondition(this);
            else return visitor.visitChildren(this);
        }
    }

    public final OrderConditionContext orderCondition() throws RecognitionException {
        OrderConditionContext _localctx = new OrderConditionContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_orderCondition);
        int _la;
        try {
            setState(180);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
            case T__7:
            case T__8:
                enterOuterAlt(_localctx, 1);
                {
                {
                setState(174);
                _la = _input.LA(1);
                if ( !(_la==T__7 || _la==T__8) ) {
                _errHandler.recoverInline(this);
                }
                else {
                    if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(175);
                brackettedExpression();
                }
                }
                break;
            case T__24:
            case VAR1:
            case VAR2:
                enterOuterAlt(_localctx, 2);
                {
                setState(178);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                case T__24:
                    {
                    setState(176);
                    constraint();
                    }
                    break;
                case VAR1:
                case VAR2:
                    {
                    setState(177);
                    var_();
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
    public static class LimitClauseContext extends ParserRuleContext {
        public TerminalNode INTEGER() { return getToken(QuerycatParser.INTEGER, 0); }
        public LimitClauseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_limitClause; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterLimitClause(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitLimitClause(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOffsetClause(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOffsetClause(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        public List<Filter_Context> filter_() {
            return getRuleContexts(Filter_Context.class);
        }
        public Filter_Context filter_(int i) {
            return getRuleContext(Filter_Context.class,i);
        }
        public GroupGraphPatternContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_groupGraphPattern; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGroupGraphPattern(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGroupGraphPattern(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
            case T__29:
            case T__30:
            case T__31:
            case T__32:
            case T__33:
            case T__42:
            case T__43:
            case BLANK_NODE_LABEL:
            case VAR1:
            case VAR2:
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
            case INTEGER_POSITIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_NEGATIVE:
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
            case STRING_LITERAL1:
            case STRING_LITERAL2:
            case NIL:
            case ANON:
                {
                {
                setState(191);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 106837205023L) != 0)) {
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
                        filter_();
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
                    if (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 106837205023L) != 0)) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterTriplesBlock(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitTriplesBlock(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
                if (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 106837205023L) != 0)) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGraphPatternNotTriples(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGraphPatternNotTriples(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterOptionalGraphPattern(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitOptionalGraphPattern(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGroupOrUnionGraphPattern(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGroupOrUnionGraphPattern(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterInlineData(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitInlineData(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
        public Var_Context var_() {
            return getRuleContext(Var_Context.class,0);
        }
        public List<DataBlockValueContext> dataBlockValue() {
            return getRuleContexts(DataBlockValueContext.class);
        }
        public DataBlockValueContext dataBlockValue(int i) {
            return getRuleContext(DataBlockValueContext.class,i);
        }
        public DataBlockContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_dataBlock; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterDataBlock(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitDataBlock(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
            var_();
            setState(239);
            match(T__1);
            setState(243);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4034125754496188416L) != 0)) {
                {
                {
                setState(240);
                dataBlockValue();
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
    public static class DataBlockValueContext extends ParserRuleContext {
        public NumericLiteralContext numericLiteral() {
            return getRuleContext(NumericLiteralContext.class,0);
        }
        public BooleanLiteralContext booleanLiteral() {
            return getRuleContext(BooleanLiteralContext.class,0);
        }
        public String_Context string_() {
            return getRuleContext(String_Context.class,0);
        }
        public DataBlockValueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_dataBlockValue; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterDataBlockValue(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitDataBlockValue(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitDataBlockValue(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DataBlockValueContext dataBlockValue() throws RecognitionException {
        DataBlockValueContext _localctx = new DataBlockValueContext(_ctx, getState());
        enterRule(_localctx, 40, RULE_dataBlockValue);
        try {
            setState(251);
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
                setState(248);
                numericLiteral();
                }
                break;
            case T__42:
            case T__43:
                enterOuterAlt(_localctx, 2);
                {
                setState(249);
                booleanLiteral();
                }
                break;
            case STRING_LITERAL1:
            case STRING_LITERAL2:
                enterOuterAlt(_localctx, 3);
                {
                setState(250);
                string_();
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
    public static class Filter_Context extends ParserRuleContext {
        public ConstraintContext constraint() {
            return getRuleContext(ConstraintContext.class,0);
        }
        public Filter_Context(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_filter_; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterFilter_(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitFilter_(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitFilter_(this);
            else return visitor.visitChildren(this);
        }
    }

    public final Filter_Context filter_() throws RecognitionException {
        Filter_Context _localctx = new Filter_Context(_ctx, getState());
        enterRule(_localctx, 42, RULE_filter_);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(253);
            match(T__16);
            setState(254);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConstraint(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConstraint(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConstraint(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ConstraintContext constraint() throws RecognitionException {
        ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
        enterRule(_localctx, 44, RULE_constraint);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(256);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSelectTriples(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSelectTriples(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSelectTriples(this);
            else return visitor.visitChildren(this);
        }
    }

    public final SelectTriplesContext selectTriples() throws RecognitionException {
        SelectTriplesContext _localctx = new SelectTriplesContext(_ctx, getState());
        enterRule(_localctx, 46, RULE_selectTriples);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(258);
            triplesSameSubject();
            setState(263);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la==T__11) {
                {
                setState(259);
                match(T__11);
                setState(261);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 106837205023L) != 0)) {
                    {
                    setState(260);
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
        public VarOrTermContext varOrTerm() {
            return getRuleContext(VarOrTermContext.class,0);
        }
        public PropertyListNotEmptyContext propertyListNotEmpty() {
            return getRuleContext(PropertyListNotEmptyContext.class,0);
        }
        public TriplesSameSubjectContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_triplesSameSubject; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterTriplesSameSubject(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitTriplesSameSubject(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitTriplesSameSubject(this);
            else return visitor.visitChildren(this);
        }
    }

    public final TriplesSameSubjectContext triplesSameSubject() throws RecognitionException {
        TriplesSameSubjectContext _localctx = new TriplesSameSubjectContext(_ctx, getState());
        enterRule(_localctx, 48, RULE_triplesSameSubject);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(265);
            varOrTerm();
            setState(266);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPropertyListNotEmpty(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPropertyListNotEmpty(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPropertyListNotEmpty(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PropertyListNotEmptyContext propertyListNotEmpty() throws RecognitionException {
        PropertyListNotEmptyContext _localctx = new PropertyListNotEmptyContext(_ctx, getState());
        enterRule(_localctx, 50, RULE_propertyListNotEmpty);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(268);
            verb();
            setState(269);
            objectList();
            setState(278);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la==T__17) {
                {
                {
                setState(270);
                match(T__17);
                setState(274);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35184539860992L) != 0)) {
                    {
                    setState(271);
                    verb();
                    setState(272);
                    objectList();
                    }
                }

                }
                }
                setState(280);
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
    public static class PropertyListContext extends ParserRuleContext {
        public PropertyListNotEmptyContext propertyListNotEmpty() {
            return getRuleContext(PropertyListNotEmptyContext.class,0);
        }
        public PropertyListContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_propertyList; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPropertyList(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPropertyList(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPropertyList(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PropertyListContext propertyList() throws RecognitionException {
        PropertyListContext _localctx = new PropertyListContext(_ctx, getState());
        enterRule(_localctx, 52, RULE_propertyList);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(282);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35184539860992L) != 0)) {
                {
                setState(281);
                propertyListNotEmpty();
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
    public static class ObjectListContext extends ParserRuleContext {
        public List<Object_Context> object_() {
            return getRuleContexts(Object_Context.class);
        }
        public Object_Context object_(int i) {
            return getRuleContext(Object_Context.class,i);
        }
        public ObjectListContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_objectList; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterObjectList(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitObjectList(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitObjectList(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ObjectListContext objectList() throws RecognitionException {
        ObjectListContext _localctx = new ObjectListContext(_ctx, getState());
        enterRule(_localctx, 54, RULE_objectList);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(284);
            object_();
            setState(289);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la==T__18) {
                {
                {
                setState(285);
                match(T__18);
                setState(286);
                object_();
                }
                }
                setState(291);
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
    public static class Object_Context extends ParserRuleContext {
        public GraphNodeContext graphNode() {
            return getRuleContext(GraphNodeContext.class,0);
        }
        public Object_Context(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_object_; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterObject_(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitObject_(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitObject_(this);
            else return visitor.visitChildren(this);
        }
    }

    public final Object_Context object_() throws RecognitionException {
        Object_Context _localctx = new Object_Context(_ctx, getState());
        enterRule(_localctx, 56, RULE_object_);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(292);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterVerb(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitVerb(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitVerb(this);
            else return visitor.visitChildren(this);
        }
    }

    public final VerbContext verb() throws RecognitionException {
        VerbContext _localctx = new VerbContext(_ctx, getState());
        enterRule(_localctx, 58, RULE_verb);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(294);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSchemaMorphismOrPath(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSchemaMorphismOrPath(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSchemaMorphismOrPath(this);
            else return visitor.visitChildren(this);
        }
    }

    public final SchemaMorphismOrPathContext schemaMorphismOrPath() throws RecognitionException {
        SchemaMorphismOrPathContext _localctx = new SchemaMorphismOrPathContext(_ctx, getState());
        enterRule(_localctx, 60, RULE_schemaMorphismOrPath);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(296);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathAlternative(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathAlternative(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathAlternative(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PathAlternativeContext pathAlternative() throws RecognitionException {
        PathAlternativeContext _localctx = new PathAlternativeContext(_ctx, getState());
        enterRule(_localctx, 62, RULE_pathAlternative);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(298);
            pathSequence();
            setState(303);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la==T__19) {
                {
                {
                setState(299);
                match(T__19);
                setState(300);
                pathSequence();
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathSequence(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathSequence(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathSequence(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PathSequenceContext pathSequence() throws RecognitionException {
        PathSequenceContext _localctx = new PathSequenceContext(_ctx, getState());
        enterRule(_localctx, 64, RULE_pathSequence);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(306);
            pathWithMod();
            setState(311);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la==T__20) {
                {
                {
                setState(307);
                match(T__20);
                setState(308);
                pathWithMod();
                }
                }
                setState(313);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathWithMod(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathWithMod(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathWithMod(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PathWithModContext pathWithMod() throws RecognitionException {
        PathWithModContext _localctx = new PathWithModContext(_ctx, getState());
        enterRule(_localctx, 66, RULE_pathWithMod);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(314);
            pathPrimary();
            setState(316);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 29360128L) != 0)) {
                {
                setState(315);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathMod(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathMod(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathMod(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PathModContext pathMod() throws RecognitionException {
        PathModContext _localctx = new PathModContext(_ctx, getState());
        enterRule(_localctx, 68, RULE_pathMod);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(318);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPathPrimary(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPathPrimary(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPathPrimary(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PathPrimaryContext pathPrimary() throws RecognitionException {
        PathPrimaryContext _localctx = new PathPrimaryContext(_ctx, getState());
        enterRule(_localctx, 70, RULE_pathPrimary);
        try {
            setState(325);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
            case T__26:
            case SCHEMA_MORPHISM:
                enterOuterAlt(_localctx, 1);
                {
                setState(320);
                schemaMorphism();
                }
                break;
            case T__24:
                enterOuterAlt(_localctx, 2);
                {
                {
                setState(321);
                match(T__24);
                setState(322);
                schemaMorphismOrPath();
                setState(323);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterSchemaMorphism(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitSchemaMorphism(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitSchemaMorphism(this);
            else return visitor.visitChildren(this);
        }
    }

    public final SchemaMorphismContext schemaMorphism() throws RecognitionException {
        SchemaMorphismContext _localctx = new SchemaMorphismContext(_ctx, getState());
        enterRule(_localctx, 72, RULE_schemaMorphism);
        try {
            setState(329);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
            case SCHEMA_MORPHISM:
                enterOuterAlt(_localctx, 1);
                {
                setState(327);
                primaryMorphism();
                }
                break;
            case T__26:
                enterOuterAlt(_localctx, 2);
                {
                setState(328);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPrimaryMorphism(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPrimaryMorphism(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPrimaryMorphism(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PrimaryMorphismContext primaryMorphism() throws RecognitionException {
        PrimaryMorphismContext _localctx = new PrimaryMorphismContext(_ctx, getState());
        enterRule(_localctx, 74, RULE_primaryMorphism);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(331);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterDualMorphism(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitDualMorphism(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitDualMorphism(this);
            else return visitor.visitChildren(this);
        }
    }

    public final DualMorphismContext dualMorphism() throws RecognitionException {
        DualMorphismContext _localctx = new DualMorphismContext(_ctx, getState());
        enterRule(_localctx, 76, RULE_dualMorphism);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(333);
            match(T__26);
            setState(334);
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
        public VarOrTermContext varOrTerm() {
            return getRuleContext(VarOrTermContext.class,0);
        }
        public Var_Context var_() {
            return getRuleContext(Var_Context.class,0);
        }
        public GraphNodeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_graphNode; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterGraphNode(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitGraphNode(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitGraphNode(this);
            else return visitor.visitChildren(this);
        }
    }

    public final GraphNodeContext graphNode() throws RecognitionException {
        GraphNodeContext _localctx = new GraphNodeContext(_ctx, getState());
        enterRule(_localctx, 78, RULE_graphNode);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(336);
            varOrTerm();
            setState(339);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la==T__27) {
                {
                setState(337);
                match(T__27);
                setState(338);
                var_();
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
    public static class VarOrTermContext extends ParserRuleContext {
        public Var_Context var_() {
            return getRuleContext(Var_Context.class,0);
        }
        public ConstantTermContext constantTerm() {
            return getRuleContext(ConstantTermContext.class,0);
        }
        public AggregationTermContext aggregationTerm() {
            return getRuleContext(AggregationTermContext.class,0);
        }
        public VarOrTermContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_varOrTerm; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterVarOrTerm(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitVarOrTerm(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitVarOrTerm(this);
            else return visitor.visitChildren(this);
        }
    }

    public final VarOrTermContext varOrTerm() throws RecognitionException {
        VarOrTermContext _localctx = new VarOrTermContext(_ctx, getState());
        enterRule(_localctx, 80, RULE_varOrTerm);
        try {
            setState(344);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
            case VAR1:
            case VAR2:
                enterOuterAlt(_localctx, 1);
                {
                setState(341);
                var_();
                }
                break;
            case T__42:
            case T__43:
            case BLANK_NODE_LABEL:
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
            case INTEGER_POSITIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_NEGATIVE:
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
            case STRING_LITERAL1:
            case STRING_LITERAL2:
            case NIL:
            case ANON:
                enterOuterAlt(_localctx, 2);
                {
                setState(342);
                constantTerm();
                }
                break;
            case T__29:
            case T__30:
            case T__31:
            case T__32:
            case T__33:
                enterOuterAlt(_localctx, 3);
                {
                setState(343);
                aggregationTerm();
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
    public static class Var_Context extends ParserRuleContext {
        public TerminalNode VAR1() { return getToken(QuerycatParser.VAR1, 0); }
        public TerminalNode VAR2() { return getToken(QuerycatParser.VAR2, 0); }
        public Var_Context(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_var_; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterVar_(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitVar_(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitVar_(this);
            else return visitor.visitChildren(this);
        }
    }

    public final Var_Context var_() throws RecognitionException {
        Var_Context _localctx = new Var_Context(_ctx, getState());
        enterRule(_localctx, 82, RULE_var_);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(346);
            _la = _input.LA(1);
            if ( !(_la==VAR1 || _la==VAR2) ) {
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
    public static class ConstantTermContext extends ParserRuleContext {
        public NumericLiteralContext numericLiteral() {
            return getRuleContext(NumericLiteralContext.class,0);
        }
        public BooleanLiteralContext booleanLiteral() {
            return getRuleContext(BooleanLiteralContext.class,0);
        }
        public String_Context string_() {
            return getRuleContext(String_Context.class,0);
        }
        public BlankNodeContext blankNode() {
            return getRuleContext(BlankNodeContext.class,0);
        }
        public TerminalNode NIL() { return getToken(QuerycatParser.NIL, 0); }
        public ConstantTermContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_constantTerm; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConstantTerm(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConstantTerm(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConstantTerm(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ConstantTermContext constantTerm() throws RecognitionException {
        ConstantTermContext _localctx = new ConstantTermContext(_ctx, getState());
        enterRule(_localctx, 84, RULE_constantTerm);
        try {
            setState(353);
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
                setState(348);
                numericLiteral();
                }
                break;
            case T__42:
            case T__43:
                enterOuterAlt(_localctx, 2);
                {
                setState(349);
                booleanLiteral();
                }
                break;
            case STRING_LITERAL1:
            case STRING_LITERAL2:
                enterOuterAlt(_localctx, 3);
                {
                setState(350);
                string_();
                }
                break;
            case BLANK_NODE_LABEL:
            case ANON:
                enterOuterAlt(_localctx, 4);
                {
                setState(351);
                blankNode();
                }
                break;
            case NIL:
                enterOuterAlt(_localctx, 5);
                {
                setState(352);
                match(NIL);
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
    public static class AggregationTermContext extends ParserRuleContext {
        public AggregationFuncContext aggregationFunc() {
            return getRuleContext(AggregationFuncContext.class,0);
        }
        public Var_Context var_() {
            return getRuleContext(Var_Context.class,0);
        }
        public DistinctModifierContext distinctModifier() {
            return getRuleContext(DistinctModifierContext.class,0);
        }
        public AggregationTermContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_aggregationTerm; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterAggregationTerm(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitAggregationTerm(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitAggregationTerm(this);
            else return visitor.visitChildren(this);
        }
    }

    public final AggregationTermContext aggregationTerm() throws RecognitionException {
        AggregationTermContext _localctx = new AggregationTermContext(_ctx, getState());
        enterRule(_localctx, 86, RULE_aggregationTerm);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(355);
            aggregationFunc();
            setState(356);
            match(T__24);
            setState(358);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la==T__28) {
                {
                setState(357);
                distinctModifier();
                }
            }

            setState(360);
            var_();
            setState(361);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterDistinctModifier(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitDistinctModifier(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
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
            setState(363);
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
    public static class AggregationFuncContext extends ParserRuleContext {
        public AggregationFuncContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_aggregationFunc; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterAggregationFunc(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitAggregationFunc(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitAggregationFunc(this);
            else return visitor.visitChildren(this);
        }
    }

    public final AggregationFuncContext aggregationFunc() throws RecognitionException {
        AggregationFuncContext _localctx = new AggregationFuncContext(_ctx, getState());
        enterRule(_localctx, 90, RULE_aggregationFunc);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(365);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterExpression(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitExpression(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ExpressionContext expression() throws RecognitionException {
        ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
        enterRule(_localctx, 92, RULE_expression);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(367);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConditionalOrExpression(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConditionalOrExpression(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConditionalOrExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ConditionalOrExpressionContext conditionalOrExpression() throws RecognitionException {
        ConditionalOrExpressionContext _localctx = new ConditionalOrExpressionContext(_ctx, getState());
        enterRule(_localctx, 94, RULE_conditionalOrExpression);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(369);
            conditionalAndExpression();
            setState(374);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la==T__34) {
                {
                {
                setState(370);
                match(T__34);
                setState(371);
                conditionalAndExpression();
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterConditionalAndExpression(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitConditionalAndExpression(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitConditionalAndExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ConditionalAndExpressionContext conditionalAndExpression() throws RecognitionException {
        ConditionalAndExpressionContext _localctx = new ConditionalAndExpressionContext(_ctx, getState());
        enterRule(_localctx, 96, RULE_conditionalAndExpression);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(377);
            valueLogical();
            setState(382);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la==T__35) {
                {
                {
                setState(378);
                match(T__35);
                setState(379);
                valueLogical();
                }
                }
                setState(384);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterValueLogical(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitValueLogical(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitValueLogical(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ValueLogicalContext valueLogical() throws RecognitionException {
        ValueLogicalContext _localctx = new ValueLogicalContext(_ctx, getState());
        enterRule(_localctx, 98, RULE_valueLogical);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(385);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterRelationalExpression(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitRelationalExpression(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitRelationalExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public final RelationalExpressionContext relationalExpression() throws RecognitionException {
        RelationalExpressionContext _localctx = new RelationalExpressionContext(_ctx, getState());
        enterRule(_localctx, 100, RULE_relationalExpression);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(387);
            expressionPart();
            setState(400);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
            case T__36:
                {
                setState(388);
                match(T__36);
                setState(389);
                expressionPart();
                }
                break;
            case T__37:
                {
                setState(390);
                match(T__37);
                setState(391);
                expressionPart();
                }
                break;
            case T__38:
                {
                setState(392);
                match(T__38);
                setState(393);
                expressionPart();
                }
                break;
            case T__39:
                {
                setState(394);
                match(T__39);
                setState(395);
                expressionPart();
                }
                break;
            case T__40:
                {
                setState(396);
                match(T__40);
                setState(397);
                expressionPart();
                }
                break;
            case T__41:
                {
                setState(398);
                match(T__41);
                setState(399);
                expressionPart();
                }
                break;
            case T__25:
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterExpressionPart(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitExpressionPart(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitExpressionPart(this);
            else return visitor.visitChildren(this);
        }
    }

    public final ExpressionPartContext expressionPart() throws RecognitionException {
        ExpressionPartContext _localctx = new ExpressionPartContext(_ctx, getState());
        enterRule(_localctx, 102, RULE_expressionPart);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(402);
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
        public NumericLiteralContext numericLiteral() {
            return getRuleContext(NumericLiteralContext.class,0);
        }
        public BooleanLiteralContext booleanLiteral() {
            return getRuleContext(BooleanLiteralContext.class,0);
        }
        public String_Context string_() {
            return getRuleContext(String_Context.class,0);
        }
        public VarOrTermContext varOrTerm() {
            return getRuleContext(VarOrTermContext.class,0);
        }
        public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_primaryExpression; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterPrimaryExpression(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitPrimaryExpression(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitPrimaryExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
        PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
        enterRule(_localctx, 104, RULE_primaryExpression);
        try {
            setState(409);
            _errHandler.sync(this);
            switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
            case 1:
                enterOuterAlt(_localctx, 1);
                {
                setState(404);
                brackettedExpression();
                }
                break;
            case 2:
                enterOuterAlt(_localctx, 2);
                {
                setState(405);
                numericLiteral();
                }
                break;
            case 3:
                enterOuterAlt(_localctx, 3);
                {
                setState(406);
                booleanLiteral();
                }
                break;
            case 4:
                enterOuterAlt(_localctx, 4);
                {
                setState(407);
                string_();
                }
                break;
            case 5:
                enterOuterAlt(_localctx, 5);
                {
                setState(408);
                varOrTerm();
                }
                break;
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterBrackettedExpression(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitBrackettedExpression(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitBrackettedExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public final BrackettedExpressionContext brackettedExpression() throws RecognitionException {
        BrackettedExpressionContext _localctx = new BrackettedExpressionContext(_ctx, getState());
        enterRule(_localctx, 106, RULE_brackettedExpression);
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(411);
            match(T__24);
            setState(412);
            expression();
            setState(413);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteral(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteral(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteral(this);
            else return visitor.visitChildren(this);
        }
    }

    public final NumericLiteralContext numericLiteral() throws RecognitionException {
        NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
        enterRule(_localctx, 108, RULE_numericLiteral);
        try {
            setState(418);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
                enterOuterAlt(_localctx, 1);
                {
                setState(415);
                numericLiteralUnsigned();
                }
                break;
            case INTEGER_POSITIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
                enterOuterAlt(_localctx, 2);
                {
                setState(416);
                numericLiteralPositive();
                }
                break;
            case INTEGER_NEGATIVE:
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
                enterOuterAlt(_localctx, 3);
                {
                setState(417);
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteralUnsigned(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteralUnsigned(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteralUnsigned(this);
            else return visitor.visitChildren(this);
        }
    }

    public final NumericLiteralUnsignedContext numericLiteralUnsigned() throws RecognitionException {
        NumericLiteralUnsignedContext _localctx = new NumericLiteralUnsignedContext(_ctx, getState());
        enterRule(_localctx, 110, RULE_numericLiteralUnsigned);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(420);
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
    public static class NumericLiteralPositiveContext extends ParserRuleContext {
        public TerminalNode INTEGER_POSITIVE() { return getToken(QuerycatParser.INTEGER_POSITIVE, 0); }
        public TerminalNode DECIMAL_POSITIVE() { return getToken(QuerycatParser.DECIMAL_POSITIVE, 0); }
        public TerminalNode DOUBLE_POSITIVE() { return getToken(QuerycatParser.DOUBLE_POSITIVE, 0); }
        public NumericLiteralPositiveContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_numericLiteralPositive; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteralPositive(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteralPositive(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteralPositive(this);
            else return visitor.visitChildren(this);
        }
    }

    public final NumericLiteralPositiveContext numericLiteralPositive() throws RecognitionException {
        NumericLiteralPositiveContext _localctx = new NumericLiteralPositiveContext(_ctx, getState());
        enterRule(_localctx, 112, RULE_numericLiteralPositive);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(422);
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
    public static class NumericLiteralNegativeContext extends ParserRuleContext {
        public TerminalNode INTEGER_NEGATIVE() { return getToken(QuerycatParser.INTEGER_NEGATIVE, 0); }
        public TerminalNode DECIMAL_NEGATIVE() { return getToken(QuerycatParser.DECIMAL_NEGATIVE, 0); }
        public TerminalNode DOUBLE_NEGATIVE() { return getToken(QuerycatParser.DOUBLE_NEGATIVE, 0); }
        public NumericLiteralNegativeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_numericLiteralNegative; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterNumericLiteralNegative(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitNumericLiteralNegative(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitNumericLiteralNegative(this);
            else return visitor.visitChildren(this);
        }
    }

    public final NumericLiteralNegativeContext numericLiteralNegative() throws RecognitionException {
        NumericLiteralNegativeContext _localctx = new NumericLiteralNegativeContext(_ctx, getState());
        enterRule(_localctx, 114, RULE_numericLiteralNegative);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(424);
            _la = _input.LA(1);
            if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 504403158265495552L) != 0)) ) {
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
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterBooleanLiteral(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitBooleanLiteral(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitBooleanLiteral(this);
            else return visitor.visitChildren(this);
        }
    }

    public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
        BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
        enterRule(_localctx, 116, RULE_booleanLiteral);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(426);
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
    public static class String_Context extends ParserRuleContext {
        public TerminalNode STRING_LITERAL1() { return getToken(QuerycatParser.STRING_LITERAL1, 0); }
        public TerminalNode STRING_LITERAL2() { return getToken(QuerycatParser.STRING_LITERAL2, 0); }
        public String_Context(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_string_; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterString_(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitString_(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitString_(this);
            else return visitor.visitChildren(this);
        }
    }

    public final String_Context string_() throws RecognitionException {
        String_Context _localctx = new String_Context(_ctx, getState());
        enterRule(_localctx, 118, RULE_string_);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(428);
            _la = _input.LA(1);
            if ( !(_la==STRING_LITERAL1 || _la==STRING_LITERAL2) ) {
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
    public static class BlankNodeContext extends ParserRuleContext {
        public TerminalNode BLANK_NODE_LABEL() { return getToken(QuerycatParser.BLANK_NODE_LABEL, 0); }
        public TerminalNode ANON() { return getToken(QuerycatParser.ANON, 0); }
        public BlankNodeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return RULE_blankNode; }
        @Override public void enterRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).enterBlankNode(this);
        }
        @Override public void exitRule(ParseTreeListener listener) {
            if ( listener instanceof QuerycatListener ) ((QuerycatListener)listener).exitBlankNode(this);
        }
        @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if ( visitor instanceof QuerycatVisitor ) return ((QuerycatVisitor<? extends T>)visitor).visitBlankNode(this);
            else return visitor.visitChildren(this);
        }
    }

    public final BlankNodeContext blankNode() throws RecognitionException {
        BlankNodeContext _localctx = new BlankNodeContext(_ctx, getState());
        enterRule(_localctx, 120, RULE_blankNode);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
            setState(430);
            _la = _input.LA(1);
            if ( !(_la==BLANK_NODE_LABEL || _la==ANON) ) {
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
        "\u0004\u0001G\u01b1\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
        "\u0001\b\u0001\b\u0003\b\u00a0\b\b\u0001\b\u0001\b\u0003\b\u00a4\b\b\u0003"+
        "\b\u00a6\b\b\u0001\t\u0001\t\u0001\t\u0004\t\u00ab\b\t\u000b\t\f\t\u00ac"+
        "\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00b3\b\n\u0003\n\u00b5\b\n\u0001"+
        "\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r"+
        "\u0001\r\u0003\r\u00c0\b\r\u0001\r\u0001\r\u0003\r\u00c4\b\r\u0001\r\u0003"+
        "\r\u00c7\b\r\u0001\r\u0003\r\u00ca\b\r\u0005\r\u00cc\b\r\n\r\f\r\u00cf"+
        "\t\r\u0003\r\u00d1\b\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e"+
        "\u0003\u000e\u00d8\b\u000e\u0003\u000e\u00da\b\u000e\u0001\u000f\u0001"+
        "\u000f\u0001\u000f\u0003\u000f\u00df\b\u000f\u0001\u0010\u0001\u0010\u0001"+
        "\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u00e7\b\u0011\n"+
        "\u0011\f\u0011\u00ea\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
        "\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00f2\b\u0013\n\u0013\f\u0013"+
        "\u00f5\t\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
        "\u0003\u0014\u00fc\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016"+
        "\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0106\b\u0017"+
        "\u0003\u0017\u0108\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019"+
        "\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019"+
        "\u0113\b\u0019\u0005\u0019\u0115\b\u0019\n\u0019\f\u0019\u0118\t\u0019"+
        "\u0001\u001a\u0003\u001a\u011b\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
        "\u0005\u001b\u0120\b\u001b\n\u001b\f\u001b\u0123\t\u001b\u0001\u001c\u0001"+
        "\u001c\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001f\u0001"+
        "\u001f\u0001\u001f\u0005\u001f\u012e\b\u001f\n\u001f\f\u001f\u0131\t\u001f"+
        "\u0001 \u0001 \u0001 \u0005 \u0136\b \n \f \u0139\t \u0001!\u0001!\u0003"+
        "!\u013d\b!\u0001\"\u0001\"\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u0146"+
        "\b#\u0001$\u0001$\u0003$\u014a\b$\u0001%\u0001%\u0001&\u0001&\u0001&\u0001"+
        "\'\u0001\'\u0001\'\u0003\'\u0154\b\'\u0001(\u0001(\u0001(\u0003(\u0159"+
        "\b(\u0001)\u0001)\u0001*\u0001*\u0001*\u0001*\u0001*\u0003*\u0162\b*\u0001"+
        "+\u0001+\u0001+\u0003+\u0167\b+\u0001+\u0001+\u0001+\u0001,\u0001,\u0001"+
        "-\u0001-\u0001.\u0001.\u0001/\u0001/\u0001/\u0005/\u0175\b/\n/\f/\u0178"+
        "\t/\u00010\u00010\u00010\u00050\u017d\b0\n0\f0\u0180\t0\u00011\u00011"+
        "\u00012\u00012\u00012\u00012\u00012\u00012\u00012\u00012\u00012\u0001"+
        "2\u00012\u00012\u00012\u00032\u0191\b2\u00013\u00013\u00014\u00014\u0001"+
        "4\u00014\u00014\u00034\u019a\b4\u00015\u00015\u00015\u00015\u00016\u0001"+
        "6\u00016\u00036\u01a3\b6\u00017\u00017\u00018\u00018\u00019\u00019\u0001"+
        ":\u0001:\u0001;\u0001;\u0001<\u0001<\u0001<\u0000\u0000=\u0000\u0002\u0004"+
        "\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \""+
        "$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvx\u0000\u000b\u0001\u0000\b"+
        "\t\u0001\u0000\u000e\u000f\u0001\u0000\u0016\u0018\u0001\u000001\u0001"+
        "\u0000\u001e\"\u0001\u000024\u0001\u000057\u0001\u00008:\u0001\u0000+"+
        ",\u0001\u0000<=\u0002\u0000//BB\u01ad\u0000z\u0001\u0000\u0000\u0000\u0002"+
        "}\u0001\u0000\u0000\u0000\u0004\u0084\u0001\u0000\u0000\u0000\u0006\u0086"+
        "\u0001\u0000\u0000\u0000\b\u0089\u0001\u0000\u0000\u0000\n\u008f\u0001"+
        "\u0000\u0000\u0000\f\u0093\u0001\u0000\u0000\u0000\u000e\u0098\u0001\u0000"+
        "\u0000\u0000\u0010\u00a5\u0001\u0000\u0000\u0000\u0012\u00a7\u0001\u0000"+
        "\u0000\u0000\u0014\u00b4\u0001\u0000\u0000\u0000\u0016\u00b6\u0001\u0000"+
        "\u0000\u0000\u0018\u00b9\u0001\u0000\u0000\u0000\u001a\u00bc\u0001\u0000"+
        "\u0000\u0000\u001c\u00d4\u0001\u0000\u0000\u0000\u001e\u00de\u0001\u0000"+
        "\u0000\u0000 \u00e0\u0001\u0000\u0000\u0000\"\u00e3\u0001\u0000\u0000"+
        "\u0000$\u00eb\u0001\u0000\u0000\u0000&\u00ee\u0001\u0000\u0000\u0000("+
        "\u00fb\u0001\u0000\u0000\u0000*\u00fd\u0001\u0000\u0000\u0000,\u0100\u0001"+
        "\u0000\u0000\u0000.\u0102\u0001\u0000\u0000\u00000\u0109\u0001\u0000\u0000"+
        "\u00002\u010c\u0001\u0000\u0000\u00004\u011a\u0001\u0000\u0000\u00006"+
        "\u011c\u0001\u0000\u0000\u00008\u0124\u0001\u0000\u0000\u0000:\u0126\u0001"+
        "\u0000\u0000\u0000<\u0128\u0001\u0000\u0000\u0000>\u012a\u0001\u0000\u0000"+
        "\u0000@\u0132\u0001\u0000\u0000\u0000B\u013a\u0001\u0000\u0000\u0000D"+
        "\u013e\u0001\u0000\u0000\u0000F\u0145\u0001\u0000\u0000\u0000H\u0149\u0001"+
        "\u0000\u0000\u0000J\u014b\u0001\u0000\u0000\u0000L\u014d\u0001\u0000\u0000"+
        "\u0000N\u0150\u0001\u0000\u0000\u0000P\u0158\u0001\u0000\u0000\u0000R"+
        "\u015a\u0001\u0000\u0000\u0000T\u0161\u0001\u0000\u0000\u0000V\u0163\u0001"+
        "\u0000\u0000\u0000X\u016b\u0001\u0000\u0000\u0000Z\u016d\u0001\u0000\u0000"+
        "\u0000\\\u016f\u0001\u0000\u0000\u0000^\u0171\u0001\u0000\u0000\u0000"+
        "`\u0179\u0001\u0000\u0000\u0000b\u0181\u0001\u0000\u0000\u0000d\u0183"+
        "\u0001\u0000\u0000\u0000f\u0192\u0001\u0000\u0000\u0000h\u0199\u0001\u0000"+
        "\u0000\u0000j\u019b\u0001\u0000\u0000\u0000l\u01a2\u0001\u0000\u0000\u0000"+
        "n\u01a4\u0001\u0000\u0000\u0000p\u01a6\u0001\u0000\u0000\u0000r\u01a8"+
        "\u0001\u0000\u0000\u0000t\u01aa\u0001\u0000\u0000\u0000v\u01ac\u0001\u0000"+
        "\u0000\u0000x\u01ae\u0001\u0000\u0000\u0000z{\u0003\u0002\u0001\u0000"+
        "{|\u0005\u0000\u0000\u0001|\u0001\u0001\u0000\u0000\u0000}\u007f\u0003"+
        "\u0006\u0003\u0000~\u0080\u0003\n\u0005\u0000\u007f~\u0001\u0000\u0000"+
        "\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000"+
        "\u0000\u0081\u0082\u0003\f\u0006\u0000\u0082\u0083\u0003\u000e\u0007\u0000"+
        "\u0083\u0003\u0001\u0000\u0000\u0000\u0084\u0085\u0003\u0002\u0001\u0000"+
        "\u0085\u0005\u0001\u0000\u0000\u0000\u0086\u0087\u0005\u0001\u0000\u0000"+
        "\u0087\u0088\u0003\b\u0004\u0000\u0088\u0007\u0001\u0000\u0000\u0000\u0089"+
        "\u008b\u0005\u0002\u0000\u0000\u008a\u008c\u0003.\u0017\u0000\u008b\u008a"+
        "\u0001\u0000\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008d"+
        "\u0001\u0000\u0000\u0000\u008d\u008e\u0005\u0003\u0000\u0000\u008e\t\u0001"+
        "\u0000\u0000\u0000\u008f\u0090\u0005\u0004\u0000\u0000\u0090\u0091\u0005"+
        ".\u0000\u0000\u0091\u000b\u0001\u0000\u0000\u0000\u0092\u0094\u0005\u0005"+
        "\u0000\u0000\u0093\u0092\u0001\u0000\u0000\u0000\u0093\u0094\u0001\u0000"+
        "\u0000\u0000\u0094\u0095\u0001\u0000\u0000\u0000\u0095\u0096\u0003\u001a"+
        "\r\u0000\u0096\r\u0001\u0000\u0000\u0000\u0097\u0099\u0003\u0012\t\u0000"+
        "\u0098\u0097\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000"+
        "\u0099\u009b\u0001\u0000\u0000\u0000\u009a\u009c\u0003\u0010\b\u0000\u009b"+
        "\u009a\u0001\u0000\u0000\u0000\u009b\u009c\u0001\u0000\u0000\u0000\u009c"+
        "\u000f\u0001\u0000\u0000\u0000\u009d\u009f\u0003\u0016\u000b\u0000\u009e"+
        "\u00a0\u0003\u0018\f\u0000\u009f\u009e\u0001\u0000\u0000\u0000\u009f\u00a0"+
        "\u0001\u0000\u0000\u0000\u00a0\u00a6\u0001\u0000\u0000\u0000\u00a1\u00a3"+
        "\u0003\u0018\f\u0000\u00a2\u00a4\u0003\u0016\u000b\u0000\u00a3\u00a2\u0001"+
        "\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4\u00a6\u0001"+
        "\u0000\u0000\u0000\u00a5\u009d\u0001\u0000\u0000\u0000\u00a5\u00a1\u0001"+
        "\u0000\u0000\u0000\u00a6\u0011\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005"+
        "\u0006\u0000\u0000\u00a8\u00aa\u0005\u0007\u0000\u0000\u00a9\u00ab\u0003"+
        "\u0014\n\u0000\u00aa\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000"+
        "\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000"+
        "\u0000\u0000\u00ad\u0013\u0001\u0000\u0000\u0000\u00ae\u00af\u0007\u0000"+
        "\u0000\u0000\u00af\u00b5\u0003j5\u0000\u00b0\u00b3\u0003,\u0016\u0000"+
        "\u00b1\u00b3\u0003R)\u0000\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b1"+
        "\u0001\u0000\u0000\u0000\u00b3\u00b5\u0001\u0000\u0000\u0000\u00b4\u00ae"+
        "\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b5\u0015"+
        "\u0001\u0000\u0000\u0000\u00b6\u00b7\u0005\n\u0000\u0000\u00b7\u00b8\u0005"+
        "2\u0000\u0000\u00b8\u0017\u0001\u0000\u0000\u0000\u00b9\u00ba\u0005\u000b"+
        "\u0000\u0000\u00ba\u00bb\u00052\u0000\u0000\u00bb\u0019\u0001\u0000\u0000"+
        "\u0000\u00bc\u00d0\u0005\u0002\u0000\u0000\u00bd\u00d1\u0003\u0004\u0002"+
        "\u0000\u00be\u00c0\u0003\u001c\u000e\u0000\u00bf\u00be\u0001\u0000\u0000"+
        "\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00cd\u0001\u0000\u0000"+
        "\u0000\u00c1\u00c4\u0003\u001e\u000f\u0000\u00c2\u00c4\u0003*\u0015\u0000"+
        "\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c3\u00c2\u0001\u0000\u0000\u0000"+
        "\u00c4\u00c6\u0001\u0000\u0000\u0000\u00c5\u00c7\u0005\f\u0000\u0000\u00c6"+
        "\u00c5\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000\u0000\u0000\u00c7"+
        "\u00c9\u0001\u0000\u0000\u0000\u00c8\u00ca\u0003\u001c\u000e\u0000\u00c9"+
        "\u00c8\u0001\u0000\u0000\u0000\u00c9\u00ca\u0001\u0000\u0000\u0000\u00ca"+
        "\u00cc\u0001\u0000\u0000\u0000\u00cb\u00c3\u0001\u0000\u0000\u0000\u00cc"+
        "\u00cf\u0001\u0000\u0000\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00cd"+
        "\u00ce\u0001\u0000\u0000\u0000\u00ce\u00d1\u0001\u0000\u0000\u0000\u00cf"+
        "\u00cd\u0001\u0000\u0000\u0000\u00d0\u00bd\u0001\u0000\u0000\u0000\u00d0"+
        "\u00bf\u0001\u0000\u0000\u0000\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2"+
        "\u00d3\u0005\u0003\u0000\u0000\u00d3\u001b\u0001\u0000\u0000\u0000\u00d4"+
        "\u00d9\u00030\u0018\u0000\u00d5\u00d7\u0005\f\u0000\u0000\u00d6\u00d8"+
        "\u0003\u001c\u000e\u0000\u00d7\u00d6\u0001\u0000\u0000\u0000\u00d7\u00d8"+
        "\u0001\u0000\u0000\u0000\u00d8\u00da\u0001\u0000\u0000\u0000\u00d9\u00d5"+
        "\u0001\u0000\u0000\u0000\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u001d"+
        "\u0001\u0000\u0000\u0000\u00db\u00df\u0003 \u0010\u0000\u00dc\u00df\u0003"+
        "\"\u0011\u0000\u00dd\u00df\u0003$\u0012\u0000\u00de\u00db\u0001\u0000"+
        "\u0000\u0000\u00de\u00dc\u0001\u0000\u0000\u0000\u00de\u00dd\u0001\u0000"+
        "\u0000\u0000\u00df\u001f\u0001\u0000\u0000\u0000\u00e0\u00e1\u0005\r\u0000"+
        "\u0000\u00e1\u00e2\u0003\u001a\r\u0000\u00e2!\u0001\u0000\u0000\u0000"+
        "\u00e3\u00e8\u0003\u001a\r\u0000\u00e4\u00e5\u0007\u0001\u0000\u0000\u00e5"+
        "\u00e7\u0003\u001a\r\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e7\u00ea"+
        "\u0001\u0000\u0000\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e8\u00e9"+
        "\u0001\u0000\u0000\u0000\u00e9#\u0001\u0000\u0000\u0000\u00ea\u00e8\u0001"+
        "\u0000\u0000\u0000\u00eb\u00ec\u0005\u0010\u0000\u0000\u00ec\u00ed\u0003"+
        "&\u0013\u0000\u00ed%\u0001\u0000\u0000\u0000\u00ee\u00ef\u0003R)\u0000"+
        "\u00ef\u00f3\u0005\u0002\u0000\u0000\u00f0\u00f2\u0003(\u0014\u0000\u00f1"+
        "\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f5\u0001\u0000\u0000\u0000\u00f3"+
        "\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001\u0000\u0000\u0000\u00f4"+
        "\u00f6\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000\u00f6"+
        "\u00f7\u0005\u0003\u0000\u0000\u00f7\'\u0001\u0000\u0000\u0000\u00f8\u00fc"+
        "\u0003l6\u0000\u00f9\u00fc\u0003t:\u0000\u00fa\u00fc\u0003v;\u0000\u00fb"+
        "\u00f8\u0001\u0000\u0000\u0000\u00fb\u00f9\u0001\u0000\u0000\u0000\u00fb"+
        "\u00fa\u0001\u0000\u0000\u0000\u00fc)\u0001\u0000\u0000\u0000\u00fd\u00fe"+
        "\u0005\u0011\u0000\u0000\u00fe\u00ff\u0003,\u0016\u0000\u00ff+\u0001\u0000"+
        "\u0000\u0000\u0100\u0101\u0003j5\u0000\u0101-\u0001\u0000\u0000\u0000"+
        "\u0102\u0107\u00030\u0018\u0000\u0103\u0105\u0005\f\u0000\u0000\u0104"+
        "\u0106\u0003.\u0017\u0000\u0105\u0104\u0001\u0000\u0000\u0000\u0105\u0106"+
        "\u0001\u0000\u0000\u0000\u0106\u0108\u0001\u0000\u0000\u0000\u0107\u0103"+
        "\u0001\u0000\u0000\u0000\u0107\u0108\u0001\u0000\u0000\u0000\u0108/\u0001"+
        "\u0000\u0000\u0000\u0109\u010a\u0003P(\u0000\u010a\u010b\u00032\u0019"+
        "\u0000\u010b1\u0001\u0000\u0000\u0000\u010c\u010d\u0003:\u001d\u0000\u010d"+
        "\u0116\u00036\u001b\u0000\u010e\u0112\u0005\u0012\u0000\u0000\u010f\u0110"+
        "\u0003:\u001d\u0000\u0110\u0111\u00036\u001b\u0000\u0111\u0113\u0001\u0000"+
        "\u0000\u0000\u0112\u010f\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000"+
        "\u0000\u0000\u0113\u0115\u0001\u0000\u0000\u0000\u0114\u010e\u0001\u0000"+
        "\u0000\u0000\u0115\u0118\u0001\u0000\u0000\u0000\u0116\u0114\u0001\u0000"+
        "\u0000\u0000\u0116\u0117\u0001\u0000\u0000\u0000\u01173\u0001\u0000\u0000"+
        "\u0000\u0118\u0116\u0001\u0000\u0000\u0000\u0119\u011b\u00032\u0019\u0000"+
        "\u011a\u0119\u0001\u0000\u0000\u0000\u011a\u011b\u0001\u0000\u0000\u0000"+
        "\u011b5\u0001\u0000\u0000\u0000\u011c\u0121\u00038\u001c\u0000\u011d\u011e"+
        "\u0005\u0013\u0000\u0000\u011e\u0120\u00038\u001c\u0000\u011f\u011d\u0001"+
        "\u0000\u0000\u0000\u0120\u0123\u0001\u0000\u0000\u0000\u0121\u011f\u0001"+
        "\u0000\u0000\u0000\u0121\u0122\u0001\u0000\u0000\u0000\u01227\u0001\u0000"+
        "\u0000\u0000\u0123\u0121\u0001\u0000\u0000\u0000\u0124\u0125\u0003N\'"+
        "\u0000\u01259\u0001\u0000\u0000\u0000\u0126\u0127\u0003<\u001e\u0000\u0127"+
        ";\u0001\u0000\u0000\u0000\u0128\u0129\u0003>\u001f\u0000\u0129=\u0001"+
        "\u0000\u0000\u0000\u012a\u012f\u0003@ \u0000\u012b\u012c\u0005\u0014\u0000"+
        "\u0000\u012c\u012e\u0003@ \u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012e"+
        "\u0131\u0001\u0000\u0000\u0000\u012f\u012d\u0001\u0000\u0000\u0000\u012f"+
        "\u0130\u0001\u0000\u0000\u0000\u0130?\u0001\u0000\u0000\u0000\u0131\u012f"+
        "\u0001\u0000\u0000\u0000\u0132\u0137\u0003B!\u0000\u0133\u0134\u0005\u0015"+
        "\u0000\u0000\u0134\u0136\u0003B!\u0000\u0135\u0133\u0001\u0000\u0000\u0000"+
        "\u0136\u0139\u0001\u0000\u0000\u0000\u0137\u0135\u0001\u0000\u0000\u0000"+
        "\u0137\u0138\u0001\u0000\u0000\u0000\u0138A\u0001\u0000\u0000\u0000\u0139"+
        "\u0137\u0001\u0000\u0000\u0000\u013a\u013c\u0003F#\u0000\u013b\u013d\u0003"+
        "D\"\u0000\u013c\u013b\u0001\u0000\u0000\u0000\u013c\u013d\u0001\u0000"+
        "\u0000\u0000\u013dC\u0001\u0000\u0000\u0000\u013e\u013f\u0007\u0002\u0000"+
        "\u0000\u013fE\u0001\u0000\u0000\u0000\u0140\u0146\u0003H$\u0000\u0141"+
        "\u0142\u0005\u0019\u0000\u0000\u0142\u0143\u0003<\u001e\u0000\u0143\u0144"+
        "\u0005\u001a\u0000\u0000\u0144\u0146\u0001\u0000\u0000\u0000\u0145\u0140"+
        "\u0001\u0000\u0000\u0000\u0145\u0141\u0001\u0000\u0000\u0000\u0146G\u0001"+
        "\u0000\u0000\u0000\u0147\u014a\u0003J%\u0000\u0148\u014a\u0003L&\u0000"+
        "\u0149\u0147\u0001\u0000\u0000\u0000\u0149\u0148\u0001\u0000\u0000\u0000"+
        "\u014aI\u0001\u0000\u0000\u0000\u014b\u014c\u0005-\u0000\u0000\u014cK"+
        "\u0001\u0000\u0000\u0000\u014d\u014e\u0005\u001b\u0000\u0000\u014e\u014f"+
        "\u0003J%\u0000\u014fM\u0001\u0000\u0000\u0000\u0150\u0153\u0003P(\u0000"+
        "\u0151\u0152\u0005\u001c\u0000\u0000\u0152\u0154\u0003R)\u0000\u0153\u0151"+
        "\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000\u0154O\u0001"+
        "\u0000\u0000\u0000\u0155\u0159\u0003R)\u0000\u0156\u0159\u0003T*\u0000"+
        "\u0157\u0159\u0003V+\u0000\u0158\u0155\u0001\u0000\u0000\u0000\u0158\u0156"+
        "\u0001\u0000\u0000\u0000\u0158\u0157\u0001\u0000\u0000\u0000\u0159Q\u0001"+
        "\u0000\u0000\u0000\u015a\u015b\u0007\u0003\u0000\u0000\u015bS\u0001\u0000"+
        "\u0000\u0000\u015c\u0162\u0003l6\u0000\u015d\u0162\u0003t:\u0000\u015e"+
        "\u0162\u0003v;\u0000\u015f\u0162\u0003x<\u0000\u0160\u0162\u0005A\u0000"+
        "\u0000\u0161\u015c\u0001\u0000\u0000\u0000\u0161\u015d\u0001\u0000\u0000"+
        "\u0000\u0161\u015e\u0001\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000"+
        "\u0000\u0161\u0160\u0001\u0000\u0000\u0000\u0162U\u0001\u0000\u0000\u0000"+
        "\u0163\u0164\u0003Z-\u0000\u0164\u0166\u0005\u0019\u0000\u0000\u0165\u0167"+
        "\u0003X,\u0000\u0166\u0165\u0001\u0000\u0000\u0000\u0166\u0167\u0001\u0000"+
        "\u0000\u0000\u0167\u0168\u0001\u0000\u0000\u0000\u0168\u0169\u0003R)\u0000"+
        "\u0169\u016a\u0005\u001a\u0000\u0000\u016aW\u0001\u0000\u0000\u0000\u016b"+
        "\u016c\u0005\u001d\u0000\u0000\u016cY\u0001\u0000\u0000\u0000\u016d\u016e"+
        "\u0007\u0004\u0000\u0000\u016e[\u0001\u0000\u0000\u0000\u016f\u0170\u0003"+
        "^/\u0000\u0170]\u0001\u0000\u0000\u0000\u0171\u0176\u0003`0\u0000\u0172"+
        "\u0173\u0005#\u0000\u0000\u0173\u0175\u0003`0\u0000\u0174\u0172\u0001"+
        "\u0000\u0000\u0000\u0175\u0178\u0001\u0000\u0000\u0000\u0176\u0174\u0001"+
        "\u0000\u0000\u0000\u0176\u0177\u0001\u0000\u0000\u0000\u0177_\u0001\u0000"+
        "\u0000\u0000\u0178\u0176\u0001\u0000\u0000\u0000\u0179\u017e\u0003b1\u0000"+
        "\u017a\u017b\u0005$\u0000\u0000\u017b\u017d\u0003b1\u0000\u017c\u017a"+
        "\u0001\u0000\u0000\u0000\u017d\u0180\u0001\u0000\u0000\u0000\u017e\u017c"+
        "\u0001\u0000\u0000\u0000\u017e\u017f\u0001\u0000\u0000\u0000\u017fa\u0001"+
        "\u0000\u0000\u0000\u0180\u017e\u0001\u0000\u0000\u0000\u0181\u0182\u0003"+
        "d2\u0000\u0182c\u0001\u0000\u0000\u0000\u0183\u0190\u0003f3\u0000\u0184"+
        "\u0185\u0005%\u0000\u0000\u0185\u0191\u0003f3\u0000\u0186\u0187\u0005"+
        "&\u0000\u0000\u0187\u0191\u0003f3\u0000\u0188\u0189\u0005\'\u0000\u0000"+
        "\u0189\u0191\u0003f3\u0000\u018a\u018b\u0005(\u0000\u0000\u018b\u0191"+
        "\u0003f3\u0000\u018c\u018d\u0005)\u0000\u0000\u018d\u0191\u0003f3\u0000"+
        "\u018e\u018f\u0005*\u0000\u0000\u018f\u0191\u0003f3\u0000\u0190\u0184"+
        "\u0001\u0000\u0000\u0000\u0190\u0186\u0001\u0000\u0000\u0000\u0190\u0188"+
        "\u0001\u0000\u0000\u0000\u0190\u018a\u0001\u0000\u0000\u0000\u0190\u018c"+
        "\u0001\u0000\u0000\u0000\u0190\u018e\u0001\u0000\u0000\u0000\u0190\u0191"+
        "\u0001\u0000\u0000\u0000\u0191e\u0001\u0000\u0000\u0000\u0192\u0193\u0003"+
        "h4\u0000\u0193g\u0001\u0000\u0000\u0000\u0194\u019a\u0003j5\u0000\u0195"+
        "\u019a\u0003l6\u0000\u0196\u019a\u0003t:\u0000\u0197\u019a\u0003v;\u0000"+
        "\u0198\u019a\u0003P(\u0000\u0199\u0194\u0001\u0000\u0000\u0000\u0199\u0195"+
        "\u0001\u0000\u0000\u0000\u0199\u0196\u0001\u0000\u0000\u0000\u0199\u0197"+
        "\u0001\u0000\u0000\u0000\u0199\u0198\u0001\u0000\u0000\u0000\u019ai\u0001"+
        "\u0000\u0000\u0000\u019b\u019c\u0005\u0019\u0000\u0000\u019c\u019d\u0003"+
        "\\.\u0000\u019d\u019e\u0005\u001a\u0000\u0000\u019ek\u0001\u0000\u0000"+
        "\u0000\u019f\u01a3\u0003n7\u0000\u01a0\u01a3\u0003p8\u0000\u01a1\u01a3"+
        "\u0003r9\u0000\u01a2\u019f\u0001\u0000\u0000\u0000\u01a2\u01a0\u0001\u0000"+
        "\u0000\u0000\u01a2\u01a1\u0001\u0000\u0000\u0000\u01a3m\u0001\u0000\u0000"+
        "\u0000\u01a4\u01a5\u0007\u0005\u0000\u0000\u01a5o\u0001\u0000\u0000\u0000"+
        "\u01a6\u01a7\u0007\u0006\u0000\u0000\u01a7q\u0001\u0000\u0000\u0000\u01a8"+
        "\u01a9\u0007\u0007\u0000\u0000\u01a9s\u0001\u0000\u0000\u0000\u01aa\u01ab"+
        "\u0007\b\u0000\u0000\u01abu\u0001\u0000\u0000\u0000\u01ac\u01ad\u0007"+
        "\t\u0000\u0000\u01adw\u0001\u0000\u0000\u0000\u01ae\u01af\u0007\n\u0000"+
        "\u0000\u01afy\u0001\u0000\u0000\u0000+\u007f\u008b\u0093\u0098\u009b\u009f"+
        "\u00a3\u00a5\u00ac\u00b2\u00b4\u00bf\u00c3\u00c6\u00c9\u00cd\u00d0\u00d7"+
        "\u00d9\u00de\u00e8\u00f3\u00fb\u0105\u0107\u0112\u0116\u011a\u0121\u012f"+
        "\u0137\u013c\u0145\u0149\u0153\u0158\u0161\u0166\u0176\u017e\u0190\u0199"+
        "\u01a2";
    public static final ATN _ATN =
        new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
