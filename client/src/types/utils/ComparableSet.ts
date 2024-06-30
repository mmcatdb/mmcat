export type Injection<Key, KeyId> = (key: Key) => KeyId;

export class ComparableSet<Key, Id> implements Set<Key> {
    private readonly map = new Map<Id, Key>();

    public constructor(
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

    entries(): IterableIterator<[Key, Key]> {
        return injectionIterator(this.map.entries(), ([ , key ]) => [ key, key ]);
    }

    forEach(callbackfn: (value: Key, value2: Key, set: Set<Key>) => void): void {
        this.map.forEach(value => callbackfn(value, value, this));
    }

    keys(): IterableIterator<Key> {
        return this.map.values();
    }

    values(): IterableIterator<Key> {
        return this.map.values();
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
