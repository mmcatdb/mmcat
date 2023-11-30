import type { Core, EdgeSingular, EventHandler, EventObject, LayoutOptions, NodeSingular } from 'cytoscape';
import type { LogicalModel } from '../logicalModel';
import type { ComparablePosition, SchemaMorphism, SchemaObject } from '../schema';
import { Edge } from './Edge';
import { Node, type Group } from './Node';
import type { Key, Signature } from '../identifiers';
import { ComparableMap } from '@/utils/ComparableMap';

export type TemporaryEdge = {
    delete: () => void;
};

export class Graph {
    private readonly nodes: ComparableMap<Key, number, Node> = new ComparableMap(key => key.value);
    private readonly edges: ComparableMap<Signature, string, Edge> = new ComparableMap(signature => signature.value);
    private eventListener: GraphEventListener;

    constructor(
        private readonly cytoscape: Core,
    ) {
        this.eventListener = new GraphEventListener(cytoscape);
    }

    public resetElements(): void {
        this.cytoscape.elements().remove();

        this.nodes.clear();
        this.edges.clear();
        this.groups = [];
    }

    public batch(callback: () => void): void {
        this.cytoscape.batch(callback);
    }

    listen(): ListenerSession {
        return this.eventListener.openSession();
    }

    resetAvailabilityStatus(): void {
        this.nodes.forEach(node => node.resetAvailabilityStatus());
    }

    groups: Group[] = [];

    getGroupOrAddIt(logicalModel: LogicalModel): Group {
        const results = this.groups.filter(group => group.logicalModel.id === logicalModel.id);
        if (results[0])
            return results[0];

        const id = this.groups.length + 1;
        const newGroup = {
            id,
            logicalModel,
            node: this.cytoscape.add({
                data: {
                    id: 'group_' + id,
                    label: logicalModel.label,
                },
                classes: 'group ' + 'group-' + id,
            }),
        };

        this.groups.push(newGroup);
        return newGroup;
    }

    createNode(object: SchemaObject, position: ComparablePosition, logicalModels: LogicalModel[]): Node {
        const groups = logicalModels.map(logicalModel => this.getGroupOrAddIt(logicalModel));
        const node = Node.create(this.cytoscape, object, position, groups);
        this.nodes.set(object.key, node);

        return node;
    }

    deleteNode(object: SchemaObject) {
        const node = this.nodes.get(object.key);
        if (!node)
            return;

        node.remove();
        this.nodes.delete(object.key);

        // Only the newly created nodes can be deleted an those can't be in any database so we don't have to remove their database placeholders.
        // However, the no-group placeholder has to be removed.
    }

    createEdge(morphism: SchemaMorphism): Edge {
        const dom = this.nodes.get(morphism.domKey) as Node;
        const cod = this.nodes.get(morphism.codKey) as Node;

        const edge = Edge.create(this.cytoscape, morphism, dom, cod);
        this.edges.set(morphism.signature, edge);

        return edge;
    }

    deleteEdge(signature: Signature) {
        const edge = this.getEdge(signature);
        if (!edge)
            return;

        edge.remove();
        this.edges.delete(signature);
    }

    _lastTemporaryEdgeId = 0;

    createTemporaryEdge(node1: Node, node2: Node): TemporaryEdge {
        const id = 'te' + this._lastTemporaryEdgeId;
        this._lastTemporaryEdgeId++;

        this.cytoscape.add({
            data: {
                id,
                source: node1.schemaObject.key.value,
                target: node2.schemaObject.key.value,
                label: '',
            },
            classes: 'temporary',
        });

        return { delete: () => this.cytoscape.remove('#' + id) };
    }

    center() {
        this.cytoscape.center();
    }

    layout() {
        // TODO fix adding objects
        this.cytoscape.layout({
            //name: 'dagre',
            //name: 'cola',
            name: 'fcose',
            animate: false,
            fixedNodeConstraint: [ ...this.nodes.values() ].filter(node => node.isFixed).map(node => node.cytoscapeIdAndPosition),
            //randomize: false,
            //quality: 'proof',
            nodeDimensionsIncludeLabels: true,
            //boundingBox: { x1: 0, x2: 1000, y1: 0, y2: 500 }
        } as LayoutOptions).run();

        this.fixLayout();
    }

    fixLayout() {
        this.nodes.forEach(node => node.isFixed = true);
    }

    resetLayout() {
        this.nodes.forEach(node => node.isFixed = false);

        // A necessary workaround for the bug with nodes without placeholders. More below.
        // Also, both parts of the workaround DO HAVE to be outside the layout function. Otherwise it causes a particularly hard to find bug (when the layout function is called from AddObject, then a new morphism is added in AddMorphism and then this function is called).
        this.groups.forEach(group => group.node.remove());

        this.layout();

        // A continuation of the workaround.
        this.groups.forEach(group => group.node.restore());
        this.nodes.forEach(node => node.refreshGroupPlaceholders());
    }

    getNode(key: Key): Node | undefined {
        return this.nodes.get(key);
    }

    getEdge(signature: Signature): Edge | undefined {
        return this.edges.get(signature);
    }
}

class GraphEventListener {
    private lastSessionId = -1;

    constructor(
        private readonly cytoscape: Core,
    ) {}

    private openSessions: Map<number, ListenerSession> = new Map();

    openSession(): ListenerSession {
        this.lastSessionId++;
        const session = new ListenerSession(this.lastSessionId, this, this.cytoscape);
        this.openSessions.set(this.lastSessionId, session);

        return session;
    }

    onSessionClose(sessionId: number) {
        this.openSessions.delete(sessionId);
    }

    clear() {
        [ ...this.openSessions.values() ].forEach(session => session.close());
    }
}

type NodeEventFunction = (node: Node) => void;
type EdgeEventFunction = (edge: Edge) => void;
type CanvasEventFunction = () => void;

type EventHandlerObject = {
    event: string;
    handler: EventHandler;
};

class ListenerSession {
    private lastHandlerId = -1;

    constructor(
        private readonly id: number,
        private readonly eventListener: GraphEventListener,
        private readonly cytoscape: Core,
    ) {}

    private eventHandlers: Map<number, EventHandlerObject> = new Map();

    close() {
        [ ...this.eventHandlers.keys() ].forEach(handler => this.removeEventHandler(handler));
        this.eventListener.onSessionClose(this.id);
    }

    private createEventHandler(event: string, handler: EventHandler, selector?: string): number {
        this.lastHandlerId++;
        this.eventHandlers.set(this.lastHandlerId, { event, handler });
        if (selector)
            this.cytoscape.on(event, selector, handler);
        else
            this.cytoscape.on(event, handler);

        return this.lastHandlerId;
    }

    private removeEventHandler(handlerId: number) {
        const handler = this.eventHandlers.get(handlerId);
        if (handler) {
            this.cytoscape.off(handler.event, handler.handler);
            this.eventHandlers.delete(handlerId);
        }
    }

    onNode(event: string, handler: NodeEventFunction): number {
        const innerHandler = (event: EventObject) => {
            const node = (event.target as NodeSingular).data('schemaData') as Node;
            handler(node);
        };

        return this.createEventHandler(event, innerHandler, 'node');
    }

    onEdge(event: string, handler: EdgeEventFunction): number {
        const innerHandler = (event: EventObject) => {
            const edge = (event.target as EdgeSingular).data('schemaData') as Edge;
            handler(edge);
        };

        return this.createEventHandler(event, innerHandler, 'edge');
    }


    onCanvas(event: string, handler: CanvasEventFunction): number {
        const innerHandler = (event: EventObject) => {
            if (event.target === this.cytoscape)
                handler();
        };

        return this.createEventHandler(event, innerHandler);
    }

    off(handlerId: number) {
        this.removeEventHandler(handlerId);
    }
}
