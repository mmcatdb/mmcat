export type KeyResponse = number;

export class Key {
    private constructor(
        public readonly value: number,
    ) {}

    static fromResponse(input: KeyResponse): Key {
        return new Key(input);
    }

    static createNew(value: number): Key {
        return new Key(value);
    }

    public toServer(): KeyResponse {
        return this.value;
    }

    public equals(key: Key): boolean {
        return this.value === key.value;
    }

    public toString(): string {
        return '' + this.value;
    }
}
