import type { NameJSON, SignatureFromServer, StaticNameJSON } from "../identifiers";

export type SimplePropertyJSON = {
    name: NameJSON;
    signature: SignatureFromServer;
};

export type ComplexPropertyJSON = {
    name: NameJSON;
    signature: SignatureFromServer;
    isAuxiliary: boolean;
    subpaths: ChildPropertyJSON[];
};

export type RootPropertyJSON = ComplexPropertyJSON & { name: StaticNameJSON };

export type ChildPropertyJSON = ComplexPropertyJSON | SimplePropertyJSON;
