import type { Core, EdgeSingular, ElementDefinition, EventHandler, EventObject, LayoutOptions, NodeSingular } from "cytoscape";
import type { LogicalModel, LogicalModelInfo } from "../logicalModel";
import type { SchemaMorphism, SchemaObject } from "../schema";
import { Edge } from "./Edge";
import { Node } from "./Node";

export type TemporaryEdge = {
    delete: () => void;
};

type Group = {
    id: number;
    logicalModel: LogicalModelInfo;
    node: NodeSingular;
};

export class Graph {
    // Workaround for the vue reactivity (all properties are replaced by proxies, but this way, we can have access to the original Core)
    _nodes: Node[] = [];
    _edges: Edge[] = [];
    // How many nodes have fixed positions.
    _fixedNodes = 0;

    private eventListener: GraphEventListener;

    constructor(
        private readonly cytoscape: Core,
    ) {
        this.eventListener = new GraphEventListener(cytoscape);
    }

    // Workaround for Evocat - remove as soon as possible
    public getCytoscape(): Core {
        return this.cytoscape;
    }

    public reset(): void {
        this.cytoscape.elements().remove();

        this._nodes = [];
        this._edges = [];
        this._fixedNodes = 0;
        this.groups = [];

        this.eventListener.clear();
    }

    listen(): ListenerSession {
        return this.eventListener.openSession();
    }

    resetAvailabilityStatus(): void {
        this._nodes.forEach(node => node.resetAvailabilityStatus());
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

    createNode(object: SchemaObject): Node {
        const groupPlaceholders = object.logicalModels
            .map(logicalModel => this.getGroupOrAddIt(logicalModel))
            .map(group => this.cytoscape.add(createGroupPlaceholderDefinition(object, group.id)));

        const noGroupPlaceholder = groupPlaceholders.length > 0 ?
            undefined :
            this.cytoscape.add(createNoGroupDefinition(object));

        const node = new Node(object, groupPlaceholders, noGroupPlaceholder);
        this._nodes.push(node);

        const cytoscapeNode = this.cytoscape.add(createNodeDefinition(object, node, object.isNew ? 'new' : ''));
        node.setCytoscapeNode(cytoscapeNode);

        cytoscapeNode.on('drag', () => node.refreshGroupPlaceholders());

        return node;
    }

    deleteNode(object: SchemaObject) {
        const node = this.getNode(object);
        if (!node)
            return;

        node.remove();
        this._nodes = this._nodes.filter(n => !n.equals(node));

        // Only the newly created nodes can be deleted an those can't be in any database so we don't have to remove their database placeholders.
        // However, the no group placeholder has to be removed.
    }

    createEdge(morphism: SchemaMorphism): Edge {
        const domNode = this._nodes.find(node => node.schemaObject.key.equals(morphism.domKey)) as Node;
        const codNode = this._nodes.find(node => node.schemaObject.key.equals(morphism.codKey)) as Node;

        const edge = new Edge(morphism, domNode, codNode);
        this._edges.push(edge);


        // This ensures the Bezier morphism pairs have allways the same chirality.
        //const noSwitchNeeded = morphism.domId > morphism.codId;

        const definition = createEdgeDefinition(morphism, edge, morphism.isNew ? 'new' : '');
        const cytoscapeEdge = this.cytoscape.add(definition);
        edge.setCytoscapeEdge(cytoscapeEdge);

        domNode.addNeighbour(edge, true);
        codNode.addNeighbour(edge, false);

        return edge;
    }

    deleteEdge(morphism: SchemaMorphism) {
        const edge = this.getEdge(morphism);
        if (!edge)
            return;

        const cytoscapeEdge = edge.edge;
        if (cytoscapeEdge)
            this.cytoscape.remove(cytoscapeEdge);

        edge.domainNode.removeNeighbour(edge.codomainNode);
        edge.codomainNode.removeNeighbour(edge.domainNode);
        this._edges = this._edges.filter(e => !e.equals(edge));
    }

    _lastTemporaryEdgeId = 0;

    createTemporaryEdge(node1: Node, node2: Node): TemporaryEdge {
        const id = 'te' + this._lastTemporaryEdgeId;
        this._lastTemporaryEdgeId++;

        this.cytoscape.add({
            data: {
                id,
                source: node1.schemaObject.key.toString(),
                target: node2.schemaObject.key.toString(),
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
            fixedNodeConstraint: this._nodes.slice(0, this._fixedNodes).map(node => ({
                nodeId: node.node.id(),
                position: node.node.position(),
            })),
            //randomize: false,
            //quality: 'proof',
            nodeDimensionsIncludeLabels: true,
            //boundingBox: { x1: 0, x2: 1000, y1: 0, y2: 500 }
        } as LayoutOptions).run();

        this.fixLayout();
    }

    fixLayout() {
        this._fixedNodes = this._nodes.length;
    }

    resetLayout() {
        this._fixedNodes = 0;

        // A necessary workaround for the bug with nodes without placeholders. More below.
        // Also, both parts of the workaround DO HAVE to be outside the layout function. Otherwise it causes a particularly hard to find bug (when the layout function is called from AddObject, then a new morphism is added in AddMorphism and then this function is called).
        this.groups.forEach(group => group.node.remove());

        this.layout();

        // A continuation of the workaround.
        this.groups.forEach(group => group.node.restore());
        this._nodes.forEach(node => node.refreshGroupPlaceholders());
    }

    getNode(object: SchemaObject): Node | undefined {
        return this._nodes.find(node => node.schemaObject.equals(object));
    }

    getEdge(morphism: SchemaMorphism): Edge | undefined {
        return this._edges.find(edge => edge.schemaMorphism.signature.equals(morphism.signature));
    }
}

function createNodeDefinition(object: SchemaObject, node: Node, classes?: string): ElementDefinition {
    return {
        data: {
            id: object.key.toString(),
            label: node.label,
            schemaData: node,
        },
        position: object.position,
        ...classes ? { classes } : {},
    };
}

function createGroupPlaceholderDefinition(object: SchemaObject, groupId: number): ElementDefinition {
    return {
        data: {
            id: groupId + '_' + object.key.toString(),
            parent: 'group_' + groupId,
        },
        position: object.position,
        classes: 'group-placeholder',
    };
}

function createNoGroupDefinition(object: SchemaObject): ElementDefinition {
    return {
        data: {
            id: 'no-group_' + object.key.toString(),
        },
        position: object.position,
        classes: 'no-group',
    };
}

function createEdgeDefinition(morphism: SchemaMorphism, edge: Edge, classes = ''): ElementDefinition {
    return {
        data: {
            id: 'm' + morphism.signature.toString(),
            source: morphism.domKey.toString(),
            target: morphism.codKey.toString(),
            label: edge.label,
            schemaData: edge,
        },
        classes: classes + ' ' + morphism.tags.join(' '),
    };
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
