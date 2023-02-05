import type { NameJSON, SignatureJSON, StaticNameJSON } from "../identifiers";

export type SimplePropertyJSON = {
    name: NameJSON;
    signature: SignatureJSON;
};

export type ComplexPropertyJSON = {
    name: NameJSON;
    signature: SignatureJSON;
    isAuxiliary: boolean;
    subpaths: ChildPropertyJSON[];
};

export type RootPropertyJSON = ComplexPropertyJSON & { name: StaticNameJSON };

export type ChildPropertyJSON = ComplexPropertyJSON | SimplePropertyJSON;
