export enum Key {
    Shift = 'shift',
    Ctrl = 'ctrl',
    Alt = 'alt',
}

const keys = {
    [Key.Shift]: false,
    [Key.Ctrl]: false,
    [Key.Alt]: false,
};

export function startCapturingKeys() {
    document.addEventListener('keydown', event => {
        keys.shift = event.shiftKey;
        keys.ctrl = event.ctrlKey;
        keys.alt = event.altKey;
    });

    document.addEventListener('keyup', event => {
        keys.shift = event.shiftKey;
        keys.ctrl = event.ctrlKey;
        keys.alt = event.altKey;
    });
}

export function isKeyPressed(key: Key): boolean {
    return keys[key];
}
