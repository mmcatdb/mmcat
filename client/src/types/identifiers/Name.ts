import { Signature, type SignatureFromServer } from './Signature';

export type NameFromServer = StaticNameFromServer | DynamicNameFromServer;

export function nameFromServer(input: NameFromServer): Name {
    return 'signature' in input
        ? DynamicName.fromServer(input)
        : StaticName.fromServer(input);
}

export type Name = StaticName | DynamicName;

enum StaticNameType {
    Static = 'STATIC',
    Anonymous = 'ANONYMOUS',
}

export type StaticNameFromServer = {
    value: string;
    type: StaticNameType;
};

export class StaticName {
    private constructor(
        readonly value: string,
        readonly isAnonymous: boolean,
    ) {}

    private static anonymousInstance = new StaticName('', true);

    static anonymous(): StaticName {
        return this.anonymousInstance;
    }

    static fromString(value: string): StaticName {
        return new StaticName(value, false);
    }

    static fromServer(input: StaticNameFromServer): StaticName {
        if (input.type === StaticNameType.Anonymous)
            return StaticName.anonymous();

        return new StaticName(input.value, false);
    }

    toServer(): StaticNameFromServer {
        return {
            value: this.value,
            type: this.isAnonymous ? StaticNameType.Anonymous : StaticNameType.Static,
        };
    }

    equals(other: Name | undefined): boolean {
        return other instanceof StaticName
            && other.isAnonymous === this.isAnonymous
            && other.value === this.value;
    }

    toString(): string {
        return this.isAnonymous ? '_' : this.value;
    }

}

export type DynamicNameFromServer = {
    signature: SignatureFromServer;
};

export class DynamicName {
    private constructor(
        readonly signature: Signature,
    ) {}

    static fromSignature(signature: Signature) {
        return new DynamicName(signature);
    }

    static fromServer(input: DynamicNameFromServer): DynamicName {
        return new DynamicName(Signature.fromServer(input.signature));
    }

    toServer(): DynamicNameFromServer {
        return {
            signature: this.signature.toServer(),
        };
    }

    equals(other: Name): boolean {
        return other instanceof DynamicName && this.signature.equals(other.signature);
    }

    toString(): string {
        return `<${this.signature.toString()}>`;
    }

}
