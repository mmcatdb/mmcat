import { type Injection, injectionIterator } from './ComparableSet';

export type KeyValue<Key, Value> = { key: Key, value: Value };

export class ComparableMap<Key, KeyId, Value> implements Map<Key, Value> {
    private readonly map = new Map<KeyId, KeyValue<Key, Value>>();

    constructor(
        private readonly keyToIdFunction: Injection<Key, KeyId>,
    ) {}

    clear(): void {
        this.map.clear();
    }

    delete(key: Key): boolean {
        return this.map.delete(this.keyToIdFunction(key));
    }

    has(key: Key): boolean {
        return this.map.has(this.keyToIdFunction(key));
    }

    get(key: Key): Value | undefined {
        return this.map.get(this.keyToIdFunction(key))?.value;
    }

    set(key: Key, value: Value): this {
        this.map.set(this.keyToIdFunction(key), { key, value });
        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): MapIterator<[Key, Value]> {
        return injectionIterator(this.map.entries(), ([ , keyValue ]) => [ keyValue.key, keyValue.value ]);
    }

    keyIds(): IterableIterator<KeyId> {
        return this.map.keys();
    }

    forEach(callbackfn: (value: Value, key: Key, map: Map<Key, Value>) => void): void {
        this.map.forEach(keyValue => callbackfn(keyValue.value, keyValue.key, this));
    }

    keys(): MapIterator<Key> {
        return injectionIterator(this.map.values(), keyValue => keyValue.key);
    }

    values(): MapIterator<Value> {
        return injectionIterator(this.map.values(), keyValue => keyValue.value);
    }

    [Symbol.iterator](): MapIterator<[Key, Value]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'ComparableMap';
}
