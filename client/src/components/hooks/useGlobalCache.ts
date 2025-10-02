const cache = new Map<string, { value: unknown, timeout: NodeJS.Timeout | undefined }>();

const CACHE_TIMEOUT = 60 * 1000; // 1 minute

/**
 * A global cache. Anyone can put anything here. It will be removed after some time.
 * Use this for passing large data between pages/components.
 */
export const globalCache = {
    set,
    get,
};

function set<T>(key: string, value: T | undefined): void {
    const prev = cache.get(key);
    if (prev)
        clearTimeout(prev.timeout);

    if (value === undefined) {
        cache.delete(key);
        return;
    }

    const timeout = setTimeout(() => {
        cache.delete(key);
    }, CACHE_TIMEOUT);

    cache.set(key, { value, timeout });
}

function get<T>(key: string): T | undefined {
    const entry = cache.get(key);
    return entry?.value as T | undefined;
}
