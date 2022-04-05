enum SignatureType {
    Base,
    Composite,
    Empty,
    Null
}

export type SignatureJSON = { _class: 'Signature', ids: number[] };

export class Signature {
    public readonly _ids: number[];  // TODO private
    public readonly _type: SignatureType; // TODO private

    private constructor(input: number | number[], type?: SignatureType) {
        this._ids = typeof input === 'number' ? [ input ] : [ ...input ];
        this._type = type !== undefined ? type : this._ids.length === 1 ? SignatureType.Base : SignatureType.Composite;
    }

    public static base(id: number): Signature {
        return new Signature(id);
    }

    public static fromIds(ids: number[]) {
        return ids.length === 0 ? Signature.empty : new Signature(ids);
    }

    public static copy(signature: Signature): Signature {
        return new Signature(signature._ids, signature._type);
    }

    public copy(): Signature {
        return new Signature(this._ids, this._type);
    }

    public concatenate(other: Signature): Signature {
        return new Signature(other._ids.concat(this._ids));
    }

    private static emptyInstance = new Signature([], SignatureType.Empty);

    public static get empty(): Signature {
        return this.emptyInstance;
    }

    private static nullInstance = new Signature(0, SignatureType.Null);

    public static get null(): Signature {
        return this.nullInstance;
    }

    public get isBase(): boolean {
        return this._type === SignatureType.Base;
    }

    public get isEmpty(): boolean {
        return this._type === SignatureType.Empty;
    }

    public get isNull(): boolean {
        return this._type === SignatureType.Null;
    }

    public toString(): string {
        if (this._type === SignatureType.Empty)
            return '_EMPTY';

        if (this._type === SignatureType.Null)
            return '_NULL';

        return this._ids.join('.');
    }

    public toBase(): Signature[] {
        return this._ids.map(id => new Signature(id));
    }

    public getFirstBase(): { first: Signature, rest: Signature } | undefined {
        return this._type === SignatureType.Base || this._type === SignatureType.Composite
            ? { first: Signature.fromIds([ this._ids[0] ]), rest: Signature.fromIds(this._ids.slice(1)) }
            : undefined;
    }

    public equals(other: Signature): boolean {
        return this._type === other._type
            && this._ids.length === other._ids.length
            && Signature.compareIdsWithSameLength(this._ids, other._ids) === 0;
    }

    private static compareIdsWithSameLength(ids1: number[], ids2: number[]): number {
        for (let i = 0; i < ids1.length; i++) {
            const diff = ids1[i] - ids2[i];
            if (diff !== 0)
                return diff;
        }

        return 0;
    }

    public static fromJSON(jsonObject: SignatureJSON): Signature {
        return new Signature(jsonObject.ids);
    }

    public toJSON(): SignatureJSON {
        return {
            _class: 'Signature',
            ids: this._ids
        };
    }
}
