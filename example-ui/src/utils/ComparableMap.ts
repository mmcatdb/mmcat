import { type Injection, injectionIterator } from './ComparableSet';

export type KeyValue<Key, Value> = { key: Key, value: Value };

export class ComparableMap<Key, KeyId, Value> implements Map<Key, Value> {
    _keyToIdFunction: Injection<Key, KeyId>;
    _map: Map<KeyId, KeyValue<Key, Value>> = new Map();

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
        return this._map.get(this._keyToIdFunction(key))?.value;
    }

    set(key: Key, value: Value): this {
        this._map.set(this._keyToIdFunction(key), { key, value });
        return this;
    }

    get size(): number {
        return this._map.size;
    }

    entries(): IterableIterator<[Key, Value]> {
        return injectionIterator(this._map.entries(), ([ , keyValue ]) => [ keyValue.key, keyValue.value ]);
    }

    keyIds(): IterableIterator<KeyId> {
        return this._map.keys();
    }

    forEach(callbackfn: (value: Value, key: Key, map: Map<Key, Value>) => void): void {
        this._map.forEach(keyValue => callbackfn(keyValue.value, keyValue.key, this));
    }

    keys(): IterableIterator<Key> {
        return injectionIterator(this._map.values(), (keyValue) => keyValue.key);
    }

    values(): IterableIterator<Value> {
        return injectionIterator(this._map.values(), (keyValue) => keyValue.value);
    }

    [Symbol.iterator](): IterableIterator<[Key, Value]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'ComparableMap';
}
