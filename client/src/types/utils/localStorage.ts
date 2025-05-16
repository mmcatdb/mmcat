import { useCallback, useEffect, useState } from 'react';

function parse<T>(value: string | null): T | null {
    return value === null ? null : JSON.parse(value);
}

export function get<T>(key: string): T | null {
    const value = window.localStorage.getItem(key);
    return parse<T>(value);
}

export function set(key: string, value: unknown) {
    /**
     * The storage event is only fired when a window other than itself modifies the storage area.
     * So we need to simulate the storage events for the current window.
    */
    const oldValue = window.localStorage.getItem(key);
    window.localStorage.setItem(key, JSON.stringify(value));
    const newValue = window.localStorage.getItem(key);

    listeners.forEach(listener => listener({ key, newValue, oldValue }));
}

function remove(key: string) {
    window.localStorage.removeItem(key);
}

function clear() {
    window.localStorage.clear();
}

type EventListener<T> = (newValue: T | null, oldValue: T | null) => void;

type InnerStorageEvent = {
    readonly key: string | null;
    readonly newValue: string | null;
    readonly oldValue: string | null;
}

type InnerEventListener = (event: InnerStorageEvent) => void;

const listeners = new Map as Map<EventListener<any>, InnerEventListener>;

/**
 * Adds event listener for the storage event for the given key.
 * Unlike the original storage event, this event listener is fired even on the tab that modified the storage!
 */
function addEventListener<T>(key: string, listener: EventListener<T>) {
    const innerListener = (event: InnerStorageEvent) => {
        if (event.key !== key)
            return;

        const newValue = parse<T>(event.newValue);
        const oldValue = parse<T>(event.newValue);

        listener(newValue, oldValue);
    };

    listeners.set(listener, innerListener);
    window.addEventListener('storage', innerListener);
}

function removeEventListener<T>(listener: EventListener<T>) {
    const innerListener = listeners.get(listener);
    if (!innerListener)
        return;

    listeners.delete(listener);
    window.removeEventListener('storage', innerListener);
}

type LocalStorage = {
    get: <T>(key: string) => T | null;
    set: (key: string, value: unknown) => void;
    remove: (key: string) => void;
    clear: () => void;
    addEventListener: <T>(key: string, listener: EventListener<T>) => void;
    removeEventListener: <T>(listener: EventListener<T>) => void;
};

/* eslint-disable @typescript-eslint/no-empty-function */
export const localStorage: LocalStorage = window.localStorage ? {
    get,
    set,
    remove,
    clear,
    addEventListener,
    removeEventListener,
} : {
    get: () => null,
    set: () => {},
    remove: () => {},
    clear: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
};

// export default localStorage;

/**
 * Reactive local storage hook. This can be also used for instant communication between tabs.
 * TODO: Add support for default value.
 */
export function useLocalStorageState<T>(key: string): [ T | undefined, (value?: T) => void ] {
    const [ value, setValue ] = useState<T | undefined>(() => {
        const storedValue = localStorage.get<T>(key);
        return storedValue ?? undefined;
    });

    useEffect(() => {
        const listener = (newValue: T | null) => {
            setValue(newValue ?? undefined);
        };
        addEventListener(key, listener);
        
        return () => removeEventListener(listener);
    }, [ key ]);

    const setValueToStorage = useCallback((newValue: T | undefined) => {
        localStorage.set(key, newValue ?? null);
    }, [ key ]);

    return [ value, setValueToStorage ];
}
