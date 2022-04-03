type Injection<Key, KeyId> = (key: Key) => KeyId;

export class ComparableMap<Key, KeyId, Value> {
    private keyToIdFunction: Injection<Key, KeyId>;
    private map = new Map() as Map<KeyId, Value>;

    public constructor(keyToIdFunction: Injection<Key, KeyId>) {
        this.keyToIdFunction = keyToIdFunction;
    }

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
        return this.map.get(this.keyToIdFunction(key));
    }

    set(key: Key, value: Value): this {
        this.map.set(this.keyToIdFunction(key), value);
        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): IterableIterator<[KeyId, Value]> {
        return this.map.entries();
    }

    keyIds(): IterableIterator<KeyId> {
        return this.map.keys();
    }

    values(): IterableIterator<Value> {
        return this.map.values();
    }
}
