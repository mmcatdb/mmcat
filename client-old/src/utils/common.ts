// This is kosher because this is the one use case in which the {} type actually means something.
// eslint-disable-next-line @typescript-eslint/ban-types
export type EmptyIntersection = {};

export async function sleep(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// eslint-disable-next-line @typescript-eslint/ban-types
export type Optional<T extends Record<string, unknown>> = T | {};
