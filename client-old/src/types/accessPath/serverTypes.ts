import type { NameFromServer, SignatureFromServer } from '../identifiers';

export type SimplePropertyFromServer = {
    name: NameFromServer;
    signature: SignatureFromServer;
};

export type ComplexPropertyFromServer = {
    name: NameFromServer;
    signature: SignatureFromServer;
    subpaths: ChildPropertyFromServer[];
};

export type ChildPropertyFromServer = ComplexPropertyFromServer | SimplePropertyFromServer;
