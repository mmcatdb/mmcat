export type KeyFromServer = number;

export class Key {
    private constructor(
        public readonly value: number,
    ) {}

    static fromServer(input: KeyFromServer): Key {
        return new Key(input);
    }

    static createNew(value: number): Key {
        return new Key(value);
    }

    public toServer(): KeyFromServer {
        return this.value;
    }

    public equals(key: Key): boolean {
        return this.value === key.value;
    }

    public toString(): string {
        return '' + this.value;
    }
}
