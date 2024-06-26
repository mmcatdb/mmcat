enum SignatureType {
    Base,
    Composite,
    Empty
}

const SEPARATOR = '.';

export type SignatureFromServer = string;

function determineType(idsLength: number) {
    if (idsLength === 0)
        return SignatureType.Empty;

    return idsLength === 1 ? SignatureType.Base : SignatureType.Composite;
}

export class Signature {
    readonly _ids: number[];
    readonly _type: SignatureType;

    private constructor(input: number | number[]) {
        this._ids = typeof input === 'number' ? [ input ] : [ ...input ];
        this._type = determineType(this._ids.length);
        this.value = this.toValue();
    }

    static fromServer(input: SignatureFromServer): Signature {
        if (input === 'EMPTY')
            return Signature.empty;

        return new Signature(input.split(SEPARATOR).map(base => Number.parseInt(base)));
    }

    static base(id: number): Signature {
        return new Signature(id);
    }

    private static fromIds(ids: number[]) {
        return ids.length === 0 ? Signature.empty : new Signature(ids);
    }

    dual(): Signature {
        const n = this._ids.length;
        if (n == 0)
            return this;

        const array: number[] = [];
        for (let i = 0; i < n; i++)
            array.push(- this._ids[n - i - 1]);

        return new Signature(array);
    }

    isBaseAndDualOf(signature: Signature): boolean {
        return this.baseValue !== null && signature.baseValue !== null && this.baseValue === -signature.baseValue;
    }

    copy(): Signature {
        return new Signature(this._ids);
    }

    concatenate(other: Signature): Signature {
        return new Signature(this._ids.concat(other._ids));
    }

    static _emptyInstance = new Signature([]);

    static get empty(): Signature {
        return this._emptyInstance;
    }

    get isEmpty(): boolean {
        return this._type === SignatureType.Empty;
    }

    get isBase(): boolean {
        return this._type === SignatureType.Base;
    }

    get isBaseDual(): boolean {
        return this.isBase && this._ids[0] < 0;
    }

    get baseValue(): number | null {
        return this.isBase ? this._ids[0] : null;
    }

    readonly value: string;

    private toValue(): string {
        if (this._type === SignatureType.Empty)
            return 'EMPTY';

        return this._ids.join(SEPARATOR);
    }

    toString(): string {
        return this.value;
    }

    toBases(): Signature[] {
        return this._ids.map(id => new Signature(id));
    }

    getFirstBase(): { first: Signature, rest: Signature } | undefined {
        return this._type === SignatureType.Base || this._type === SignatureType.Composite
            ? {
                first: Signature.base(this._ids[0]),
                rest: Signature.fromIds(this._ids.slice(1)),
            }
            : undefined;
    }

    getLastBase(): { rest: Signature, last: Signature} | undefined {
        return this._type === SignatureType.Base || this._type === SignatureType.Composite
            ? {
                rest: Signature.fromIds(this._ids.slice(0, -1)),
                last: Signature.base(this._ids[this._ids.length - 1]),
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
