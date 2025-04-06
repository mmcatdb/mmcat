import { type EmptyObject } from '@/types/utils/common';
import { useEffect, useRef, useState } from 'react';

/**
 * Caches last non-undefined value for modals and other components with transition that rely on it.
 */
export function useCached<T>(value: T | undefined): T | undefined {
    // The useState hook is not applicable here because it would force re-render event if the new value didn't change.
    const lastRef = useRef(value);

    useEffect(() => {
        if (value !== undefined)
            lastRef.current = value;

    }, [ value ]);

    // We do want to provide the current value first and only fallback to the cached one if the current is not defined.
    return value ?? lastRef.current;
}

/**
 * Caches last non-undefined value for modals and other components with transition that rely on it.
 * The cached value will be used only for the specified time.
 */
export function useCachedWithTimeout<T>(value: T | undefined, timeout: number): T | undefined {
    const lastRef = useRef(value);
    const timeoutRef = useRef<NodeJS.Timeout>();
    const [ , updateState ] = useState<EmptyObject>();

    useEffect(() => {
        if (value !== undefined) {
            lastRef.current = value;
            clearTimeout(timeoutRef.current);
        }
        else {
            if (timeoutRef.current)
                clearTimeout(timeoutRef.current);

            timeoutRef.current = setTimeout(() => {
                lastRef.current = undefined;
                updateState({});
            }, timeout);
        }

        return () => {
            clearTimeout(timeoutRef.current);
            timeoutRef.current = undefined;
        };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ value ]);

    return value ?? lastRef.current;
}
