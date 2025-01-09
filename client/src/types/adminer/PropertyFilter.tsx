import type { Operator } from './Operators';

export type PropertyFilter = {
    id: number;
    propertyName: string;
    operator : Operator;
    propertyValue: string;
};
