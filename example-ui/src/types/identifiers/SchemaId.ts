import { Signature, type SignatureJSON } from "./Signature";

export type SchemaIdJSON = {
    _class: 'Id',
    signatures: SignatureJSON[]
}

export class SchemaId {
    _signatures: Signature[];

    constructor(signatures: Signature[]) {
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

export class SchemaIdFactory {
    _signatures = [] as Signature[];
    _schemaId: SchemaId;

    constructor(signatures?: Signature[]) {
        if (signatures)
            this._signatures = signatures;

        this._schemaId = new SchemaId(this._signatures);
    }

    get schemaId(): SchemaId {
        return this._schemaId;
    }

    get isEmpty(): boolean {
        return this._signatures.length === 0;
    }

    get length(): number {
        return this._signatures.length;
    }

    addSignature(signature: Signature): void {
        this._signatures.push(signature);
        this._schemaId = new SchemaId(this._signatures);
    }

    static createEmpty(): SchemaId {
        return new SchemaId([ Signature.empty ]);
    }
}
