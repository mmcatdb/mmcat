export type Injection<Key, KeyId> = (key: Key) => KeyId;

export class ComparableSet<Key, Id> implements Set<Key> {
    _keyToIdFunction: Injection<Key, Id>;
    _map: Map<Id, Key> = new Map();

    public constructor(keyToIdFunction: Injection<Key, Id>) {
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

    add(key: Key): this {
        this._map.set(this._keyToIdFunction(key), key);
        return this;
    }

    get size(): number {
        return this._map.size;
    }

    entries(): IterableIterator<[Key, Key]> {
        return injectionIterator(this._map.entries(), ([ , key ]) => [ key, key ]);
    }

    forEach(callbackfn: (value: Key, value2: Key, set: Set<Key>) => void): void {
        this._map.forEach(value => callbackfn(value, value, this));
    }

    keys(): IterableIterator<Key> {
        return this._map.values();
    }

    values(): IterableIterator<Key> {
        return this._map.values();
    }

    [Symbol.iterator](): IterableIterator<Key> {
        return this.keys();
    }

    [Symbol.toStringTag] = 'ComparableSet';
}

export function* injectionIterator<A, B>(iterator: IterableIterator<A>, injection: Injection<A, B>): IterableIterator<B> {
    while (true) {
        const nextResult = iterator.next();
        if (nextResult.done)
            break;

        yield injection(nextResult.value);
    }
}
