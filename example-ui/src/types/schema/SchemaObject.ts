import { ComparableSet } from "@/utils/ComparableSet";
import type { Iri } from "@/types/integration";
import type { Position } from "cytoscape";
import type { DatabaseWithConfiguration } from "../database";
import { Key, ObjectIds, SignatureId, Type, type KeyJSON, type NonSignaturesType, type ObjectIdsJSON, type SignatureIdJSON } from "../identifiers";
import { ComparablePosition, type PositionUpdate } from "./Position";
import type { LogicalModel } from "../logicalModel";
import type { Entity, Id } from "../id";

export type SchemaObjectJSON = {
    label: string,
    key: KeyJSON,
    ids?: ObjectIdsJSON,
    superId: SignatureIdJSON,
    databases?: string[],
    iri?: Iri,
    pimIri?: Iri
}

export class SchemaObject implements Entity {
    //key: number | undefined;
    //label: number | undefined;
    iri?: Iri;
    pimIri?: Iri;

    id!: Id;
    label!: string;
    key!: Key;
    ids?: ObjectIds;
    superId!: SignatureId;
    position!: ComparablePosition;
    _isNew!: boolean;

    _originalPosition?: ComparablePosition;

    _logicalModels = new ComparableSet<LogicalModel, Id>(logicalModel => logicalModel.id);

    private constructor() {}

    static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject();

        const jsonObject = JSON.parse(input.jsonValue) as SchemaObjectJSON;
        object.id = input.id;
        object.label = jsonObject.label;
        object.key = Key.fromServer(jsonObject.key);
        object.ids = jsonObject.ids ? ObjectIds.fromJSON(jsonObject.ids) : undefined;
        object.superId = SignatureId.fromJSON(jsonObject.superId);
        object._isNew = false;
        object.position = new ComparablePosition(input.position);
        object._originalPosition = new ComparablePosition(input.position);
        object.iri = jsonObject.iri;
        object.pimIri = jsonObject.pimIri;

        return object;
    }

    static createNew(id: Id, label: string, key: Key, ids?: ObjectIds, iri?: Iri, pimIri?: Iri): SchemaObject {
        const object = new SchemaObject();

        object.id = id;
        object.label = label;
        object.key = key;
        object.ids = ids;
        object._updateDefaultSuperId(); // TODO maybe a computed variable?

        object.position = new ComparablePosition({ x: 0, y: 0});
        object._isNew = true;
        object.iri = iri;
        object.pimIri = pimIri;

        return object;
    }

    _updateDefaultSuperId() {
        this.superId = this.ids?.generateDefaultSuperId() || SignatureId.union([]);
    }

    addSignatureId(signatureId: SignatureId): void {
        if (this.ids && this.ids.type !== Type.Signatures)
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
        if (!this.ids || this.ids.type !== Type.Signatures)
            return;

        const newIds = this.ids.signatureIds.filter((_, i) => i !== index);
        this.ids = newIds.length > 0 ? ObjectIds.createSignatures(newIds) : undefined;
        this._updateDefaultSuperId();
    }

    deleteNonSignatureId() {
        this.ids = undefined;
        this._updateDefaultSuperId();
    }

    get canBeSimpleProperty(): boolean {
        if (!this.ids) // This should not happen (i.e., ids should have a value when this property is relevant)
            return false;

        if (this.ids.type !== Type.Signatures)
            return true;

        for (const id of this.ids.signatureIds) {
            if (id.signatures.length < 2)
                return true;
        }

        return false;
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

    toPositionUpdate(): PositionUpdate | null {
        return this.position.equals(this._originalPosition) ? null : { schemaObjectId: this.id, position: this.position };
    }

    toJSON(): SchemaObjectJSON {
        return {
            label: this.label,
            key: this.key.toJSON(),
            ids: this.ids?.toJSON(),
            superId: this.superId.toJSON(),
            iri: this.iri,
            pimIri: this.pimIri
        };
    }
}

export type SchemaObjectUpdate = {
    temporaryId: Id;
    position: Position;
    jsonValue: string;
}

export type SchemaObjectFromServer = {
    id: Id;
    jsonValue: string;
    position: Position;
}
