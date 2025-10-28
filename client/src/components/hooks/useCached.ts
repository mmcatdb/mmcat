import { type EmptyObject } from '@/types/utils/common';
import { useRef, useState } from 'react';

/**
 * Caches last non-undefined value for modals and other components with transition that rely on it.
 */
export function useCached<T>(value: T | undefined): T | undefined {
    // The useState hook is not applicable here because it would force re-render event if the new value didn't change.
    const lastRef = useRef(value);

    // We do want to provide the current value first and only fallback to the cached one if the current is not defined.
    if (value !== undefined) {
        lastRef.current = value;
        return value;
    }

    return lastRef.current;
}

/**
 * Caches last non-undefined value for modals and other components with transition that rely on it.
 * The cached value will be used only for the specified time (counted from the moment the value becomes undefined).
 */
export function useCachedWithTimeout<T>(value: T | undefined, timeout: number): T | undefined {
    const lastValidRef = useRef(value);
    const timeoutRef = useRef<NodeJS.Timeout>(undefined);
    const [ , updateState ] = useState<EmptyObject>();

    if (value !== undefined) {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
            timeoutRef.current = undefined;
        }

        lastValidRef.current = value;
        return value;
    }

    // If there is no cached value, we can't return anything. There's also no point in setting a timeout to clear it. And there can't be a timeout running at this point.
    if (lastValidRef.current === undefined)
        return undefined;

    // If the timeout isn't defined, we set it now.
    // Otherwise, we don't want to clear it - at that point, the value didn't change (it had to be undefined in the previous render to set/keep the timeout, and it has to be undefined now to reach this point again).
    // So we just continue counting down.
    if (!timeoutRef.current) {
        timeoutRef.current = setTimeout(() => {
            lastValidRef.current = undefined;
            timeoutRef.current = undefined;
            // There is no need to use a cleanup function (via useEffect) here because setting state on an unmounted component is just a no-op. And we already clear the timeout when a new value is provided.
            updateState({});
        }, timeout);
    }

    return lastValidRef.current;
}
