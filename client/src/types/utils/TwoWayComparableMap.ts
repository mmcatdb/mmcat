import type { KeyValue } from './ComparableMap';
import { injectionIterator } from './ComparableSet';

type Injection<Input, Output> = (input: Input) => Output;

export class TwoWayComparableMap<Key, KeyId, Value, ValueId> implements Map<Key, Value> {
    private readonly map = new Map<KeyId, KeyValue<Key, Value>>();
    private readonly reverseMap = new Map<ValueId, Key>();

    constructor(
        private readonly keyToIdFunction: Injection<Key, KeyId>,
        private readonly valueToIdFunction: Injection<Value, ValueId>,
    ) {}

    clear(): void {
        this.map.clear();
        this.reverseMap.clear();
    }

    delete(key: Key): boolean {
        const keyId = this.keyToIdFunction(key);
        const keyValue = this.map.get(keyId);
        if (keyValue === undefined)
            return false;

        const result = this.map.delete(keyId);
        return this.reverseMap.delete(this.valueToIdFunction(keyValue.value)) && result;
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
        return this.map.get(this.keyToIdFunction(key))?.value;
    }

    getKey(value: Value): Key | undefined {
        return this.reverseMap.get(this.valueToIdFunction(value));
    }

    set(key: Key, value: Value): this {
        this.map.set(this.keyToIdFunction(key), { key, value });
        this.reverseMap.set(this.valueToIdFunction(value), key);

        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): MapIterator<[Key, Value]> {
        return injectionIterator(this.map.values(), keyValue => [ keyValue.key, keyValue.value ]);
    }

    forEach(callbackfn: (value: Value, key: Key, map: Map<Key, Value>) => void): void {
        this.map.forEach(keyValue => callbackfn(keyValue.value, keyValue.key, this));
    }

    keys(): MapIterator<Key> {
        return this.reverseMap.values();
    }

    values(): MapIterator<Value> {
        return injectionIterator(this.map.values(), keyValue => keyValue.value);
    }

    [Symbol.iterator](): MapIterator<[Key, Value]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'TwoWayComparableMap';
}
