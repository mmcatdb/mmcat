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

    static union(ids: SchemaId[]): SchemaId {
        const union = [] as Signature[];
        ids.forEach(id => {
            id.signatures.forEach(signature => {
                for (const signatureInUnion of union) {
                    if (signatureInUnion.equals(signature))
                        return;
                }

                union.push(signature);
            });
        });

        return new SchemaId(union);
    }

    toJSON(): SchemaIdJSON {
        return {
            _class: 'Id',
            signatures: this._signatures.map(signature => signature.toJSON())
        };
    }
}
