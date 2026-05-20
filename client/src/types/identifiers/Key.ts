export type KeyResponse = number;

export class Key {
    private constructor(
        readonly value: number,
    ) {}

    static fromResponse(input: KeyResponse): Key {
        return new Key(input);
    }

    static fromNumber(value: number): Key {
        return new Key(value);
    }

    toServer(): KeyResponse {
        return this.value;
    }

    equals(key: Key): boolean {
        return this.value === key.value;
    }

    toString(): string {
        return '' + this.value;
    }
}
