import { DatasourceType } from '../datasource/Datasource';

export enum Operator {
    Equal = 'Equal',
    NotEqual = 'NotEqual',
    LessOrEqual = 'LessOrEqual',
    GreaterOrEqual = 'GreaterOrEqual',
    Less = 'Less',
    Greater = 'Greater'
}

type OperatorLabels = Partial<Record<Operator, string>>;

const POSTGRESQL_OPERATOR: OperatorLabels = {
    [Operator.Equal]: '=',
    [Operator.NotEqual]: '<>',
    [Operator.LessOrEqual]: '<=',
    [Operator.GreaterOrEqual]: '>=',
    [Operator.Less]: '<',
    [Operator.Greater]: '>',
};

const MONGODB_OPERATOR: OperatorLabels = {
    [Operator.Equal]: '=',
    [Operator.NotEqual]: '<>',
    [Operator.LessOrEqual]: '<=',
    [Operator.GreaterOrEqual]: '>=',
    [Operator.Less]: '<',
    [Operator.Greater]: '>',
};

const NEO4J_OPERATOR: OperatorLabels = {
    [Operator.Equal]: '=',
    [Operator.NotEqual]: '<>',
    [Operator.LessOrEqual]: '<=',
    [Operator.GreaterOrEqual]: '>=',
    [Operator.Less]: '<',
    [Operator.Greater]: '>',
};

export const OPERATOR_MAPPING: Partial<Record<DatasourceType, OperatorLabels>> = {
    [DatasourceType.postgresql]: POSTGRESQL_OPERATOR,
    [DatasourceType.mongodb]: MONGODB_OPERATOR,
    [DatasourceType.neo4j]: NEO4J_OPERATOR,
};
