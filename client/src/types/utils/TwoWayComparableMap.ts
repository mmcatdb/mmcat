import type { KeyValue } from './ComparableMap';
import { injectionIterator } from './ComparableSet';

type Injection<Input, Output> = (input: Input) => Output;

export class TwoWayComparableMap<TKey, TKeyId, TValue, TValueId> implements Map<TKey, TValue> {
    private readonly map = new Map<TKeyId, KeyValue<TKey, TValue>>();
    private readonly reverseMap = new Map<TValueId, TKey>();

    constructor(
        private readonly keyToIdFunction: Injection<TKey, TKeyId>,
        private readonly valueToIdFunction: Injection<TValue, TValueId>,
    ) {}

    clear(): void {
        this.map.clear();
        this.reverseMap.clear();
    }

    delete(key: TKey): boolean {
        const keyId = this.keyToIdFunction(key);
        const keyValue = this.map.get(keyId);
        if (keyValue === undefined)
            return false;

        const result = this.map.delete(keyId);
        return this.reverseMap.delete(this.valueToIdFunction(keyValue.value)) && result;
    }

    deleteValue(value: TValue) {
        const valueId = this.valueToIdFunction(value);
        const key = this.reverseMap.get(valueId);
        if (key === undefined)
            return false;

        const result = this.reverseMap.delete(valueId);
        return this.map.delete(this.keyToIdFunction(key)) && result;
    }

    has(key: TKey): boolean {
        return this.map.has(this.keyToIdFunction(key));
    }

    hasValue(value: TValue): boolean {
        return this.reverseMap.has(this.valueToIdFunction(value));
    }

    get(key: TKey): TValue | undefined {
        return this.map.get(this.keyToIdFunction(key))?.value;
    }

    getKey(value: TValue): TKey | undefined {
        return this.reverseMap.get(this.valueToIdFunction(value));
    }

    set(key: TKey, value: TValue): this {
        this.map.set(this.keyToIdFunction(key), { key, value });
        this.reverseMap.set(this.valueToIdFunction(value), key);

        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): MapIterator<[TKey, TValue]> {
        return injectionIterator(this.map.values(), keyValue => [ keyValue.key, keyValue.value ]);
    }

    forEach(callbackfn: (value: TValue, key: TKey, map: Map<TKey, TValue>) => void): void {
        this.map.forEach(keyValue => callbackfn(keyValue.value, keyValue.key, this));
    }

    keys(): MapIterator<TKey> {
        return this.reverseMap.values();
    }

    values(): MapIterator<TValue> {
        return injectionIterator(this.map.values(), keyValue => keyValue.value);
    }

    [Symbol.iterator](): MapIterator<[TKey, TValue]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'TwoWayComparableMap';
}
