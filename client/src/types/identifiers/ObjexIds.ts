import { type Signature } from './Signature';
import { SignatureId, type SignatureIdResponse } from './SignatureId';

export type ObjexIdsResponse = SignatureIdResponse[];

export class ObjexIds {
    private readonly _signatureIds: SignatureId[];

    private constructor(signatureIds: SignatureId[]) {
        this._signatureIds = signatureIds;
    }

    get signatureIds(): readonly SignatureId[] {
        return this._signatureIds.length === 0 ? [ SignatureId.empty() ] : this._signatureIds;
    }

    static create(signatureIds: SignatureId[]): ObjexIds {
        return signatureIds.length === 0 ? this.empty() : new ObjexIds(signatureIds);
    }

    // TODO Re-implement if needed (or delete if not). Should be useful when creating maps or arrays.
    // static createCrossProduct(elements: { signature: Signature, ids: ObjexIds }[]): ObjexIds {
    //     let signatureIds = [ new SignatureId([]) ];
    //     for (const element of elements)
    //         signatureIds = ObjexIds.combineCrossProductIds(signatureIds, element.signature, element.ids);

    //     return ObjexIds.create(signatureIds);
    // }

    // private static combineCrossProductIds(current: SignatureId[], signature: Signature, ids: ObjexIds): SignatureId[] {
    //     const newSignatureIds = ids.isEmpty
    //         ? [ [ Signature.empty() ] ]
    //         : ids._signatureIds.map(id => id.signatures);

    //     const concatenatedSignatureIds = newSignatureIds.map(signatureId => signatureId.map(s => signature.concatenate(s)));

    //     return current.flatMap(currentId => concatenatedSignatureIds.map(signatureId => new SignatureId([ ...currentId.signatures, ...signatureId ])));
    // }

    private static readonly emptyInstance = new ObjexIds([]);

    static empty(): ObjexIds {
        return this.emptyInstance;
    }

    get isEmpty(): boolean {
        return this._signatureIds.length === 0;
    }

    static fromResponse(input: ObjexIdsResponse): ObjexIds {
        return this.create(input.map(SignatureId.fromResponse));
    }

    static build(sigantures: Signature[][]) {
        let signatureIds: SignatureId[] = [];

        for (const signatures of sigantures) {
            const id = SignatureId.create(signatures);

            for (const existingId of signatureIds) {
                if (existingId.equals(id))
                    continue;
            }

            signatureIds.push(id);
        }

        if (signatureIds.length > 1)
            signatureIds = signatureIds.filter(id => !id.equals(SignatureId.empty()));

        return ObjexIds.create(signatureIds);
    }

    toServer(): ObjexIdsResponse {
        return this._signatureIds.map(id => id.toServer());
    }

    equals(other: ObjexIds): boolean {
        if (this === other)
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
}
