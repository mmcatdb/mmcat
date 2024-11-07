import type { Position } from 'cytoscape';
import { idsAreEqual, Key, ObjectIds, SignatureId, type KeyFromServer, type ObjectIdsFromServer, type SignatureIdFromServer } from '../identifiers';
import { SchemaCategoryInvalidError } from './Error';
import { isPositionEqual, type Graph } from '../categoryGraph';

export type SchemaObjectFromServer = {
    key: KeyFromServer;
    ids?: ObjectIdsFromServer;
    superId: SignatureIdFromServer;
};

export type MetadataObjectFromServer = {
    key: KeyFromServer;
    label: string;
    position: Position;
};

export class SchemaObject {
    private constructor(
        readonly key: Key,
        readonly ids: ObjectIds | undefined,
        readonly superId: SignatureId,
        private _isNew: boolean,
    ) {}

    static fromServer(schema: SchemaObjectFromServer): SchemaObject {
        const object = new SchemaObject(
            Key.fromServer(schema.key),
            schema.ids ? ObjectIds.fromServer(schema.ids) : undefined,
            SignatureId.fromServer(schema.superId),
            false,
        );

        return object;
    }

    static createNew(key: Key, def: Omit<ObjectDefinition, 'label'>): SchemaObject {
        const object = new SchemaObject(
            key,
            def.ids,
            def.ids?.generateDefaultSuperId() ?? SignatureId.union([]),
            true,
        );

        return object;
    }

    /** If there is nothing to update, undefined will be returned. */
    update({ ids }: { ids?: ObjectIds | null }): SchemaObject | undefined {
        if (ids === null && this.ids)
            return SchemaObject.createNew(this.key, {});

        if (ids && !idsAreEqual(ids, this.ids))
            return SchemaObject.createNew(this.key, { ids });

        return undefined;
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

export class MetadataObject {
    private constructor(
        readonly label: string,
        readonly position: Position,
    ) {}

    static fromServer(input: MetadataObjectFromServer): MetadataObject {
        return new MetadataObject(
            input.label,
            input.position,
        );
    }

    static createDefault(): MetadataObject {
        return new MetadataObject(
            '',
            { x: 0, y: 0 },
        );
    }

    static create(label: string, position: Position): MetadataObject {
        return new MetadataObject(
            label,
            position,
        );
    }

    toServer(key: Key): MetadataObjectFromServer {
        return {
            key: key.toServer(),
            label: this.label,
            position: this.position,
        };
    }
}

// TODO rename for consistency

export class VersionedSchemaObject {
    public readonly originalMetadata: MetadataObject;

    private constructor(
        readonly key: Key,
        private _metadata: MetadataObject,
        private _graph?: Graph,
    ) {
        this.originalMetadata = _metadata;
    }

    static fromServer(input: SchemaObjectFromServer, metadata: MetadataObjectFromServer): VersionedSchemaObject {
        const output = new VersionedSchemaObject(
            Key.fromServer(input.key),
            MetadataObject.fromServer(metadata),
        );
        output.current = SchemaObject.fromServer(input);

        return output;
    }

    static create(key: Key, graph: Graph | undefined): VersionedSchemaObject {
        return new VersionedSchemaObject(
            key,
            MetadataObject.createDefault(),
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

    get metadata(): MetadataObject {
        return this._metadata;
    }

    // TODO position and label sync with cytoscape ...

    set metadata(value: MetadataObject) {
        const isUpdateNeeded = this._metadata.label !== value.label || !isPositionEqual(this._metadata.position, value.position);
        this._metadata = value;

        if (isUpdateNeeded && this._current)
            this._graph?.getNode(this.key)?.update(this._current);
    }

    private updateGraph(graph: Graph) {
        // TODO Candice be replaced by delete + create?
        const currentNode = graph.getNode(this.key);
        if (!currentNode) {
            if (this._current)
                graph.createNode(this, this._current, this.metadata.position, [ ...this.groupIds.values() ]);

            return;
        }

        if (!this._current)
            currentNode.remove();
        else
            currentNode.update(this._current);
    }

    private readonly groupIds = new Set<string>();

    addGroup(id: string) {
        this.groupIds.add(id);
    }
}
