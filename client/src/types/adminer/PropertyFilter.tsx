export type PropertyFilter = {
    id: number;
    propertyName: string;
    operator : Operator;
    propertyValue: string;
};

export enum Operator {
    eq = '=',
    neq = '<>',
    lte = '<=',
    gte = '>=',
    lt = '<',
    gt = '>'
}
