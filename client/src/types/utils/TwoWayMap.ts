export class TwoWayMap<TKey, TValue> implements Map<TKey, TValue> {
    private readonly map = new Map<TKey, TValue>();
    private readonly reverseMap = new Map<TValue, TKey>();

    clear(): void {
        this.map.clear();
        this.reverseMap.clear();
    }

    delete(key: TKey): boolean {
        const value = this.map.get(key);
        if (value === undefined)
            return false;

        const result = this.map.delete(key);
        return this.reverseMap.delete(value) && result;
    }

    deleteValue(value: TValue) {
        const key = this.reverseMap.get(value);
        if (key === undefined)
            return false;

        const result = this.reverseMap.delete(value);
        return this.map.delete(key) && result;
    }

    has(key: TKey): boolean {
        return this.map.has(key);
    }

    hasValue(value: TValue): boolean {
        return this.reverseMap.has(value);
    }

    get(key: TKey): TValue | undefined {
        return this.map.get(key);
    }

    getKey(value: TValue): TKey | undefined {
        return this.reverseMap.get(value);
    }

    set(key: TKey, value: TValue): this {
        this.map.set(key, value);
        this.reverseMap.set(value, key);

        return this;
    }

    get size(): number {
        return this.map.size;
    }

    entries(): MapIterator<[TKey, TValue]> {
        return this.map.entries();
    }

    forEach(callbackfn: (value: TValue, key: TKey, map: Map<TKey, TValue>) => void): void {
        this.map.forEach(callbackfn);
    }

    keys(): MapIterator<TKey> {
        return this.map.keys();
    }

    values(): MapIterator<TValue> {
        return this.map.values();
    }

    [Symbol.iterator](): MapIterator<[TKey, TValue]> {
        return this.entries();
    }

    [Symbol.toStringTag] = 'TwoWayMap';
}
