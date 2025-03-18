import { DatasourceType } from '../datasource/Datasource';

export enum Operator {
    Equal = 'Equal',
    NotEqual = 'NotEqual',
    LessOrEqual = 'LessOrEqual',
    GreaterOrEqual = 'GreaterOrEqual',
    Less = 'Less',
    Greater = 'Greater',
    IsNull = 'IsNull',
    IsNotNull = 'IsNotNull',
    Like = 'Like',
    ILike = 'ILike',
    NotLike = 'NotLike',
    MatchRegEx = 'MatchRegEx',
    NotMatchRegEx = 'NotMatchRegEx',
    In = 'In',
    NotIn = 'NotIn',
    StartsWith = 'StartsWith',
    EndsWith = 'EndsWith',
    Contains = 'Contains',
    Size = 'Size'
}

export type OperatorLabels = Partial<Record<Operator, string>>;

const COMMON_OPERATORS: OperatorLabels = {
    [Operator.Equal]: '=',
    [Operator.NotEqual]: '<>',
    [Operator.LessOrEqual]: '<=',
    [Operator.GreaterOrEqual]: '>=',
    [Operator.Less]: '<',
    [Operator.Greater]: '>',
    [Operator.In]: 'IN',
};

const POSTGRESQL_OPERATOR: OperatorLabels = {
    ...COMMON_OPERATORS,
    [Operator.NotIn]: 'NOT IN',
    [Operator.IsNull]: 'IS NULL',
    [Operator.IsNotNull]: 'IS NOT NULL',
    [Operator.Like]: 'LIKE',
    [Operator.ILike]: 'ILIKE',
    [Operator.NotLike]: 'NOT LIKE',
    [Operator.MatchRegEx]: '~',
    [Operator.NotMatchRegEx]: '!~',
};

const MONGODB_OPERATOR: OperatorLabels = {
    ...COMMON_OPERATORS,
    [Operator.NotIn]: 'NOT IN',
    [Operator.MatchRegEx]: 'Match RegEx',
};

const NEO4J_OPERATOR: OperatorLabels = {
    ...COMMON_OPERATORS,
    [Operator.IsNull]: 'IS NULL',
    [Operator.IsNotNull]: 'IS NOT NULL',
    [Operator.StartsWith]: 'STARTS WITH',
    [Operator.EndsWith]: 'ENDS WITH',
    [Operator.Contains]: 'CONTAINS',
    [Operator.MatchRegEx]: '=~',
};

const LABEL_QUANTIFIERS = [ 'ANY', 'ALL', 'NONE', 'SINGLE' ];

const NEO4J_LABEL_OPERATOR: OperatorLabels = Object.fromEntries(
    Object.entries(COMMON_OPERATORS).map(([ key, value ]) =>
        [ `${Operator.Size},${key}`, `SIZE, ${value}` ],
    ).concat(Object.entries(NEO4J_OPERATOR).flatMap(([ key, value ]) =>
        LABEL_QUANTIFIERS.map(quantifier => [ `${quantifier},${key}`, `${quantifier}, ${value}` ]),
    )),
) as OperatorLabels;

export const getNeo4jOperators = (propertyName: string): OperatorLabels =>
    propertyName === '#labels' ? NEO4J_LABEL_OPERATOR : NEO4J_OPERATOR;

export const OPERATOR_MAPPING: Partial<Record<DatasourceType, (propertyName: string) => OperatorLabels>> = {
    [DatasourceType.postgresql]: () => POSTGRESQL_OPERATOR,
    [DatasourceType.mongodb]: () => MONGODB_OPERATOR,
    [DatasourceType.neo4j]: getNeo4jOperators,
};

const BASIC_UNARY_OPERATORS: Operator[] = [ Operator.IsNull, Operator.IsNotNull ];

export const UNARY_OPERATORS: string[] = [
    ...BASIC_UNARY_OPERATORS,
    ...BASIC_UNARY_OPERATORS.flatMap(operator =>
        LABEL_QUANTIFIERS.map(quantifier => `${quantifier},${operator}`),
    ),
];
