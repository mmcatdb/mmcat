type Injection<Input, Output> = (input: Input) => Output;

export class TwoWayComparableMap<Key, KeyId, Value, ValueId> {
    _keyToIdFunction: Injection<Key, KeyId>;
    _valueToIdFunction: Injection<Value, ValueId>;
    _map = new Map() as Map<KeyId, Value>;
    _reverseMap = new Map() as Map<ValueId, Key>;

    public constructor(keyToIdFunction: Injection<Key, KeyId>, valueToIdFunction: Injection<Value, ValueId>) {
        this._keyToIdFunction = keyToIdFunction;
        this._valueToIdFunction = valueToIdFunction;
    }

    clear(): void {
        this._map.clear();
        this._reverseMap.clear();
    }

    delete(key: Key): boolean {
        const keyId = this._keyToIdFunction(key);
        const value = this._map.get(keyId);
        if (value === undefined)
            return false;

        const result = this._map.delete(keyId);
        return this._reverseMap.delete(this._valueToIdFunction(value)) && result;
    }

    deleteValue(value: Value) {
        const valueId = this._valueToIdFunction(value);
        const key = this._reverseMap.get(valueId);
        if (key === undefined)
            return false;

        const result = this._reverseMap.delete(valueId);
        return this._map.delete(this._keyToIdFunction(key)) && result;
    }

    has(key: Key): boolean {
        return this._map.has(this._keyToIdFunction(key));
    }

    hasValue(value: Value): boolean {
        return this._reverseMap.has(this._valueToIdFunction(value));
    }

    get(key: Key): Value | undefined {
        return this._map.get(this._keyToIdFunction(key));
    }

    getKey(value: Value): Key | undefined {
        return this._reverseMap.get(this._valueToIdFunction(value));
    }

    set(key: Key, value: Value): this {
        this._map.set(this._keyToIdFunction(key), value);
        this._reverseMap.set(this._valueToIdFunction(value), key);

        return this;
    }

    get size(): number {
        return this._map.size;
    }

    entries(): [Key, Value][] {
        return [ ...this._map.values() ].map(value => [ this._reverseMap.get(this._valueToIdFunction(value)) as Key, value ]);
    }

    keys(): IterableIterator<Key> {
        return this._reverseMap.values();
    }

    values(): IterableIterator<Value> {
        return this._map.values();
    }
}
