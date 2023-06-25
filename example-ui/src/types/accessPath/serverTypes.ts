import type { NameFromServer, SignatureFromServer, StaticNameFromServer } from '../identifiers';

export type SimplePropertyFromServer = {
    name: NameFromServer;
    signature: SignatureFromServer;
};

export type ComplexPropertyFromServer = {
    name: NameFromServer;
    signature: SignatureFromServer;
    isAuxiliary: boolean;
    subpaths: ChildPropertyFromServer[];
};

export type RootPropertyFromServer = ComplexPropertyFromServer & { name: StaticNameFromServer };

export type ChildPropertyFromServer = ComplexPropertyFromServer | SimplePropertyFromServer;
