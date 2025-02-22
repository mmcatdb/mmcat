enum SignatureType {
    Base,
    Composite,
    Empty,
}

const SEPARATOR = '.';

export type SignatureFromServer = string;

function determineType(idsLength: number) {
    if (idsLength === 0)
        return SignatureType.Empty;

    return idsLength === 1 ? SignatureType.Base : SignatureType.Composite;
}

export class Signature {
    private readonly ids: number[];
    private readonly type: SignatureType;

    private constructor(input: number | number[]) {
        this.ids = typeof input === 'number' ? [ input ] : [ ...input ];
        this.type = determineType(this.ids.length);
        this.value = this.toValue();
    }

    static fromServer(input: SignatureFromServer): Signature {
        if (input === 'EMPTY')
            return Signature.empty();

        return new Signature(input.split(SEPARATOR).map(base => Number.parseInt(base)));
    }

    static base(id: number): Signature {
        return new Signature(id);
    }

    private static fromIds(ids: number[]) {
        return ids.length === 0 ? Signature.empty() : new Signature(ids);
    }

    dual(): Signature {
        const n = this.ids.length;
        if (n == 0)
            return this;

        const array: number[] = [];
        for (let i = 0; i < n; i++)
            array.push(- this.ids[n - i - 1]);

        return new Signature(array);
    }

    isBaseAndDualOf(signature: Signature): boolean {
        return this.baseValue !== null && signature.baseValue !== null && this.baseValue === -signature.baseValue;
    }

    concatenate(other: Signature): Signature {
        return new Signature(this.ids.concat(other.ids));
    }

    private static emptyInstance = new Signature([]);

    static empty(): Signature {
        return this.emptyInstance;
    }

    get isEmpty(): boolean {
        return this.type === SignatureType.Empty;
    }

    get isBase(): boolean {
        return this.type === SignatureType.Base;
    }

    get isBaseDual(): boolean {
        return this.isBase && this.ids[0] < 0;
    }

    get baseValue(): number | null {
        return this.isBase ? this.ids[0] : null;
    }

    readonly value: string;

    private toValue(): string {
        if (this.type === SignatureType.Empty)
            return 'EMPTY';

        return this.ids.join(SEPARATOR);
    }

    toString(): string {
        return this.value;
    }

    toBases(): Signature[] {
        return this.ids.map(id => new Signature(id));
    }

    getFirstBase(): { first: Signature, rest: Signature } | undefined {
        return this.type === SignatureType.Base || this.type === SignatureType.Composite
            ? {
                first: Signature.base(this.ids[0]),
                rest: Signature.fromIds(this.ids.slice(1)),
            }
            : undefined;
    }

    getLastBase(): { rest: Signature, last: Signature} | undefined {
        return this.type === SignatureType.Base || this.type === SignatureType.Composite
            ? {
                rest: Signature.fromIds(this.ids.slice(0, -1)),
                last: Signature.base(this.ids[this.ids.length - 1]),
            }
            : undefined;
    }

    equals(other: Signature): boolean {
        return this.value === other.value;
    }

    toServer(): SignatureFromServer {
        return this.value;
    }
}
