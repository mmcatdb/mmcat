import { SignatureId, SignatureIdFactory, type SignatureIdFromServer } from "./SignatureId";

export enum Type {
    Signatures = 'Signatures',
    Value = 'Value',
    Generated = 'Generated'
}

export type NonSignaturesType = Type.Value | Type.Generated;

export type ObjectIdsFromServer = {
    type: Type;
    signatureIds?: SignatureIdFromServer[];
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

    static fromServer(input: ObjectIdsFromServer): ObjectIds {
        const type = input.type;
        const signatureIds = input.signatureIds?.map(SignatureId.fromServer);
        return new ObjectIds(type, signatureIds);
    }

    toServer(): ObjectIdsFromServer {
        return {
            type: this.type,
            signatureIds: this.type === Type.Signatures ? this._signatureIds.map(id => id.toServer()) : undefined
        };
    }

    public generateDefaultSuperId(): SignatureId {
        if (this.type !== Type.Signatures)
            return SignatureIdFactory.createEmpty();

        return SignatureId.union(this._signatureIds);
    }

}
