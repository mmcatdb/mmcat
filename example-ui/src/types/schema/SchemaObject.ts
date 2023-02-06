import { ComparableSet } from "@/utils/ComparableSet";
import type { Iri } from "@/types/integration";
import type { Position } from "cytoscape";
import { Key, ObjectIds, SignatureId, type KeyJSON, type NonSignaturesType, type ObjectIdsJSON, type SignatureIdJSON } from "../identifiers";
import { ComparablePosition, type PositionUpdate } from "./Position";
import type { LogicalModel } from "../logicalModel";
import type { Entity, Id } from "../id";

export type SchemaObjectFromServer = {
    key: KeyJSON;
    label: string;
    position: Position;
    superId: SignatureIdJSON;
    ids?: ObjectIdsJSON;
    //databases?: string[];
    iri?: Iri;
    pimIri?: Iri;
};

export class SchemaObject {
    key!: Key;
    label!: string;
    position!: ComparablePosition;
    ids?: ObjectIds;
    superId!: SignatureId;
    _isNew!: boolean;

    iri?: Iri;
    pimIri?: Iri;

    _originalPosition?: ComparablePosition;

    _logicalModels = new ComparableSet<LogicalModel, Id>(logicalModel => logicalModel.id);

    private constructor() {}

    static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        object.key = Key.fromServer(input.key);
        object.label = input.label;
        object.position = new ComparablePosition(input.position);
        object.superId = SignatureId.fromJSON(input.superId);
        object.ids = input.ids ? ObjectIds.fromJSON(input.ids) : undefined;
        object._isNew = false;
        object._originalPosition = new ComparablePosition(input.position);
        object.iri = input.iri;
        object.pimIri = input.pimIri;

        return object;
    }

    static createNew(key: Key, label: string, ids?: ObjectIds, iri?: Iri, pimIri?: Iri): SchemaObject {
        const object = new SchemaObject();

        object.key = key;
        object.label = label;
        object.position = new ComparablePosition({ x: 0, y: 0});
        object.ids = ids;
        object._updateDefaultSuperId(); // TODO maybe a computed variable?

        object._isNew = true;
        object.iri = iri;
        object.pimIri = pimIri;

        return object;
    }

    _updateDefaultSuperId() {
        this.superId = this.ids?.generateDefaultSuperId() || SignatureId.union([]);
    }

    addSignatureId(signatureId: SignatureId): void {
        if (this.ids && !this.ids.isSignatures)
            return;

        const currentIds = this.ids ? this.ids.signatureIds : [];
        this.ids = ObjectIds.createSignatures([ ...currentIds, signatureId ]);
        this._updateDefaultSuperId();
    }

    addNonSignatureId(type: NonSignaturesType) {
        this.ids = ObjectIds.createNonSignatures(type);
        this._updateDefaultSuperId();
    }

    deleteSignatureId(index: number): void {
        if (!this.ids || !this.ids.isSignatures)
            return;

        const newIds = this.ids.signatureIds.filter((_, i) => i !== index);
        this.ids = newIds.length > 0 ? ObjectIds.createSignatures(newIds) : undefined;
        this._updateDefaultSuperId();
    }

    deleteNonSignatureId() {
        this.ids = undefined;
        this._updateDefaultSuperId();
    }

    get isNew(): boolean {
        return this._isNew;
    }

    get logicalModels(): LogicalModel[] {
        return [ ...this._logicalModels.values() ];
    }

    setLogicalModel(logicalModel: LogicalModel) {
        this._logicalModels.add(logicalModel);
    }

    setLabel(label: string) {
        this.label = label;
    }

    /*
    toPositionUpdate(): PositionUpdate | null {
        return this.position.equals(this._originalPosition) ? null : { schemaObjectId: this.id, position: this.position };
    }
    */

    toJSON(): SchemaObjectFromServer {
        return {
            key: this.key.toJSON(),
            position: this.position,
            label: this.label,
            ids: this.ids?.toJSON(),
            superId: this.superId.toJSON(),
            iri: this.iri,
            pimIri: this.pimIri
        };
    }

    equals(other: SchemaObject | null | undefined): boolean {
        return !!other && this.key.equals(other.key);
    }
}

export type SchemaObjectUpdate = {
    temporaryId: Id;
    position: Position;
    jsonValue: string;
};
