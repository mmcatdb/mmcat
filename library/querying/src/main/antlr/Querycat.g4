grammar Querycat;

query: selectQuery EOF;
selectQuery: selectClause whereClause solutionModifier;
subSelect: selectQuery;

selectClause: 'SELECT' '{' selectTriples? '}';
// The og sparql grammar allows omiting the WHERE keyword, but that ain't happening here.
whereClause: 'WHERE' graphPattern;
solutionModifier: orderClause? limitOffsetClauses?;

orderClause: 'ORDER' 'BY' orderCondition+;
orderCondition
    : (('ASC' | 'DESC') brackettedExpression)
    | (constraint | variable)
    ;
limitOffsetClauses: (limitClause offsetClause?) | (offsetClause limitClause?);
limitClause: 'LIMIT' INTEGER;
offsetClause: 'OFFSET' INTEGER;

graphPattern: '{' (subSelect | graphPatternInner) '}';
graphPatternInner: triplesBlock? (nonTriples triplesBlock?)*;
nonTriples
    : unionGraphPattern
    | optionalGraphPattern
    | filter
    | inlineValues
    ;
unionGraphPattern: graphPattern (('UNION' | 'MINUS') graphPattern)*;
optionalGraphPattern: 'OPTIONAL' graphPattern;
filter: 'FILTER' constraint;
constraint: brackettedExpression;
inlineValues: 'VALUES' variable '{' constant* '}';

selectTriples: triplesSameSubject ('.' selectTriples?)?;
triplesBlock: triplesSameSubject ('.' triplesBlock?)?;
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

// The and operator is evaluated first, then or, then the comparison operators.
// The reason is that and is like multiplication while or is like addition.
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
