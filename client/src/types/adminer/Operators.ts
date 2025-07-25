import { DatasourceType } from '../Datasource';

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

export const getNeo4jOperators = (propertyName: string): OperatorLabels =>
    propertyName.startsWith('#') && propertyName.endsWith(' - SIZE') ? COMMON_OPERATORS : NEO4J_OPERATOR;

export const OPERATOR_MAPPING: Partial<Record<DatasourceType, (propertyName: string) => OperatorLabels>> = {
    [DatasourceType.postgresql]: () => POSTGRESQL_OPERATOR,
    [DatasourceType.mongodb]: () => MONGODB_OPERATOR,
    [DatasourceType.neo4j]: getNeo4jOperators,
};

export const UNARY_OPERATORS: string[] = [ Operator.IsNull, Operator.IsNotNull ];
