type Injection<Key, KeyId> = (key: Key) => KeyId;

export class ComparableMap<Key, KeyId, Value> {
    _keyToIdFunction: Injection<Key, KeyId>;
    _map = new Map() as Map<KeyId, Value>;

    public constructor(keyToIdFunction: Injection<Key, KeyId>) {
        this._keyToIdFunction = keyToIdFunction;
    }

    clear(): void {
        this._map.clear();
    }

    delete(key: Key): boolean {
        return this._map.delete(this._keyToIdFunction(key));
    }

    has(key: Key): boolean {
        return this._map.has(this._keyToIdFunction(key));
    }

    get(key: Key): Value | undefined {
        return this._map.get(this._keyToIdFunction(key));
    }

    set(key: Key, value: Value): this {
        this._map.set(this._keyToIdFunction(key), value);
        return this;
    }

    get size(): number {
        return this._map.size;
    }

    entries(): IterableIterator<[KeyId, Value]> {
        return this._map.entries();
    }

    keyIds(): IterableIterator<KeyId> {
        return this._map.keys();
    }

    values(): IterableIterator<Value> {
        return this._map.values();
    }
}
