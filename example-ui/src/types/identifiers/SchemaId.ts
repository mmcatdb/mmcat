import { Signature, type SignatureJSON } from "./Signature";

export type SchemaIdJSON = {
    _class: 'Id',
    signatures: SignatureJSON[]
}

export class SchemaId {
    _signatures: Signature[];

    private constructor(signatures: Signature[]) {
        this._signatures = signatures;
    }

    get signatures(): Signature[] {
        return this._signatures;
    }

    static fromJSON(jsonObject: SchemaIdJSON): SchemaId {
        const signatures = jsonObject.signatures.map(signature => Signature.fromJSON(signature));
        return new SchemaId(signatures);
    }
}
