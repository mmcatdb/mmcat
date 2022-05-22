enum SignatureType {
    Base,
    Composite,
    Empty,
    Null
}

export type SignatureJSON = { _class: 'Signature', ids: number[], isNull: boolean };

export class Signature {
    readonly _ids: number[];  // TODO private
    readonly _type: SignatureType; // TODO private

    private constructor(input: number | number[], isNull = false) {
        this._ids = typeof input === 'number' ? [ input ] : [ ...input ];
        this._type = isNull ?
            SignatureType.Null :
            this._ids.length === 0 ?
                SignatureType.Empty :
                this._ids.length === 1 ?
                    SignatureType.Base :
                    SignatureType.Composite;
    }

    static fromServer(input: SignatureFromServer): Signature {
        return new Signature(input.ids, input.isNull);
    }

    static base(id: number): Signature {
        return new Signature(id);
    }

    static fromIds(ids: number[]) {
        return ids.length === 0 ? Signature.empty : new Signature(ids);
    }

    dual(): Signature {
        const n = this._ids.length;
        if (n == 0)
            return this;

        const array = [] as number[];
        for (let i = 0; i < n; i++)
            array.push(- this._ids[n - i - 1]);

        return new Signature(array);
    }

    isBaseAndDualOf(signature: Signature): boolean {
        return this.baseValue !== null && signature.baseValue !== null && this.baseValue === - signature.baseValue;
    }

    copy(): Signature {
        return new Signature(this._ids, this._type === SignatureType.Null);
    }

    concatenate(other: Signature): Signature {
        return new Signature(other._ids.concat(this._ids));
    }

    static _emptyInstance = new Signature([]);

    static get empty(): Signature {
        return this._emptyInstance;
    }

    static _nullInstance = new Signature(0, true);

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

    get baseValue(): number | null {
        return this.isBase ? this._ids[0] : null;
    }

    toString(): string {
        if (this._type === SignatureType.Empty)
            return '_EMPTY';

        if (this._type === SignatureType.Null)
            return '_NULL';

        return this._ids.join('.');
    }

    toBases(): Signature[] {
        return this._ids.map(id => new Signature(id));
    }

    getFirstBase(): { first: Signature, rest: Signature } | undefined {
        return this._type === SignatureType.Base || this._type === SignatureType.Composite ?
            {
                first: Signature.fromIds([ this._ids[this._ids.length - 1] ]),
                rest: Signature.fromIds(this._ids.slice(0, -1))
            } :
            undefined;
    }

    getLastBase(): { rest: Signature, last: Signature} | undefined {
        return this._type === SignatureType.Base || this._type === SignatureType.Composite ?
            {
                rest: Signature.fromIds(this._ids.slice(1)),
                last: Signature.fromIds([ this._ids[0] ])
            } :
            undefined;
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
        return new Signature(jsonObject.ids, jsonObject.isNull);
    }

    toJSON(): SignatureJSON {
        return {
            _class: 'Signature',
            ids: this._ids,
            isNull: this.isNull
        };
    }
}

export type SignatureFromServer = {
    ids: number[];
    isNull: boolean;
}
