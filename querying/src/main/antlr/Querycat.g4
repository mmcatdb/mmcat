grammar Querycat;

query: selectQuery EOF;

selectQuery: selectClause fromClause? whereClause solutionModifier;

subSelect: selectQuery;

selectClause: 'SELECT' selectGraphPattern;

selectGraphPattern: '{' selectTriples? '}';

fromClause: 'FROM' SCHEMA_IDENTIFIER;

whereClause: 'WHERE'? groupGraphPattern;

solutionModifier: orderClause? limitOffsetClauses?;

limitOffsetClauses: (
		limitClause offsetClause?
		| offsetClause limitClause?
	);

orderClause: 'ORDER' 'BY' orderCondition+;

orderCondition: (( 'ASC' | 'DESC') brackettedExpression)
	| ( constraint | var_);

limitClause: 'LIMIT' INTEGER;

offsetClause: 'OFFSET' INTEGER;

groupGraphPattern:
	'{' (subSelect | (triplesBlock? (
		(graphPatternNotTriples | filter_) '.'? triplesBlock?
	)*)) '}';

triplesBlock: triplesSameSubject ( '.' triplesBlock?)?;

graphPatternNotTriples:
	optionalGraphPattern
	| groupOrUnionGraphPattern
	| inlineData;

optionalGraphPattern: 'OPTIONAL' groupGraphPattern;

groupOrUnionGraphPattern:
	groupGraphPattern (('UNION' | 'MINUS') groupGraphPattern)*;

inlineData: 'VALUES' dataBlock;

dataBlock: var_ '{' dataBlockValue* '}';

dataBlockValue: numericLiteral
	| booleanLiteral
	| string_;

filter_: 'FILTER' constraint;

constraint: brackettedExpression;

selectTriples: triplesSameSubject ( '.' selectTriples?)?;

triplesSameSubject: varOrTerm propertyListNotEmpty;

propertyListNotEmpty: verb objectList ( ';' ( verb objectList)?)*;

propertyList: propertyListNotEmpty?;

objectList: object_ ( ',' object_)*;

object_: graphNode;

verb: schemaMorphismOrPath;

schemaMorphismOrPath: pathAlternative;

pathAlternative: pathSequence ( '|' pathSequence )*;

pathSequence: pathWithMod ( '/' pathWithMod )*;

pathWithMod: pathPrimary pathMod?;

pathMod: '?' | '*' | '+';

pathPrimary: schemaMorphism | ( '(' schemaMorphismOrPath ')' ) ;

schemaMorphism: primaryMorphism | dualMorphism;

primaryMorphism: SCHEMA_MORPHISM;

dualMorphism: '-' primaryMorphism;

graphNode: varOrTerm ( 'AS' var_)?;

varOrTerm: var_ | constantTerm | aggregationTerm;

var_: VAR1 | VAR2;

constantTerm:
	numericLiteral
	| booleanLiteral
	| string_
	| blankNode
	| NIL;

aggregationTerm:
	aggregationFunc '(' (distinctModifier)? var_ ')';

distinctModifier:
	'DISTINCT';

aggregationFunc:
	'COUNT'
	| 'SUM'
	| 'AVG'
	| 'MIN'
	| 'MAX';

expression: conditionalOrExpression;

conditionalOrExpression:
	conditionalAndExpression ('||' conditionalAndExpression)*;

conditionalAndExpression: valueLogical ( '&&' valueLogical)*;

valueLogical: relationalExpression;

relationalExpression:
	expressionPart (
		'=' expressionPart
		| '!=' expressionPart
		| '<' expressionPart
		| '>' expressionPart
		| '<=' expressionPart
		| '>=' expressionPart
	)?;

expressionPart: primaryExpression;

primaryExpression:
	brackettedExpression
	| numericLiteral
	| booleanLiteral
	| string_
	| varOrTerm;

brackettedExpression: '(' expression ')';

numericLiteral:
	numericLiteralUnsigned
	| numericLiteralPositive
	| numericLiteralNegative;

numericLiteralUnsigned: INTEGER | DECIMAL | DOUBLE;

numericLiteralPositive:
	INTEGER_POSITIVE
	| DECIMAL_POSITIVE
	| DOUBLE_POSITIVE;

numericLiteralNegative:
	INTEGER_NEGATIVE
	| DECIMAL_NEGATIVE
	| DOUBLE_NEGATIVE;

booleanLiteral: 'true' | 'false';

string_:
	STRING_LITERAL1
	| STRING_LITERAL2;

blankNode: BLANK_NODE_LABEL | ANON;

// LEXER RULES

SCHEMA_MORPHISM: (PN_CHARS)+;

SCHEMA_IDENTIFIER: (PN_CHARS)+;

BLANK_NODE_LABEL: '_:' PN_LOCAL;

VAR1: '?' VARNAME;

VAR2: '$' VARNAME;

INTEGER: DIGIT+;

DECIMAL: DIGIT+ '.' DIGIT* | '.' DIGIT+;

DOUBLE:
	DIGIT+ '.' DIGIT* EXPONENT
	| '.' DIGIT+ EXPONENT
	| DIGIT+ EXPONENT;

INTEGER_POSITIVE: '+' INTEGER;

DECIMAL_POSITIVE: '+' DECIMAL;

DOUBLE_POSITIVE: '+' DOUBLE;

INTEGER_NEGATIVE: '-' INTEGER;

DECIMAL_NEGATIVE: '-' DECIMAL;

DOUBLE_NEGATIVE: '-' DOUBLE;

EXPONENT: ('e' | 'E') ('+' | '-')? DIGIT+;

STRING_LITERAL1:
	'\'' (
		~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR
	)* '\'';

STRING_LITERAL2:
	'"' (
		~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR
	)* '"';

STRING_LITERAL_LONG1:
	'\'\'\'' (
		( '\'' | '\'\'')? (~('\'' | '\\') | ECHAR)
	)* '\'\'\'';

STRING_LITERAL_LONG2:
	'"""' (
		( '"' | '""')? ( ~('\'' | '\\') | ECHAR)
	)* '"""';

ECHAR: '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '"' | '\'');

NIL: '(' WS* ')';

ANON: '[' WS* ']';

PN_CHARS_U: PN_CHARS_BASE | '_';

VARNAME: (PN_CHARS_U | DIGIT) (
		PN_CHARS_U
		| DIGIT
		| '\u00B7'
		| ('\u0300' ..'\u036F')
		| ('\u203F' ..'\u2040')
	)*;

fragment PN_CHARS:
	PN_CHARS_U
	| '-'
	| DIGIT;

PN_PREFIX: PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?;

PN_LOCAL: ( PN_CHARS_U | DIGIT) ((PN_CHARS | '.')* PN_CHARS)?;

fragment PN_CHARS_BASE:
	'A' ..'Z'
	| 'a' ..'z'
	| '\u00C0' ..'\u00D6'
	| '\u00D8' ..'\u00F6'
	| '\u00F8' ..'\u02FF'
	| '\u0370' ..'\u037D'
	| '\u037F' ..'\u1FFF'
	| '\u200C' ..'\u200D'
	| '\u2070' ..'\u218F'
	| '\u2C00' ..'\u2FEF'
	| '\u3001' ..'\uD7FF'
	| '\uF900' ..'\uFDCF'
	| '\uFDF0' ..'\uFFFD';

fragment DIGIT: '0' ..'9';

WS: (' ' | '\t' | '\n' | '\r')+ -> skip;
