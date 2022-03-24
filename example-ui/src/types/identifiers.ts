enum SignatureType {
    Base,
    Composite,
    Empty,
    Null
}

export type SignatureJSON = { _class: 'Signature', ids: number[] };

export class Signature {
    private ids: number[];
    private type: SignatureType;

    private constructor(input: number | number[], type?: SignatureType) {
        this.ids = typeof input === 'number' ? [ input ] : [ ...input ];
        this.type = !!type ? type : this.ids.length === 1 ? SignatureType.Base : SignatureType.Composite;
    }

    public static base(id: number): Signature {
        return new Signature(id);
    }

    public static composite(signatures: Signature[]): Signature {
        const idsArray = signatures.map(signature => signature.ids);
        return new Signature(idsArray.flat());
    }

    public concatenate(other: Signature): Signature {
        return new Signature(other.ids.concat(this.ids));
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
        return this.type === SignatureType.Base;
    }

    public get isNull(): boolean {
        return this.type === SignatureType.Null;
    }

    public toString(): string {
        if (this.type === SignatureType.Empty)
            return '_EMPTY';

        if (this.type === SignatureType.Null)
            return '_NULL';

        return this.ids.join('.');
    }

    public equals(other: Signature): boolean {
        return this.type === other.type
            && this.ids.length === other.ids.length
            && Signature.compareIdsWithSameLength(this.ids, other.ids) === 0;
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
    public value: string;
    private isAnonymous: boolean;

    private constructor(value: string, anonymous = false) {
        this.value = value;
        this.isAnonymous = anonymous;
    }

    public static fromString(value: string): StaticName {
        return new StaticName(value);
    }

    private static anonymousInstance = new StaticName('', true);

    public static anonymous(): StaticName {
        return this.anonymousInstance;
    }

    public equals(other: Name): boolean {
        return other instanceof StaticName
            && other.isAnonymous === this.isAnonymous
            && other.value === this.value;
    }

    public toString(): string {
        return this.isAnonymous ? '_ANONYMOUS' : this.value;
    }

    public static fromJSON(jsonObject: StaticNameJSON): StaticName {
        return new StaticName(jsonObject.value, jsonObject.type === 'ANONYMOUS');
    }
}

export type DynamicNameJSON = { _class: 'DynamicName', signature: SignatureJSON }

export class DynamicName {
    private signature: Signature;

    private constructor(signature: Signature) {
        this.signature = signature;
    }

    public static fromSignature(signature: Signature) {
        return new DynamicName(signature);
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
