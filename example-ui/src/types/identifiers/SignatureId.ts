import { Signature, type SignatureFromServer, type SignatureJSON } from "./Signature";

export type SignatureIdJSON = {
    signatures: SignatureJSON[]
}

export class SignatureId {
    _signatures: Signature[]; // TODO make set?

    constructor(signatures: Signature[]) {
        this._signatures = signatures;
    }

    get signatures(): Signature[] {
        return this._signatures;
    }

    static fromJSON(jsonObject: SignatureIdJSON): SignatureId {
        const signatures = jsonObject.signatures.map(signature => Signature.fromJSON(signature));
        return new SignatureId(signatures);
    }

    static fromServer(input: SignatureIdFromServer): SignatureId {
        return new SignatureId(input.map(Signature.fromServer));
    }

    static union(ids: SignatureId[]): SignatureId {
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

        return new SignatureId(union);
    }

    toJSON(): SignatureIdJSON {
        return {
            signatures: this._signatures.map(signature => signature.toJSON())
        };
    }
}

export type SignatureIdFromServer = SignatureFromServer[];

export class SignatureIdFactory {
    _signatures = [] as Signature[];
    _signatureId: SignatureId;

    constructor(signatures?: Signature[]) {
        if (signatures)
            this._signatures = signatures;

        this._signatureId = new SignatureId(this._signatures);
    }

    get signatureId(): SignatureId {
        return this._signatureId;
    }

    get isEmpty(): boolean {
        return this._signatures.length === 0;
    }

    get length(): number {
        return this._signatures.length;
    }

    addSignature(signature: Signature): void {
        this._signatures.push(signature);
        this._signatureId = new SignatureId(this._signatures);
    }

    static createEmpty(): SignatureId {
        return new SignatureId([ Signature.empty ]);
    }
}
