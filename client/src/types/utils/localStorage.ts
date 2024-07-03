function parse<T>(value: string | null): T | null {
    return value === null ? null : JSON.parse(value) as T;
}

function get<T>(key: string): T | null {
    const value = window.localStorage.getItem(key);
    return parse<T>(value);
}

function set(key: string, value: unknown) {
    window.localStorage.setItem(key, JSON.stringify(value));
}

function remove(key: string) {
    window.localStorage.removeItem(key);
}

function clear() {
    window.localStorage.clear();
}

type LocalStorage = {
    get: <T>(key: string) => T | null;
    set: (key: string, value: unknown) => void;
    remove: (key: string) => void;
    clear: () => void;
};

/* eslint-disable @typescript-eslint/no-empty-function */
export const localStorage: LocalStorage = window.localStorage ? {
    get,
    set,
    remove,
    clear,
} : {
    get: () => null,
    set: () => {},
    remove: () => {},
    clear: () => {},
};
