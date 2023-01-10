import { SignatureId, SignatureIdFactory, type SignatureIdJSON } from "./SignatureId";

export enum Type {
    Signatures = 'Signatures',
    Value = 'Value',
    Generated = 'Generated'
}

export type NonSignaturesType = Type.Value | Type.Generated;

export type ObjectIdsJSON = {
    type: Type;
    signatureIds?: SignatureIdJSON[];
};

export class ObjectIds {
    readonly type: Type;
    readonly _signatureIds: SignatureId[];

    private constructor(type: Type, signatureIds?: SignatureId[]) {
        this.type = type;
        this._signatureIds = signatureIds || [];
    }

    get signatureIds(): SignatureId[] {
        return this._signatureIds;
    }

    get isSignatures(): boolean {
        return this.type === Type.Signatures;
    }

    static createSignatures(signatureIds: SignatureId[]): ObjectIds {
        return new ObjectIds(Type.Signatures, signatureIds);
    }

    static createNonSignatures(type: NonSignaturesType): ObjectIds {
        return new ObjectIds(type);
    }

    static fromJSON(jsonObject: ObjectIdsJSON): ObjectIds {
        const type = jsonObject.type;
        const signatureIds = jsonObject.signatureIds?.map(SignatureId.fromJSON);
        return new ObjectIds(type, signatureIds);
    }

    toJSON(): ObjectIdsJSON {
        return {
            type: this.type,
            signatureIds: this.type === Type.Signatures ? this._signatureIds.map(id => id.toJSON()) : undefined
        };
    }

    public generateDefaultSuperId(): SignatureId {
        if (this.type !== Type.Signatures)
            return SignatureIdFactory.createEmpty();

        return SignatureId.union(this._signatureIds);
    }

}
