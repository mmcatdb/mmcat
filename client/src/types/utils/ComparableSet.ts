export type Injection<TKey, TKeyId> = (key: TKey) => TKeyId;

export class ComparableSet<TKey, TKeyId> implements Set<TKey> {
    private readonly map = new Map<TKeyId, TKey>();

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

    add(key: TKey): this {
        this.map.set(this.keyToIdFunction(key), key);
        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): SetIterator<[TKey, TKey]> {
        return injectionIterator(this.map.entries(), ([ , key ]) => [ key, key ]);
    }

    forEach(callbackfn: (value: TKey, value2: TKey, set: Set<TKey>) => void): void {
        this.map.forEach(value => callbackfn(value, value, this));
    }

    keys(): SetIterator<TKey> {
        return this.map.values();
    }

    values(): SetIterator<TKey> {
        return this.map.values();
    }

    [Symbol.iterator](): SetIterator<TKey> {
        return this.keys();
    }

    [Symbol.toStringTag] = 'ComparableSet';

    union<U>(): Set<TKey | U> {
        throw new Error('Method not implemented.');
    }

    intersection<U>(): Set<TKey & U> {
        throw new Error('Method not implemented.');
    }

    difference(): Set<TKey> {
        throw new Error('Method not implemented.');
    }

    symmetricDifference<U>(): Set<TKey | U> {
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
