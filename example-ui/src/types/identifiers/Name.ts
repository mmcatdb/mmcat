import { Signature, type SignatureJSON } from "./Signature";

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

    public toJSON(): StaticNameJSON {
        return {
            _class: 'StaticName',
            value: this.value,
            type: this._isAnonymous ? 'ANONYMOUS' : 'STATIC_NAME'
        };
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

    public toJSON(): DynamicNameJSON {
        return {
            _class: 'DynamicName',
            signature: this.signature.toJSON()
        };
    }
}
