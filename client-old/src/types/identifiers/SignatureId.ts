import { Signature, type SignatureFromServer } from './Signature';

export type SignatureIdFromServer = SignatureFromServer[];

export class SignatureId {
    private _signatures: Signature[]; // TODO make set?

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

        // This is O(n^2), however it should be more effective for small ids than the O(n log n) solution.
        for (const signature of this._signatures) {
            if (!other._signatures.find(s => s.equals(signature)))
                return false;
        }

        return true;
    }

    static union(ids: SignatureId[]): SignatureId {
        const union: Signature[] = [];
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
    _signatures: Signature[] = [];
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

    addSignature(signature: Signature): SignatureId {
        this._signatures.push(signature);
        this._signatureId = new SignatureId(this._signatures);
        return this._signatureId;
    }

    static createEmpty(): SignatureId {
        return new SignatureId([ Signature.empty ]);
    }
}
