type Injection<Input, Output> = (input: Input) => Output;

export class TwoWayComparableMap<Key, KeyId, Value, ValueId> {
    private keyToIdFunction: Injection<Key, KeyId>;
    private valueToIdFunction: Injection<Value, ValueId>;
    private map = new Map() as Map<KeyId, Value>;
    private reverseMap = new Map() as Map<ValueId, Key>;

    public constructor(keyToIdFunction: Injection<Key, KeyId>, valueToIdFunction: Injection<Value, ValueId>) {
        this.keyToIdFunction = keyToIdFunction;
        this.valueToIdFunction = valueToIdFunction;
    }

    clear(): void {
        this.map.clear();
        this.reverseMap.clear();
    }

    delete(key: Key): boolean {
        const keyId = this.keyToIdFunction(key);
        const value = this.map.get(keyId);
        if (value === undefined)
            return false;

        const result = this.map.delete(keyId);
        return this.reverseMap.delete(this.valueToIdFunction(value)) && result;
    }

    deleteValue(value: Value) {
        const valueId = this.valueToIdFunction(value);
        const key = this.reverseMap.get(valueId);
        if (key === undefined)
            return false;

        const result = this.reverseMap.delete(valueId);
        return this.map.delete(this.keyToIdFunction(key)) && result;
    }

    has(key: Key): boolean {
        return this.map.has(this.keyToIdFunction(key));
    }

    hasValue(value: Value): boolean {
        return this.reverseMap.has(this.valueToIdFunction(value));
    }

    get(key: Key): Value | undefined {
        return this.map.get(this.keyToIdFunction(key));
    }

    getKey(value: Value): Key | undefined {
        return this.reverseMap.get(this.valueToIdFunction(value));
    }

    set(key: Key, value: Value): this {
        this.map.set(this.keyToIdFunction(key), value);
        this.reverseMap.set(this.valueToIdFunction(value), key);

        return this;
    }

    get size(): number {
        return this.map.size;
    }

    /*
    entries(): IterableIterator<[Key, Value]> {
        return this.map.entries();
    }
    */

    keys(): IterableIterator<Key> {
        return this.reverseMap.values();
    }

    values(): IterableIterator<Value> {
        return this.map.values();
    }
}
