// This is kosher because this is the one use case in which the {} type actually means something.
// eslint-disable-next-line @typescript-eslint/ban-types
export type EmptyIntersection = {};

export async function sleep(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// eslint-disable-next-line @typescript-eslint/ban-types
export type Optional<T extends Record<string, unknown>> = T | {};

export function isArrayEqual<TType>(a: TType[], b: TType[], isEqual?: (a: TType, b: TType) => boolean): boolean {
    const comparator = isEqual ?? ((a, b) => a === b);
    return a.length === b.length && a.every((value, index) => comparator(value, b[index]));
}
