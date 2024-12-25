export class TwoWayMap<Key, Value> implements Map<Key, Value> {
    private readonly map = new Map<Key, Value>();
    private readonly reverseMap = new Map<Value, Key>();

    clear(): void {
        this.map.clear();
        this.reverseMap.clear();
    }

    delete(key: Key): boolean {
        const value = this.map.get(key);
        if (value === undefined)
            return false;

        const result = this.map.delete(key);
        return this.reverseMap.delete(value) && result;
    }

    deleteValue(value: Value) {
        const key = this.reverseMap.get(value);
        if (key === undefined)
            return false;

        const result = this.reverseMap.delete(value);
        return this.map.delete(key) && result;
    }

    has(key: Key): boolean {
        return this.map.has(key);
    }

    hasValue(value: Value): boolean {
        return this.reverseMap.has(value);
    }

    get(key: Key): Value | undefined {
        return this.map.get(key);
    }

    getKey(value: Value): Key | undefined {
        return this.reverseMap.get(value);
    }

    set(key: Key, value: Value): this {
        this.map.set(key, value);
        this.reverseMap.set(value, key);

        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): MapIterator<[Key, Value]> {
        return this.map.entries();
    }

    forEach(callbackfn: (value: Value, key: Key, map: Map<Key, Value>) => void): void {
        this.map.forEach(callbackfn);
    }

    keys(): MapIterator<Key> {
        return this.map.keys();
    }

    values(): MapIterator<Value> {
        return this.map.values();
    }

    [Symbol.iterator](): MapIterator<[Key, Value]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'TwoWayMap';
}
