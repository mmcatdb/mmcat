import { type Dispatch, type SetStateAction, useCallback, useEffect, useRef, useState } from 'react';

function parse<T>(value: string | null): T | null {
    return value === null ? null : JSON.parse(value);
}

function get<T>(key: string): T | null {
    const value = window.localStorage.getItem(key);
    return parse<T>(value);
}

function set(key: string, value: unknown) {
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
};

type InnerEventListener = (event: InnerStorageEvent) => void;

const listeners = new Map<EventListener<unknown>, InnerEventListener>();

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

    listeners.set(listener as EventListener<unknown>, innerListener);
    window.addEventListener('storage', innerListener);
}

function removeEventListener<T>(listener: EventListener<T>) {
    const innerListener = listeners.get(listener as EventListener<unknown>);
    if (!innerListener)
        return;

    listeners.delete(listener as EventListener<unknown>);
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

/**
 * Reactive one-way local storage hook. All changes to state are propagated to the local storage, but changes in local storage are not propagated back to the state.
 * The initial value is taken from the local storage (if not provided).
 * The defualtState is used whenever the local storage is empty. Like in useState, only its first value is ever used.
 */
export function useLocalStorageBackup<T>(key: string, defaultState: T): [ T, Dispatch<SetStateAction<T>> ];

export function useLocalStorageBackup<T>(key: string): [ T | undefined, Dispatch<SetStateAction<T | undefined>> ];

export function useLocalStorageBackup<T>(key: string, defaultState?: T): [ T | undefined, Dispatch<SetStateAction<T | undefined>> ] {
    const defaultRef = useRef(defaultState);

    const [ value, setValue ] = useState<T | undefined>(() => {
        const storedValue = localStorage.get<T>(key);
        return storedValue ?? defaultRef.current;
    });

    const setValueToStorage = useCallback((action: SetStateAction<T | undefined>) => {
        setValue(action);

        if (typeof action === 'function') {
            const functionAction = action as (prevState: T | undefined) => T | undefined;
            localStorage.set(key, functionAction(localStorage.get(key) ?? defaultRef.current) ?? null);
        }
        else {
            localStorage.set(key, action ?? null);
        }
    }, [ key ]);

    return [ value, setValueToStorage ];
}

/**
 * Reactive local storage hook. All changes to state are propagated to the local storage and vice versa.
 * The initial value is taken from the local storage (if not provided).
 * The defualtState is used whenever the local storage is empty. Like in useState, only its first value is ever used.
 * This can be also used for instant communication between tabs.
 */
export function useLocalStorageState<T>(key: string, defaultState: T): [ T, Dispatch<SetStateAction<T>> ];

export function useLocalStorageState<T>(key: string): [ T | undefined, Dispatch<SetStateAction<T | undefined>> ];

export function useLocalStorageState<T>(key: string, defaultState?: T): [ T | undefined, Dispatch<SetStateAction<T | undefined>> ] {
    const defaultRef = useRef(defaultState);

    const [ value, setValue ] = useState<T | undefined>(() => {
        const storedValue = localStorage.get<T>(key);
        return storedValue ?? defaultRef.current;
    });

    useEffect(() => {
        const listener = (newValue: T | null) => {
            setValue(newValue ?? defaultRef.current);
        };
        addEventListener(key, listener);

        return () => removeEventListener(listener);
    }, [ key ]);

    const setValueToStorage = useCallback((action: SetStateAction<T | undefined>) => {
        if (typeof action === 'function') {
            const functionAction = action as (prevState: T | undefined) => T | undefined;
            localStorage.set(key, functionAction(localStorage.get(key) ?? defaultRef.current) ?? null);
        }
        else {
            localStorage.set(key, action ?? null);
        }
    }, [ key ]);

    return [ value, setValueToStorage ];
}
