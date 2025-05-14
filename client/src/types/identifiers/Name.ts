import { Signature, type SignatureFromServer } from './Signature';

export type NameFromServer = StaticNameFromServer | DynamicNameFromServer;

export function nameFromServer(input: NameFromServer): Name {
    return 'signature' in  input
        ? DynamicName.fromServer(input)
        : StaticName.fromServer(input);
}

export type Name = StaticName | DynamicName;

export type StaticNameFromServer = {
    value: string;
};

export class StaticName {
    private constructor(
        readonly value: string,
    ) {}

    static fromString(value: string): StaticName {
        return new StaticName(value);
    }

    copy(): StaticName {
        return new StaticName(this.value);
    }

    equals(other: Name | undefined): boolean {
        return other instanceof StaticName && other.value === this.value;
    }

    toString(): string {
        return this.value;
    }

    static fromServer(input: StaticNameFromServer): StaticName {
        return new StaticName(input.value);
    }

    toServer(): StaticNameFromServer {
        return {
            value: this.value,
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
            signature: this.signature.toServer(),
        };
    }
}
