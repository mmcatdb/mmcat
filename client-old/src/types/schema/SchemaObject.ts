import type { Position } from 'cytoscape';
import { Key, ObjectIds, SignatureId, type KeyFromServer, type ObjectIdsFromServer, type SignatureIdFromServer } from '../identifiers';
import { ComparablePosition } from './Position';
import { SchemaCategoryInvalidError } from './Error';
import type { Graph } from '../categoryGraph';

export type SchemaObjectDataFromServer = {
    label: string;
    ids?: ObjectIdsFromServer;
    superId: SignatureIdFromServer;
};

export type SchemaObjectMetadataFromServer = {
    position: Position;
};

export class SchemaObject {
    private constructor(
        readonly key: Key,
        readonly label: string,
        readonly ids: ObjectIds | undefined,
        readonly superId: SignatureId,
        private _isNew: boolean,
    ) {}

    static fromServer(key: KeyFromServer, input: SchemaObjectDataFromServer): SchemaObject {
        const object = new SchemaObject(
            Key.fromServer(key),
            input.label,
            input.ids ? ObjectIds.fromServer(input.ids) : undefined,
            SignatureId.fromServer(input.superId),
            false,
        );

        return object;
    }

    static createNew(key: Key, def: ObjectDefinition): SchemaObject {
        const object = new SchemaObject(
            key,
            def.label,
            def.ids,
            def.ids?.generateDefaultSuperId() ?? SignatureId.union([]),
            true,
        );

        return object;
    }

    toDefinition(): ObjectDefinition {
        return {
            label: this.label,
            ids: this.ids,
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

    toServer(): SchemaObjectDataFromServer {
        return {
            label: this.label,
            ids: this.ids?.toServer(),
            superId: this.superId.toServer(),
        };
    }

    equals(other: SchemaObject | null | undefined): boolean {
        return !!other && this.key.equals(other.key);
    }
}

export type ObjectDefinition = {
    label: string;
    ids?: ObjectIds;
};

export type SchemaObjectFromServer = {
    key: KeyFromServer;
    data: SchemaObjectDataFromServer;
    metadata: SchemaObjectMetadataFromServer;
};

// TODO rename for consistency

export class VersionedSchemaObject {
    private constructor(
        readonly key: Key,
        private _position: ComparablePosition,
        private _graph?: Graph,
    ) {}

    static fromServer(input: SchemaObjectFromServer): VersionedSchemaObject {
        const output = new VersionedSchemaObject(
            Key.fromServer(input.key),
            ComparablePosition.fromPosition(input.metadata.position),
        );
        output.current = SchemaObject.fromServer(input.key, input.data);

        return output;
    }

    static create(key: Key, graph: Graph | undefined): VersionedSchemaObject {
        return new VersionedSchemaObject(
            key,
            ComparablePosition.createDefault(),
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

    get position(): ComparablePosition {
        const currentPosition = this._graph?.getNode(this.key)?.cytoscapeIdAndPosition.position;
        // The fallback option this._position represents the original position the object has if it isn't in any graph.
        return currentPosition ? ComparablePosition.fromPosition(currentPosition) : this._position;
    }

    private updateGraph(graph: Graph) {
        // TODO Candice be replaced by delete + create?
        const currentNode = graph.getNode(this.key);
        if (!currentNode) {
            if (this._current)
                graph.createNode(this._current, this._position, [ ...this.groupIds.values() ]);

            return;
        }

        if (!this._current)
            currentNode.remove();
        else
            currentNode.update(this._current);
    }

    private readonly groupIds: Set<string> = new Set();

    addGroup(id: string) {
        this.groupIds.add(id);
    }
}
