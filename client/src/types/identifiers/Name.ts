import { Signature, type SignatureResponse } from './Signature';

export type NameResponse = StringNameResponse | TypedNameResponse | DynamicNameResponse;

export function nameFromResponse(input: NameResponse): Name {
    if ('value' in input)
        return StringName.fromResponse(input);
    if (!('signature' in input))
        return TypedName.fromResponse(input);
    return DynamicName.fromResponse(input);
}

export type Name = StringName | TypedName;

type StringNameResponse = {
    value: string;
};

export class StringName {
    constructor(
        readonly value: string,
    ) {}

    static fromResponse(input: StringNameResponse): StringName {
        return new StringName(input.value);
    }

    toServer(): StringNameResponse {
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

type TypedNameResponse = {
    type: string;
};

export class TypedName {
    constructor(
        readonly type: string,
    ) {}

    static fromResponse(input: TypedNameResponse): TypedName {
        return new TypedName(input.type);
    }

    toServer(): TypedNameResponse {
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

type DynamicNameResponse = TypedNameResponse & {
    signature: SignatureResponse;
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

    static fromResponse(input: DynamicNameResponse): DynamicName {
        return new DynamicName(
            input.type,
            Signature.fromResponse(input.signature),
            input.pattern,
        );
    }

    toServer(): DynamicNameResponse {
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
