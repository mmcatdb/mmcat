import { Signature, type SignatureFromServer } from "./Signature";

export type NameFromServer = StaticNameFromServer | DynamicNameFromServer;

export function nameFromServer(input: NameFromServer): Name {
    return 'signature' in  input
        ? DynamicName.fromServer(input)
        : StaticName.fromServer(input);
}

export type Name = StaticName | DynamicName;

export type StaticNameFromServer = { value: string, type: 'STATIC' | 'ANONYMOUS' };

export class StaticName {
    readonly value: string;
    readonly _isAnonymous: boolean;

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

    equals(other: Name | undefined): boolean {
        return other instanceof StaticName
            && other._isAnonymous === this._isAnonymous
            && other.value === this.value;
    }

    toString(): string {
        return this._isAnonymous ? '_' : this.value;
    }

    static fromServer(input: StaticNameFromServer): StaticName {
        return new StaticName(input.value, input.type === 'ANONYMOUS');
    }

    toServer(): StaticNameFromServer {
        return {
            value: this.value,
            type: this._isAnonymous ? 'ANONYMOUS' : 'STATIC'
        };
    }
}

export type DynamicNameFromServer = { signature: SignatureFromServer };

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

    static fromServer(input: DynamicNameFromServer): DynamicName {
        return new DynamicName(Signature.fromServer(input.signature));
    }

    toServer(): DynamicNameFromServer {
        return {
            signature: this.signature.toServer()
        };
    }
}
