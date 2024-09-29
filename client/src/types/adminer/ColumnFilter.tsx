export type ColumnFilter = {
    id: number;
    columnName: string;
    operator : Operator;
    columnValue: string;
};

export enum Operator {
    eq = '=',
    neq = '<>',
    lte = '<=',
    gte = '>=',
    lt = '<',
    gt = '>'
}
