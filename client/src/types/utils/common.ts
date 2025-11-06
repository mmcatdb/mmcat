// This is kosher because this is the one use case in which the {} type actually means something.

import { type Position } from '@/components/graph/graphUtils';

// This is kosher because this is the one use case in which the {} type actually means something.
// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export type EmptyIntersection = {};
export type EmptyObject = Record<string, never>;
export function emptyFunction() {
    // This function is intentionally empty.
}

export async function sleep(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export type Optional<T extends Record<string, unknown>> = T | {};

export type DeepPartial<T> = {
    [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

declare const secretType: unique symbol;

/**
 * An instance of SpecialType<T> is assignable to T.
 * An instance of T is NOT assignable to SpecialType<T>.
 */
export type SpecialType<T, S extends string> = T & { [secretType]: S };

/**
 * An instance of UniqueType<T> is NOT assignable to T.
 * An instance of T is NOT assignable to UniqueType<T>.
 */
export type UniqueType<T, S extends string> = Omit<T, typeof secretType> & { [secretType]: S };

export function isArrayEqual<TType>(a: TType[], b: TType[], isEqual?: (a: TType, b: TType) => boolean): boolean {
    const comparator = isEqual ?? ((a, b) => a === b);
    return a.length === b.length && a.every((value, index) => comparator(value, b[index]));
}

export function deepClone<T extends object & { [Symbol.iterator]?: never }>(o: T): T {
    const output: T = {} as T;
    for (const key in o)
        output[key] = deepCloneValue(o[key]);

    return output;
}

function deepCloneValue<T>(a: T): T {
    if (a === null || a === undefined)
        return a;

    if (typeof a === 'object') {
        if (Array.isArray(a))
            return a.map(deepCloneValue) as T;

        return deepClone(a);
    }

    return a;
}

export function toFormNumber(value: unknown): number | '' | '-' {
    if (typeof value === 'number')
        return value;

    if (typeof value === 'string') {
        if (value === '-')
            return '-';

        const number = Number.parseFloat(value);
        if (!Number.isNaN(number) && Number.isFinite(number))
            return number;
    }

    return '';
}

export type FormNumber = number | '' | '-';
export function toNumber(value: FormNumber): number {
    return (value === '' || value ==='-') ? 0 : value;
}

export type FormPosition = { x: FormNumber, y: FormNumber };
export function toPosition(value: FormPosition): Position {
    return { x: toNumber(value.x), y: toNumber(value.y) };
}

/** Comparison of only ascii-like strings. */
export function compareStringsAscii(a: string, b: string): number {
    return a < b ? -1 : (a > b ? 1 : 0);
}

/** Changes the first character of given string to upper case. */
export function capitalize(word: string) {
    return word.charAt(0).toUpperCase() + word.slice(1);
}
