function set(key: string, value: string): void {
    document.cookie = `${key}=${value}; path=/`;
}

function get(key: string): string | undefined {
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
        const [ cookieKey, cookieValue ] = cookie.split('=');
        if (cookieKey.trim() === key) 
            return cookieValue;
    }
}

function remove(key: string): void {
    document.cookie = `${key}=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT`;
}

const cookies = {
    set,
    get,
    remove,
};

export default cookies;

// TODO there is no single api for listening on cookie changes that works for all browsers.
// Therefore, a cross-tab synchronization can't be done in this way. However, we can always use the local storage for this purpose.
