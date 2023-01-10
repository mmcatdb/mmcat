export class Key {
    private constructor(
        public readonly value: number
    ) {}

    static fromServer(input: KeyFromServer): Key {
        return new Key(input.value);
    }

    static createNew(value: number): Key {
        return new Key(value);
    }

    public toJSON(): KeyJSON {
        return {
            value: this.value
        };
    }

    public equals(key: Key): boolean {
        return this.value === key.value;
    }
}

export type KeyFromServer = {
    value: number;
};

export type KeyJSON = {
    value: number;
};
