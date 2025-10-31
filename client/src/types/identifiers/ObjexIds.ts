import { Signature } from './Signature';
import { SignatureId, SignatureIdFactory, type SignatureIdResponse } from './SignatureId';

export enum Type {
    Signatures = 'Signatures',
    Value = 'Value',
    Generated = 'Generated',
}

export type ObjexIdsResponse = {
    type: Type;
    signatureIds?: SignatureIdResponse[];
};

export class ObjexIds {
    readonly type: Type;
    private readonly _signatureIds: SignatureId[];

    private constructor(type: Type, signatureIds?: SignatureId[]) {
        this.type = type;
        this._signatureIds = signatureIds ?? [];
    }

    get signatureIds(): readonly SignatureId[] {
        return this._signatureIds;
    }

    get isSignatures(): boolean {
        return this.type === Type.Signatures;
    }

    static createSignatures(signatureIds: SignatureId[]): ObjexIds {
        return new ObjexIds(Type.Signatures, signatureIds);
    }

    static createNonSignatures(type: Type.Value | Type.Generated): ObjexIds {
        return new ObjexIds(type);
    }

    static createCrossProduct(elements: { signature: Signature, ids: ObjexIds }[]): ObjexIds {
        let signatureIds = [ new SignatureId([]) ];
        for (const element of elements)
            signatureIds = ObjexIds.combineCrossProductIds(signatureIds, element.signature, element.ids);

        return ObjexIds.createSignatures(signatureIds);
    }

    private static combineCrossProductIds(current: SignatureId[], signature: Signature, ids: ObjexIds): SignatureId[] {
        const newSignatureIds = ids.isSignatures
            ? ids._signatureIds.map(id => id.signatures)
            : [ [ Signature.empty() ] ];

        const concatenatedSignatureIds = newSignatureIds.map(signatureId => signatureId.map(s => signature.concatenate(s)));

        return current.flatMap(currentId => concatenatedSignatureIds.map(signatureId => new SignatureId([ ...currentId.signatures, ...signatureId ])));
    }

    static fromResponse(input: ObjexIdsResponse): ObjexIds {
        const type = input.type;
        const signatureIds = input.signatureIds?.map(SignatureId.fromResponse);
        return new ObjexIds(type, signatureIds);
    }

    toServer(): ObjexIdsResponse {
        return {
            type: this.type,
            signatureIds: this.type === Type.Signatures ? this._signatureIds.map(id => id.toServer()) : undefined,
        };
    }

    equals(other: ObjexIds): boolean {
        if (this === other)
            return true;

        if (this.type !== other.type)
            return false;

        if (!this.isSignatures)
            return true;

        if (this._signatureIds.length !== other._signatureIds.length)
            return false;

        // This is O(n^2), however it should be more effective for small ids than the O(n log n) solution.
        for (const signatureId of this._signatureIds) {
            if (!other._signatureIds.find(id => id.equals(signatureId)))
                return false;
        }

        return true;
    }

    public generateDefaultSuperId(): SignatureId {
        if (this.type !== Type.Signatures)
            return SignatureIdFactory.createEmpty();

        return SignatureId.union(this._signatureIds);
    }
}

export function idsAreEqual(a: ObjexIds | undefined, b: ObjexIds | undefined) {
    return (!a && !b) || (b && a?.equals(b));
}
