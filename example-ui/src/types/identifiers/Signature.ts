enum SignatureType {
    Base,
    Composite,
    Empty,
    Null
}

export type SignatureJSON = { _class: 'Signature', ids: number[] };

export class Signature {
    readonly _ids: number[];  // TODO private
    readonly _type: SignatureType; // TODO private

    private constructor(input: number | number[], type?: SignatureType) {
        this._ids = typeof input === 'number' ? [ input ] : [ ...input ];
        this._type = type !== undefined ? type : this._ids.length === 1 ? SignatureType.Base : SignatureType.Composite;
    }

    static base(id: number): Signature {
        return new Signature(id);
    }

    static fromIds(ids: number[]) {
        return ids.length === 0 ? Signature.empty : new Signature(ids);
    }

    static copy(signature: Signature): Signature {
        return new Signature(signature._ids, signature._type);
    }

    copy(): Signature {
        return new Signature(this._ids, this._type);
    }

    concatenate(other: Signature): Signature {
        return new Signature(other._ids.concat(this._ids));
    }

    static _emptyInstance = new Signature([], SignatureType.Empty);

    static get empty(): Signature {
        return this._emptyInstance;
    }

    static _nullInstance = new Signature(0, SignatureType.Null);

    static get null(): Signature {
        return this._nullInstance;
    }

    get isBase(): boolean {
        return this._type === SignatureType.Base;
    }

    get isEmpty(): boolean {
        return this._type === SignatureType.Empty;
    }

    get isNull(): boolean {
        return this._type === SignatureType.Null;
    }

    toString(): string {
        if (this._type === SignatureType.Empty)
            return '_EMPTY';

        if (this._type === SignatureType.Null)
            return '_NULL';

        return this._ids.join('.');
    }

    toBase(): Signature[] {
        return this._ids.map(id => new Signature(id));
    }

    getFirstBase(): { first: Signature, rest: Signature } | undefined {
        return this._type === SignatureType.Base || this._type === SignatureType.Composite
            ? { first: Signature.fromIds([ this._ids[0] ]), rest: Signature.fromIds(this._ids.slice(1)) }
            : undefined;
    }

    equals(other: Signature): boolean {
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

    static fromJSON(jsonObject: SignatureJSON): Signature {
        return new Signature(jsonObject.ids);
    }

    toJSON(): SignatureJSON {
        return {
            _class: 'Signature',
            ids: this._ids
        };
    }
}
