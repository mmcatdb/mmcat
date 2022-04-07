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

    static fromString(value: string): StaticName {
        return new StaticName(value);
    }

    static copy(name: StaticName): StaticName {
        return name._isAnonymous ? StaticName.anonymous : new StaticName(name.value);
    }

    copy(): StaticName {
        return this._isAnonymous ? StaticName.anonymous : new StaticName(this.value);
    }

    static _anonymousInstance = new StaticName('', true);

    static get anonymous(): StaticName {
        return this._anonymousInstance;
    }

    get isAnonymous(): boolean {
        return this._isAnonymous;
    }

    equals(other: Name): boolean {
        return other instanceof StaticName
            && other._isAnonymous === this._isAnonymous
            && other.value === this.value;
    }

    toString(): string {
        return this._isAnonymous ? '_ANONYMOUS' : this.value;
    }

    static fromJSON(jsonObject: StaticNameJSON): StaticName {
        return new StaticName(jsonObject.value, jsonObject.type === 'ANONYMOUS');
    }

    toJSON(): StaticNameJSON {
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

    static fromSignature(signature: Signature) {
        return new DynamicName(signature);
    }

    static copy(name: DynamicName): DynamicName {
        return new DynamicName(name.signature.copy());
    }

    copy(): DynamicName {
        return new DynamicName(this.signature.copy());
    }

    equals(other: Name): boolean {
        return other instanceof DynamicName && this.signature.equals(other.signature);
    }

    toString(): string {
        return `<${this.signature.toString()}>`;
    }

    static fromJSON(jsonObject: DynamicNameJSON): DynamicName {
        return new DynamicName(Signature.fromJSON(jsonObject.signature));
    }

    toJSON(): DynamicNameJSON {
        return {
            _class: 'DynamicName',
            signature: this.signature.toJSON()
        };
    }
}
