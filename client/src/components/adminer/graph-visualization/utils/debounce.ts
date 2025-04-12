/**
 * Creates a debounced function that delays invoking func until after wait milliseconds have elapsed since the last time the debounced function was invoked. The debounced function comes with a cancel method to cancel delayed invocations and a flush method to immediately invoke them. Subsequent calls to the debounced function return the result of the last func invocation.
 *
 * @param func The function to debounce.
 * @param wait The number of milliseconds to delay.
 * @return Returns the new debounced function.
 */
export function debounce<T extends(...args: Parameters<T>) => void>(func: T, wait: number) {
    let lastArgs: Parameters<T> | undefined;
    let timerId: NodeJS.Timeout | undefined;
    let lastCallTime: number | undefined;

    function timerExpired() {
        const time = Date.now();
        const remaining = lastCallTime ? (lastCallTime + wait - time) : 0;
        if (remaining <= 0) {
            runFunction();
            return;
        }

        // Restart the timer.
        timerId = setTimeout(timerExpired, remaining);
    }

    function runFunction() {
        timerId = undefined;

        const args = lastArgs;
        lastArgs = undefined;

        // Only invoke if we have `lastArgs` which means `func` has been debounced at least once.
        if (args)
            func(...(args as Parameters<T>));
    }

    return (...args: Parameters<T>) => {
        lastArgs = args;
        lastCallTime = Date.now();

        if (!timerId)
            timerId = setTimeout(timerExpired, wait);
    };
}
