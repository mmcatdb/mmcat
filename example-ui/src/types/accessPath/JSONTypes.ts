import type { NameJSON, SignatureJSON, StaticNameJSON } from "../identifiers";

export type SimpleValueJSON = { signature: SignatureJSON };

export type SimplePropertyJSON = {
    _class: 'SimpleProperty',
    name: NameJSON,
    value: SimpleValueJSON
};

export type ComplexPropertyJSON = {
    _class: 'ComplexProperty',
    name: NameJSON,
    signature: SignatureJSON,
    subpaths: ChildPropertyJSON[]
};

export type RootPropertyJSON = ComplexPropertyJSON & { name: StaticNameJSON };

export type ChildPropertyJSON = ComplexPropertyJSON | SimplePropertyJSON;
