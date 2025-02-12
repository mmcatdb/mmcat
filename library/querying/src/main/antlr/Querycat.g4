grammar Querycat;

query: selectQuery EOF;
selectQuery: selectClause fromClause? whereClause solutionModifier;
subSelect: selectQuery;

selectClause: 'SELECT' selectGraphPattern;
selectGraphPattern: '{' selectTriples? '}';

fromClause: 'FROM' SCHEMA_IDENTIFIER;
whereClause: 'WHERE'? groupGraphPattern;

solutionModifier: orderClause? limitOffsetClauses?;

orderClause: 'ORDER' 'BY' orderCondition+;
orderCondition
    : (('ASC' | 'DESC') brackettedExpression)
    | (constraint | variable)
    ;

limitOffsetClauses: (limitClause offsetClause?) | (offsetClause limitClause?);
limitClause: 'LIMIT' INTEGER;
offsetClause: 'OFFSET' INTEGER;

groupGraphPattern:
    '{' (subSelect | (triplesBlock? (
        (graphPatternNotTriples | filter) '.'? triplesBlock?
    )*)) '}';

triplesBlock: triplesSameSubject ('.' triplesBlock?)?;

graphPatternNotTriples
    : optionalGraphPattern
    | groupOrUnionGraphPattern
    | inlineData
    ;
optionalGraphPattern: 'OPTIONAL' groupGraphPattern;
groupOrUnionGraphPattern: groupGraphPattern (('UNION' | 'MINUS') groupGraphPattern)*;
inlineData: 'VALUES' dataBlock;
dataBlock: variable '{' constant* '}';

filter: 'FILTER' constraint;
constraint: brackettedExpression;

selectTriples: triplesSameSubject ('.' selectTriples?)?;
triplesSameSubject: term propertyListNotEmpty;
propertyListNotEmpty: verb objectList (';' (verb objectList)?)*;

objectList: object (',' object)*;
object: graphNode;

verb: schemaMorphismOrPath;
schemaMorphismOrPath: pathAlternative;
pathAlternative: pathSequence ('|' pathSequence)*;
pathSequence: pathWithMod ('/' pathWithMod)*;
pathWithMod: pathPrimary pathMod?;
pathMod: '?' | '*' | '+';
pathPrimary: schemaMorphism | ('(' schemaMorphismOrPath ')') ;
schemaMorphism: primaryMorphism | dualMorphism;
primaryMorphism: SCHEMA_MORPHISM;
dualMorphism: '-' primaryMorphism;

graphNode: term ('AS' variable)?;
term: variable | constant | computation;

variable: VARIABLE;
constant
    : numericLiteral
    | booleanLiteral
    | string
    ;

computation
    : aggregation
    | 'CONCAT' '(' termList ')'
    ;
termList: term (',' term)*;

aggregation: aggregationFunction '(' (distinctModifier)? expression referenceArgument? ')';
distinctModifier: 'DISTINCT';
referenceArgument: ',' variable;
aggregationFunction
    : 'COUNT'
    | 'SUM'
    | 'AVG'
    | 'MIN'
    | 'MAX'
    ;

expression: conditionalOrExpression;
conditionalOrExpression: conditionalAndExpression ('||' conditionalAndExpression)*;
conditionalAndExpression: valueLogical ('&&' valueLogical)*;
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
primaryExpression: brackettedExpression | term;
brackettedExpression: '(' expression ')';

numericLiteral: numericLiteralUnsigned | numericLiteralPositive | numericLiteralNegative;
numericLiteralUnsigned: INTEGER | DECIMAL | DOUBLE;
numericLiteralPositive: INTEGER_POSITIVE | DECIMAL_POSITIVE | DOUBLE_POSITIVE;
numericLiteralNegative: INTEGER_NEGATIVE | DECIMAL_NEGATIVE | DOUBLE_NEGATIVE;
booleanLiteral: 'true' | 'false';
string: STRING_LITERAL_SINGLE | STRING_LITERAL_DOUBLE;

// LEXER RULES

SCHEMA_MORPHISM: (PN_CHARS)+;
SCHEMA_IDENTIFIER: (PN_CHARS)+;
VARIABLE: '?' VARNAME;
INTEGER: DIGIT+;
DECIMAL: DIGIT* '.' DIGIT+;
DOUBLE
    : (DIGIT+ '.' DIGIT* EXPONENT)
    | ('.' DIGIT+ EXPONENT)
    | (DIGIT+ EXPONENT)
    ;

INTEGER_POSITIVE: '+' INTEGER;
DECIMAL_POSITIVE: '+' DECIMAL;
DOUBLE_POSITIVE: '+' DOUBLE;
INTEGER_NEGATIVE: '-' INTEGER;
DECIMAL_NEGATIVE: '-' DECIMAL;

DOUBLE_NEGATIVE: '-' DOUBLE;

EXPONENT: ('e' | 'E') ('+' | '-')? DIGIT+;

// \u0027 - single quote: '
// \u0022 - double quote: "
// \u005C - backslash: \
// \u000A - linefeed: \n
// \u000D - carriage return: \r

STRING_LITERAL_SINGLE: '\'' (~['\\\n\r] | ESCAPE_CHAR)* '\'';
STRING_LITERAL_DOUBLE: '"' (~["\\\n\r] | ESCAPE_CHAR)* '"';
ESCAPE_CHAR: '\\' ['"tbnrf\\];

VARNAME: (PN_CHARS_U | DIGIT) (PN_CHARS_U | DIGIT)*;

fragment PN_CHARS
    : PN_CHARS_U
    | '-'
    | DIGIT
    ;

PN_CHARS_U: PN_CHARS_BASE | '_';

fragment PN_CHARS_BASE
    : [A-Z]
    | [a-z]
    | [\u00C0-\u00D6]
    | [\u00D8-\u00F6]
    | [\u00F8-\u02FF]
    | [\u0370-\u037D]
    | [\u037F-\u1FFF]
    | [\u200C-\u200D]
    | [\u2070-\u218F]
    | [\u2C00-\u2FEF]
    | [\u3001-\uD7FF]
    | [\uF900-\uFDCF]
    | [\uFDF0-\uFFFD]
    ;

fragment DIGIT: [0-9];

WS: [ \t\n\r]+ -> skip;
COMMENT: '#' ~[\n\r]* -> skip;
