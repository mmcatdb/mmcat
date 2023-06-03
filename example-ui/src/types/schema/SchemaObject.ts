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
    ids?: ObjectIdsFromServer;
    superId: SignatureIdFromServer;
    //databases?: string[];
    iri?: Iri;
    pimIri?: Iri;
};

export class SchemaObject {
    private readonly originalPosition?: ComparablePosition;
    private _logicalModels = new ComparableSet<LogicalModel, Id>(logicalModel => logicalModel.id);

    private constructor(
        readonly key: Key,
        readonly label: string,
        public position: ComparablePosition,
        public ids: ObjectIds | undefined,
        public superId: SignatureId,
        readonly iri: Iri | undefined,
        readonly pimIri: Iri | undefined,
        private _isNew: boolean,
    ) {
        this.originalPosition = _isNew ? undefined : position.copy();
    }

    static fromServer(input: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject(
            Key.fromServer(input.key),
            input.label,
            ComparablePosition.fromPosition(input.position),
            input.ids ? ObjectIds.fromServer(input.ids) : undefined,
            SignatureId.fromServer(input.superId),
            input.iri,
            input.pimIri,
            false,
        );

        return object;
    }

    static createNew(key: Key, def: ObjectDefinition): SchemaObject {
        const [ iri, pimIri ] = 'iri' in def
            ? [ def.iri, def.pimIri ]
            : [ undefined, undefined ];

        const object = new SchemaObject(
            key,
            def.label,
            def.position?.copy() ?? ComparablePosition.createDefault(),
            def.ids,
            SignatureId.union([]),
            iri,
            pimIri,
            true,
        );

        object._updateDefaultSuperId(); // TODO maybe a computed variable?

        return object;
    }

    createCopy(def: ObjectDefinition): SchemaObject {
        return SchemaObject.createNew(this.key, def);
    }

    _updateDefaultSuperId() {
        this.superId = this.ids?.generateDefaultSuperId() ?? SignatureId.union([]);
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

    setLogicalModels(logicalModels: ComparableSet<LogicalModel, Id>) {
        this._logicalModels = logicalModels;
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
    position?: ComparablePosition;
} & Optional<{
    iri: Iri;
    pimIri: Iri;
}>;
