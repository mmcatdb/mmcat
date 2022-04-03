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
}

export type NameJSON = StaticNameJSON | DynamicNameJSON;

export function nameFromJSON(jsonObject: NameJSON): Name {
    return jsonObject._class === 'StaticName' ? StaticName.fromJSON(jsonObject) : DynamicName.fromJSON(jsonObject);
}

export type Name = StaticName | DynamicName;

export type StaticNameJSON = { _class: 'StaticName', value: string, type: 'STATIC_NAME' | 'ANONYMOUS' };

export class StaticName {
    readonly value: string;
    readonly _isAnonymous: boolean; // TODO private

    private constructor(value: string, anonymous = false) {
        this.value = value;
        this._isAnonymous = anonymous;
    }

    public static fromString(value: string): StaticName {
        return new StaticName(value);
    }

    public static copy(name: StaticName): StaticName {
        return name._isAnonymous ? StaticName.anonymous : new StaticName(name.value);
    }

    public copy(): StaticName {
        return this._isAnonymous ? StaticName.anonymous : new StaticName(this.value);
    }

    private static anonymousInstance = new StaticName('', true);

    public static get anonymous(): StaticName {
        return this.anonymousInstance;
    }

    public get isAnonymous(): boolean {
        return this._isAnonymous;
    }

    public equals(other: Name): boolean {
        return other instanceof StaticName
            && other._isAnonymous === this._isAnonymous
            && other.value === this.value;
    }

    public toString(): string {
        return this._isAnonymous ? '_ANONYMOUS' : this.value;
    }

    public static fromJSON(jsonObject: StaticNameJSON): StaticName {
        return new StaticName(jsonObject.value, jsonObject.type === 'ANONYMOUS');
    }
}

export type DynamicNameJSON = { _class: 'DynamicName', signature: SignatureJSON }

export class DynamicName {
    readonly signature: Signature;

    private constructor(signature: Signature) {
        this.signature = signature;
    }

    public static fromSignature(signature: Signature) {
        return new DynamicName(signature);
    }

    public static copy(name: DynamicName): DynamicName {
        return new DynamicName(Signature.copy(name.signature));
    }

    public copy(): DynamicName {
        return new DynamicName(Signature.copy(this.signature));
    }

    public equals(other: Name): boolean {
        return other instanceof DynamicName && this.signature.equals(other.signature);
    }

    public toString(): string {
        return `<${this.signature.toString()}>`;
    }

    public static fromJSON(jsonObject: DynamicNameJSON): DynamicName {
        return new DynamicName(Signature.fromJSON(jsonObject.signature));
    }
}
