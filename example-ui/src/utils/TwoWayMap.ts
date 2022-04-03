export class TwoWayMap<Key, Value> {
    private map = new Map() as Map<Key, Value>;
    private reverseMap = new Map() as Map<Value, Key>;

    clear(): void {
        this.map.clear();
        this.reverseMap.clear();
    }

    delete(key: Key): boolean {
        const value = this.map.get(key);
        if (value === undefined)
            return false;

        const result = this.map.delete(key);
        return this.reverseMap.delete(value) && result;
    }

    deleteValue(value: Value) {
        const key = this.reverseMap.get(value);
        if (key === undefined)
            return false;

        const result = this.reverseMap.delete(value);
        return this.map.delete(key) && result;
    }

    has(key: Key): boolean {
        return this.map.has(key);
    }

    hasValue(value: Value): boolean {
        return this.reverseMap.has(value);
    }

    get(key: Key): Value | undefined {
        return this.map.get(key);
    }

    getKey(value: Value): Key | undefined {
        return this.reverseMap.get(value);
    }

    set(key: Key, value: Value): this {
        this.map.set(key, value);
        this.reverseMap.set(value, key);

        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): IterableIterator<[Key, Value]> {
        return this.map.entries();
    }

    keys(): IterableIterator<Key> {
        return this.map.keys();
    }

    values(): IterableIterator<Value> {
        return this.map.values();
    }
}
