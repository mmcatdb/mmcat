import { Signature, type SignatureFromServer } from './Signature';

export type NameFromServer = StringNameFromServer | TypedNameFromServer | DynamicNameFromServer;

export function nameFromServer(input: NameFromServer): Name {
    if ('value' in input)
        return StringName.fromServer(input);
    if (!('signature' in input))
        return TypedName.fromServer(input);
    return DynamicName.fromServer(input);
}

export type Name = StringName | TypedName;

type StringNameFromServer = {
    value: string;
};

export class StringName {
    constructor(
        readonly value: string,
    ) {}

    static fromServer(input: StringNameFromServer): StringName {
        return new StringName(input.value);
    }

    toServer(): StringNameFromServer {
        return {
            value: this.value,
        };
    }

    equals(other: Name | undefined): boolean {
        return other instanceof StringName && other.value === this.value;
    }

    toString(): string {
        return this.value;
    }
}

type TypedNameFromServer = {
    type: string;
};

export class TypedName {
    constructor(
        readonly type: string,
    ) {}

    static fromServer(input: TypedNameFromServer): TypedName {
        return new TypedName(input.type);
    }

    toServer(): TypedNameFromServer {
        return {
            type: this.type,
        };
    }

    equals(other: Name): boolean {
        return other instanceof TypedName && this.type === other.type && !(other instanceof DynamicName);
    }

    toString(): string {
        return `<${this.type}>`;
    }

    /** The property is a root of the access path tree, the name doesn't mean anything. */
    public static readonly ROOT = 'root';
    /** The property is a value in an object, the name represents its key. */
    public static readonly KEY = 'key';
    /** The property is an element of an array, the name represents its index. */
    public static readonly INDEX = 'index';
}

type DynamicNameFromServer = TypedNameFromServer & {
    signature: SignatureFromServer;
    pattern?: string;
};

export class DynamicName extends TypedName {
    constructor(
        type: string,
        readonly signature: Signature,
        readonly pattern?: string,
    ) {
        super(type);
    }

    static fromServer(input: DynamicNameFromServer): DynamicName {
        return new DynamicName(
            input.type,
            Signature.fromServer(input.signature),
            input.pattern,
        );
    }

    toServer(): DynamicNameFromServer {
        return {
            type: this.type,
            signature: this.signature.toServer(),
            pattern: this.pattern,
        };
    }

    equals(other: Name): boolean {
        return other instanceof DynamicName && this.signature.equals(other.signature);
    }

    toString(): string {
        const patternString = this.pattern == null ? '' : ` (${this.pattern})`;
        return `<${this.type}${patternString}: ${this.signature.toString()}>`;
    }

}
