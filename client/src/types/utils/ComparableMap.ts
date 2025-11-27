import { type Injection, injectionIterator } from './ComparableSet';

export type KeyValue<TKey, TValue> = { key: TKey, value: TValue };

export class ComparableMap<TKey, TKeyId, TValue> implements Map<TKey, TValue> {
    private readonly map = new Map<TKeyId, KeyValue<TKey, TValue>>();

    constructor(
        private readonly keyToIdFunction: Injection<TKey, TKeyId>,
    ) {}

    clear(): void {
        this.map.clear();
    }

    delete(key: TKey): boolean {
        return this.map.delete(this.keyToIdFunction(key));
    }

    has(key: TKey): boolean {
        return this.map.has(this.keyToIdFunction(key));
    }

    get(key: TKey): TValue | undefined {
        return this.map.get(this.keyToIdFunction(key))?.value;
    }

    set(key: TKey, value: TValue): this {
        this.map.set(this.keyToIdFunction(key), { key, value });
        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): MapIterator<[TKey, TValue]> {
        return injectionIterator(this.map.entries(), ([ , keyValue ]) => [ keyValue.key, keyValue.value ]);
    }

    keyIds(): IterableIterator<TKeyId> {
        return this.map.keys();
    }

    forEach(callbackfn: (value: TValue, key: TKey, map: Map<TKey, TValue>) => void): void {
        this.map.forEach(keyValue => callbackfn(keyValue.value, keyValue.key, this));
    }

    keys(): MapIterator<TKey> {
        return injectionIterator(this.map.values(), keyValue => keyValue.key);
    }

    values(): MapIterator<TValue> {
        return injectionIterator(this.map.values(), keyValue => keyValue.value);
    }

    [Symbol.iterator](): MapIterator<[TKey, TValue]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'ComparableMap';
}
