import { ComparableSet } from "@/utils/ComparableSet";
import type { Iri } from "@/types/integration";
import type { Position } from "cytoscape";
import { Key, ObjectIds, SignatureId, type KeyFromServer, type ObjectIdsFromServer, type SignatureIdFromServer } from "../identifiers";
import { ComparablePosition } from "./Position";
import type { LogicalModel } from "../logicalModel";
import type { Id } from "../id";
import { SchemaCategoryInvalidError } from "./Error";
import type { Optional } from "@/utils/common";
import type { Graph } from "../categoryGraph";

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

    private constructor(
        readonly key: Key,
        readonly label: string,
        public position: ComparablePosition,
        readonly ids: ObjectIds | undefined,
        readonly superId: SignatureId,
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
            def.ids?.generateDefaultSuperId() ?? SignatureId.union([]),
            iri,
            pimIri,
            true,
        );

        return object;
    }

    toDefinition(): ObjectDefinition {
        return {
            label: this.label,
            ids: this.ids,
            position: this.position,
            iri: this.iri,
            pimIri: this.pimIri,
        };
    }

    createCopy(def: ObjectDefinition): SchemaObject {
        return SchemaObject.createNew(this.key, def);
    }

    get isNew(): boolean {
        return this._isNew;
    }

    get idsChecked(): ObjectIds {
        if (!this.ids)
            throw new SchemaCategoryInvalidError(`Object: ${this.key.toString()} doesn't have ids.`);

        return this.ids;
    }

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

export class VersionedSchemaObject {
    private constructor(
        readonly key: Key,
        private _graph?: Graph,
    ) {}

    static create(key: Key, graph: Graph | undefined): VersionedSchemaObject {
        return new VersionedSchemaObject(
            key,
            graph,
        );
    }

    set graph(newGraph: Graph | undefined) {
        this._graph = newGraph;
        if (!newGraph)
            return;

        this.updateGraph(newGraph);
    }

    private _current?: SchemaObject;

    get current(): SchemaObject | undefined {
        return this._current;
    }

    set current(value: SchemaObject | undefined) {
        this._current = value;
        if (this._graph)
            this.updateGraph(this._graph);
    }

    private updateGraph(graph: Graph) {
        const currentNode = graph.getNodeByKey(this.key);
        if (!currentNode) {
            if (this._current)
                graph.createNode(this._current, [ ...this.logicalModels.values() ]);

            return;
        }

        if (!this._current)
            currentNode.remove();
        else
            currentNode.update(this._current);
    }

    private logicalModels: ComparableSet<LogicalModel, Id> = new ComparableSet(model => model.id);

    addLogicalModel(model: LogicalModel) {
        this.logicalModels.add(model);
    }
}
