import { ComparableSet } from '../utils/ComparableSet';
import { Signature, type SignatureResponse } from './Signature';

export type SignatureIdResponse = SignatureResponse[];

export class SignatureId {
    private readonly _signatures = new ComparableSet<Signature, string>(s => s.toString());

    private constructor(signatures: Signature[]) {
        for (const signature of signatures)
            this._signatures.add(signature);
    }

    get size(): number {
        return this._signatures.size;
    }

    get signatures(): SetIterator<Signature> {
        return this._signatures.values();
    }

    static create(signatures: Signature[]): SignatureId {
        if (signatures.length === 1 && signatures[0].isEmpty)
            return this.empty();

        return new SignatureId(signatures);
    }

    static fromResponse(input: SignatureIdResponse): SignatureId {
        return new SignatureId(input.map(Signature.fromResponse));
    }

    private static readonly emptyInstance = new SignatureId([ Signature.empty() ]);

    static empty(): SignatureId {
        return this.emptyInstance;
    }

    static union(ids: SignatureId[]): SignatureId {
        const union: Signature[] = [];
        ids.forEach(id => {
            id.signatures.forEach(signature => {
                // Not the most efficient but who cares.
                for (const signatureInUnion of union) {
                    if (signatureInUnion.equals(signature))
                        return;
                }

                union.push(signature);
            });
        });

        return new SignatureId(union);
    }

    equals(other: SignatureId): boolean {
        if (this._signatures.size !== other._signatures.size)
            return false;

        for (const signature of this._signatures) {
            if (!other._signatures.has(signature))
                return false;
        }

        return true;
    }

    toServer(): SignatureIdResponse {
        return [ ...this._signatures.values() ].map(signature => signature.toServer());
    }

    toString(): string {
        return [ ...this._signatures.values() ].map(s => s.toString()).join(', ');
    }
}
