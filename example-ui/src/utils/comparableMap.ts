type KeyToIdFunction<Key, KeyID> = (key: Key) => KeyID;

export class ComparableMap<Key, KeyID, Value> {
    private keyToIdFunction: KeyToIdFunction<Key, KeyID>;
    private map = new Map() as Map<KeyID, Value>;

    public constructor(keyToIdFunction: KeyToIdFunction<Key, KeyID>) {
        this.keyToIdFunction = keyToIdFunction;
    }

    public set(key: Key, value: Value): ComparableMap<Key, KeyID, Value> {
        this.map.set(this.keyToIdFunction(key), value);
        return this;
    }

    public get(key: Key): Value | undefined {
        return this.map.get(this.keyToIdFunction(key));
    }
}
