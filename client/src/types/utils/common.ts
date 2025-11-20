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

export function prettyPrintNumber(value: number): string {
    if (value < 1000)
        return value.toString();

    return value.toExponential(2);
}

export type Quantity<TUnit extends string = string> = QuantityClass<TUnit>;

class QuantityClass<TUnit extends string = string> {
    constructor(
        private readonly units: readonly TUnit[],
        private readonly thresholds: readonly number[],
        private readonly isBaseInteger: boolean,
    ) {}

    defineUnits(from?: TUnit, to?: TUnit): TUnit[] {
        const fromIndex = from !== undefined ? this.units.indexOf(from) : 0;
        const toIndex = to !== undefined ? this.units.indexOf(to) : this.units.length;
        return this.units.slice(fromIndex, toIndex);
    }

    prettyPrint(bytes: number, unit?: TUnit, isInteger?: boolean): string {
        let value: number | undefined;
        if (!unit)
            ({ value, unit } = this.findUnit(bytes));
        else
            value = this.fromBase(bytes, unit);

        // We don't want to show decimal places for integers.
        const omitDecimal = (isInteger ?? this.isBaseInteger) && unit === this.units[0];
        const numberPart = (omitDecimal ? String(value) : value.toFixed(2));
        return `${numberPart} ${unit}`;
    }

    findUnit(valueInBase: number): { value: number, unit: TUnit } {
        let index = 0;
        let value = valueInBase;

        while (value >= this.thresholds[index] && index < this.units.length - 1) {
            value /= this.thresholds[index];
            index++;
        }

        return { value, unit: this.units[index] };
    }

    fromBase(valueInBase: number, toUnit: TUnit): number {
        let value = valueInBase;
        for (let i = 0; i < this.units.length; i++) {
            if (toUnit === this.units[i])
                return value;

            value /= this.thresholds[i];
        }
        throw new Error('Impossibruh');
    }

    toBase(value: number, fromUnit: TUnit): number {
        let baseValue = value;
        for (let i = this.units.indexOf(fromUnit); i > 0; i--)
            baseValue *= this.thresholds[i - 1];
        return baseValue;
    }
}

export type DataSizeUnit = typeof dataSizeUnits[number];
const dataSizeUnits = [ 'B', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB' ] as const;

export const dataSizeQuantity = new QuantityClass(
    dataSizeUnits,
    [ 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024 ],
    true,
);

export type TimeUnit = typeof timeUnits[number];
const timeUnits = [ 'ms', 's', 'min', 'h', 'd', 'y' ] as const;

export const timeQuantity = new QuantityClass(
    timeUnits,
    [ 1000, 60, 60, 24, 365 ],
    false,
);
