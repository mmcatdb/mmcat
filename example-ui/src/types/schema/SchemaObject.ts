import { ComparableSet } from "@/utils/ComparableSet";
import type { Iri } from "@/types/integration";
import type { Position } from "cytoscape";
import { Key, ObjectIds, SignatureId, type KeyFromServer, type ObjectIdsFromServer, type SignatureIdFromServer, type IdDefinition } from "../identifiers";
import { ComparablePosition } from "./Position";
import type { LogicalModel } from "../logicalModel";
import type { Id } from "../id";
import { SchemaCategoryInvalidError } from "./Error";
import type { Optional } from "@/utils/common";

export type SchemaObjectFromServer = {
    key: KeyFromServer;
    label: string;
    position: Position;
    superId: SignatureIdFromServer;
    ids?: ObjectIdsFromServer;
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
        object.superId = SignatureId.fromServer(input.superId);
        object.ids = input.ids ? ObjectIds.fromServer(input.ids) : undefined;
        object._isNew = false;
        object._originalPosition = new ComparablePosition(input.position);
        object.iri = input.iri;
        object.pimIri = input.pimIri;

        return object;
    }

    static createNew(key: Key, def: ObjectDefinition): SchemaObject {
        const object = new SchemaObject();

        object.key = key;
        object.label = def.label;
        object.position = new ComparablePosition({ x: 0, y: 0 });
        object.ids = def.ids;
        object._updateDefaultSuperId(); // TODO maybe a computed variable?

        object._isNew = true;

        if ('iri' in def) {
            object.iri = def.iri;
            object.pimIri = def.pimIri;
        }

        return object;
    }

    _updateDefaultSuperId() {
        this.superId = this.ids?.generateDefaultSuperId() || SignatureId.union([]);
    }

    addId(def: IdDefinition): void {
        // TODO check if id already exists - this is important for integration.
        // Also, in the second case, the id is not added but replaced, which is not consistent with the name of the method (and the SMO operation).
        if ('type' in def) {
            this.ids = ObjectIds.createNonSignatures(def.type);
        }
        else {
            if (this.ids && !this.ids.isSignatures)
                return;

            const signatureId = 'signatureId' in def
                ? def.signatureId
                : new SignatureId(def.signatures);

            const currentIds = this.ids ? this.ids.signatureIds : [];
            this.ids = ObjectIds.createSignatures([ ...currentIds, signatureId ]);
        }

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

    get idsChecked(): ObjectIds {
        if (!this.ids)
            throw new SchemaCategoryInvalidError(`Object: ${this.key.toString()} doesn't have ids.`);

        return this.ids;
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

    toServer(): SchemaObjectFromServer {
        return {
            key: this.key.toServer(),
            position: this.position,
            label: this.label,
            ids: this.ids?.toServer(),
            superId: this.superId.toServer(),
            iri: this.iri,
            pimIri: this.pimIri,
        };
    }

    equals(other: SchemaObject | null | undefined): boolean {
        return !!other && this.key.equals(other.key);
    }
}

export type ObjectDefinition = {
    label: string;
    ids?: ObjectIds;
} & Optional<{
    iri: Iri;
    pimIri: Iri;
}>;
