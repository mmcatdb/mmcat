type KeyToIdFunction<Key, KeyID> = (key: Key) => KeyID;

export class ComparableMap<Key, KeyID, Value> {
    private keyToIdFunction: KeyToIdFunction<Key, KeyID>;
    private map = new Map() as Map<KeyID, Value>;

    public constructor(keyToIdFunction: KeyToIdFunction<Key, KeyID>) {
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

    entries(): IterableIterator<[KeyID, Value]> {
        return this.map.entries();
    }

    keyIds(): IterableIterator<KeyID> {
        return this.map.keys();
    }

    values(): IterableIterator<Value> {
        return this.map.values();
    }
}
