import { Signature, type SignatureFromServer } from './Signature';

export type NameFromServer = StaticNameFromServer | SpecialNameFromServer | DynamicNameFromServer;

export function nameFromServer(input: NameFromServer): Name {
    if ('value' in input)
        return StaticName.fromServer(input);
    if ('type' in input)
        return SpecialName.fromServer(input);
    return DynamicName.fromServer(input);
}

export type Name = StaticName | SpecialName | DynamicName;

type StaticNameFromServer = {
    value: string;
};

export class StaticName {
    constructor(
        readonly value: string,
    ) {}

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

type SpecialNameFromServer = {
    type: string;
};

export class SpecialName {
    constructor(
        readonly type: string,
    ) {}

    copy(): SpecialName {
        return new SpecialName(this.type);
    }

    equals(other: Name | undefined): boolean {
        return other instanceof SpecialName && other.type === this.type;
    }

    toString(): string {
        return `<${this.type}>`;
    }

    static fromServer(input: SpecialNameFromServer): SpecialName {
        return new SpecialName(input.type);
    }

    toServer(): SpecialNameFromServer {
        return {
            type: this.type,
        };
    }
}

type DynamicNameFromServer = {
    signature: SignatureFromServer;
};

export class DynamicName {
    constructor(
        readonly signature: Signature,
        // No patterns yet - we will add them later.
    ) {}

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
