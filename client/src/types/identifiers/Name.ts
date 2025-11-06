export type NameResponse = StringNameResponse | TypedNameResponse | DynamicNameResponse | IndexNameResponse;

export function nameFromResponse(input: NameResponse): Name {
    if ('value' in input)
        return StringName.fromResponse(input);
    if (input.type === DynamicName.type)
        return DynamicName.fromResponse(input as DynamicNameResponse);
    if (input.type === IndexName.type)
        return IndexName.fromResponse(input as IndexNameResponse);
    return TypedName.fromResponse(input);
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
    static readonly ROOT = '$root';
    /** The key corresponding to the {@link TypedName.VALUE}. */
    static readonly KEY = '$key';
    /** The actual value of the map/array property. */
    static readonly VALUE = '$value';

    /**
     * The property is an element of an array, the name represents its index. Has to be dynamic.
     * @deprecated Use {@link IndexName } instead.
     */
    static readonly INDEX = 'index';
}

type DynamicNameResponse = {
    type: typeof DynamicName.type;
    pattern?: string;
};

/**
 * Name that is mapped to a key in an object / map / dictionary / etc.
 * The actual value of the name is stored in a child property with the name {@link TypedName.KEY}.
 */
export class DynamicName extends TypedName {
    static readonly type = '$dynamic';

    constructor(
        readonly pattern?: string,
    ) {
        super(DynamicName.type);
    }

    static fromResponse(input: DynamicNameResponse): DynamicName {
        return new DynamicName(
            input.pattern,
        );
    }

    toServer(): DynamicNameResponse {
        return {
            type: DynamicName.type,
            pattern: this.pattern,
        };
    }

    equals(other: Name): boolean {
        return other instanceof DynamicName && this.pattern === other.pattern;
    }

    toString(): string {
        const patternString = this.pattern ? '' : '(' + this.pattern + ')';
        return DynamicName.type + patternString;
    }

    static isPatternValid(pattern: string): boolean {
        return patternValidator.test(pattern);
    }
}

const patternValidator = /^[a-zA-Z0-9._\-*]+$/;

type IndexNameResponse = {
    type: typeof IndexName.type;
    dimension: number;
};

/**
 * Stores the value of the index in an array.
 */
export class IndexName extends TypedName {
    static readonly type = '$index';

    constructor(
        /** An array can be multi-dimensional. This tells us for which dimension this index name is used. Zero based. */
        readonly dimension: number,
    ) {
        super(IndexName.type);
    }

    static fromResponse(input: IndexNameResponse): IndexName {
        return new IndexName(input.dimension);
    }

    equals(other: Name): boolean {
        return other instanceof IndexName && this.dimension == other.dimension;
    }

    toString(): string {
        return DynamicName.type + '(' + this.dimension + ')';
    }
}

/**
 * For convenient navigation in the access path.
 * Immutable.
 */
export class NamePath {
    constructor(
        readonly names: Name[],
    ) {}

    replaceLast(name: Name): NamePath {
        const names = [ ...this.names ];
        names[names.length - 1] = name;
        return new NamePath(names);
    }

    append(name: Name): NamePath {
        return new NamePath([ ...this.names, name ]);
    }

    pop(): NamePath {
        if (this.names.length === 0)
            throw new Error('Cannot pop from an empty NamePath.');

        return new NamePath(this.names.slice(0, -1));
    }

    toString(): string {
        return this.names.map(name => name.toString()).join('.');
    }
}

export class NamePathBuilder {
    private names: Name[];

    constructor(...names: Name[]) {
        this.names = names;
    }

    prepend(name: Name): NamePathBuilder {
        this.names.push(name);
        return this;
    }

    shift(): NamePathBuilder {
        this.names.pop();
        return this;
    }

    build(): NamePath {
        return new NamePath(this.names.toReversed());
    }
}
