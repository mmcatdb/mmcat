import type { KeyValue } from './ComparableMap';
import { injectionIterator } from './ComparableSet';

type Injection<Input, Output> = (input: Input) => Output;

export class TwoWayComparableMap<Key, KeyId, Value, ValueId> implements Map<Key, Value> {
    _keyToIdFunction: Injection<Key, KeyId>;
    _valueToIdFunction: Injection<Value, ValueId>;
    _map: Map<KeyId, KeyValue<Key, Value>> = new Map();
    _reverseMap: Map<ValueId, Key> = new Map();

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
        const keyValue = this._map.get(keyId);
        if (keyValue === undefined)
            return false;

        const result = this._map.delete(keyId);
        return this._reverseMap.delete(this._valueToIdFunction(keyValue.value)) && result;
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
        return this._map.get(this._keyToIdFunction(key))?.value;
    }

    getKey(value: Value): Key | undefined {
        return this._reverseMap.get(this._valueToIdFunction(value));
    }

    set(key: Key, value: Value): this {
        this._map.set(this._keyToIdFunction(key), { key, value });
        this._reverseMap.set(this._valueToIdFunction(value), key);

        return this;
    }

    get size(): number {
        return this._map.size;
    }

    entries(): IterableIterator<[Key, Value]> {
        return injectionIterator(this._map.values(), keyValue => [ keyValue.key, keyValue.value ]);
    }

    forEach(callbackfn: (value: Value, key: Key, map: Map<Key, Value>) => void): void {
        this._map.forEach(keyValue => callbackfn(keyValue.value, keyValue.key, this));
    }

    keys(): IterableIterator<Key> {
        return this._reverseMap.values();
    }

    values(): IterableIterator<Value> {
        return injectionIterator(this._map.values(), keyValue => keyValue.value);
    }

    [Symbol.iterator](): IterableIterator<[Key, Value]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'TwoWayComparableMap';
}
