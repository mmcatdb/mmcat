export type Injection<Key, KeyId> = (key: Key) => KeyId;

export class ComparableSet<Key, Id> implements Set<Key> {
    private readonly map = new Map<Id, Key>();

    constructor(
        private readonly keyToIdFunction: Injection<Key, Id>,
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

    add(key: Key): this {
        this.map.set(this.keyToIdFunction(key), key);
        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): SetIterator<[Key, Key]> {
        return injectionIterator(this.map.entries(), ([ , key ]) => [ key, key ]);
    }

    forEach(callbackfn: (value: Key, value2: Key, set: Set<Key>) => void): void {
        this.map.forEach(value => callbackfn(value, value, this));
    }

    keys(): SetIterator<Key> {
        return this.map.values();
    }

    values(): SetIterator<Key> {
        return this.map.values();
    }

    [Symbol.iterator](): SetIterator<Key> {
        return this.keys();
    }

    [Symbol.toStringTag] = 'ComparableSet';

    union<U>(): Set<Key | U> {
        throw new Error('Method not implemented.');
    }

    intersection<U>(): Set<Key & U> {
        throw new Error('Method not implemented.');
    }

    difference(): Set<Key> {
        throw new Error('Method not implemented.');
    }

    symmetricDifference<U>(): Set<Key | U> {
        throw new Error('Method not implemented.');
    }

    isSubsetOf(): boolean {
        throw new Error('Method not implemented.');
    }

    isSupersetOf(): boolean {
        throw new Error('Method not implemented.');
    }

    isDisjointFrom(): boolean {
        throw new Error('Method not implemented.');
    }
}

export function injectionIterator<A, B>(iterator: SetIterator<A>, injection: Injection<A, B>): SetIterator<B>;

export function injectionIterator<A, B>(iterator: MapIterator<A>, injection: Injection<A, B>): MapIterator<B>;

export function* injectionIterator<A, B>(iterator: SetIterator<A> | MapIterator<A>, injection: Injection<A, B>): SetIterator<B> | MapIterator<B> {
    while (true) {
        const nextResult = iterator.next();
        if (nextResult.done)
            break;

        yield injection(nextResult.value);
    }
}
