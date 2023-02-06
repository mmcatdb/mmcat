import { Signature, type SignatureFromServer } from "./Signature";

export type SignatureIdFromServer = SignatureFromServer[];

export class SignatureId {
    _signatures: Signature[]; // TODO make set?

    constructor(signatures: Signature[]) {
        this._signatures = signatures;
    }

    get signatures(): Signature[] {
        return this._signatures;
    }

    static fromServer(input: SignatureIdFromServer): SignatureId {
        return new SignatureId(input.map(Signature.fromServer));
    }

    equals(other: SignatureId): boolean {
        if (this._signatures.length !== other._signatures.length)
            return false;

        for (let i = 0; i < this._signatures.length; i++) {
            if (!this._signatures[i].equals(other._signatures[i]))
                return false;
        }

        return true;
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

    toServer(): SignatureIdFromServer {
        return this._signatures.map(signature => signature.toServer());
    }
}

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
