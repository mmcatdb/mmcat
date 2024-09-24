// This is kosher because this is the one use case in which the {} type actually means something.
// eslint-disable-next-line @typescript-eslint/ban-types
export type EmptyIntersection = {};

export async function sleep(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// eslint-disable-next-line @typescript-eslint/ban-types
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
