export class TwoWayMap<Key, Value> implements Map<Key, Value> {
    _map: Map<Key, Value> = new Map();
    _reverseMap: Map<Value, Key> = new Map();

    clear(): void {
        this._map.clear();
        this._reverseMap.clear();
    }

    delete(key: Key): boolean {
        const value = this._map.get(key);
        if (value === undefined)
            return false;

        const result = this._map.delete(key);
        return this._reverseMap.delete(value) && result;
    }

    deleteValue(value: Value) {
        const key = this._reverseMap.get(value);
        if (key === undefined)
            return false;

        const result = this._reverseMap.delete(value);
        return this._map.delete(key) && result;
    }

    has(key: Key): boolean {
        return this._map.has(key);
    }

    hasValue(value: Value): boolean {
        return this._reverseMap.has(value);
    }

    get(key: Key): Value | undefined {
        return this._map.get(key);
    }

    getKey(value: Value): Key | undefined {
        return this._reverseMap.get(value);
    }

    set(key: Key, value: Value): this {
        this._map.set(key, value);
        this._reverseMap.set(value, key);

        return this;
    }

    get size(): number {
        return this._map.size;
    }

    entries(): IterableIterator<[Key, Value]> {
        return this._map.entries();
    }

    forEach(callbackfn: (value: Value, key: Key, map: Map<Key, Value>) => void): void {
        this._map.forEach(callbackfn);
    }

    keys(): IterableIterator<Key> {
        return this._map.keys();
    }

    values(): IterableIterator<Value> {
        return this._map.values();
    }

    [Symbol.iterator](): IterableIterator<[Key, Value]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'TwoWayMap';
}
