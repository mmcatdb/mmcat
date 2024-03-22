import type { Core, EdgeSingular, EventHandler, EventObject, LayoutOptions, NodeSingular } from 'cytoscape';
import type { ComparablePosition, GroupData, SchemaMorphism, SchemaObject } from '../schema';
import { Edge } from './Edge';
import { Node } from './Node';
import type { Key, Signature } from '../identifiers';
import { ComparableMap } from '@/utils/ComparableMap';
import type { Id } from '../id';
import { shallowRef } from 'vue';

export type TemporaryEdge = {
    delete: () => void;
};

export class Graph {
    private readonly nodes: ComparableMap<Key, number, Node> = new ComparableMap(key => key.value);
    private readonly edges: ComparableMap<Signature, string, Edge> = new ComparableMap(signature => signature.value);
    private readonly eventListener: GraphEventListener;
    public readonly highlights: GraphHighlights;

    constructor(
        private readonly cytoscape: Core,
    ) {
        this.eventListener = new GraphEventListener(cytoscape);
        this.highlights = new GraphHighlights({ nodes: this.nodes, edges: this.edges, cytoscape });
    }

    public resetElements(groupsData: GroupData[]): void {
        this.cytoscape.elements().remove();
        this.nodes.clear();
        this.edges.clear();

        this.highlights.reset(groupsData);
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

    createNode(object: SchemaObject, position: ComparablePosition, groupIds: string[]): Node {
        const nodeGroups = groupIds.map(groupId => this.highlights.getOrCreateGroup(groupId));
        const node = Node.create(this.cytoscape, object, position, nodeGroups);
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
        // TODO might not be true anymore.
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

    /** Workaround for some cytoscape BS. */
    resetLayout() {
        this.nodes.forEach(node => node.isFixed = false);

        // A necessary workaround for the bug with nodes without placeholders. More below.
        // Also, both parts of the workaround DO HAVE to be outside the layout function. Otherwise it causes a particularly hard to find bug (when the layout function is called from AddObject, then a new morphism is added in AddMorphism and then this function is called).
        this.highlights.resetLayout(false);

        this.layout();

        // A continuation of the workaround.
        this.highlights.resetLayout(true);
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

type GraphControl = {
    nodes: ComparableMap<Key, number, Node>;
    edges: ComparableMap<Signature, string, Edge>;
    cytoscape: Core;
};

export type Group = GroupData & {
    node: NodeSingular;
};

export type GraphHighlightState = {
    groupId: string;
    mappingIds?: Id[];
} | undefined;

class GraphHighlights {
    private readonly _groups: Map<string, Group> = new Map();
    /** Reactive accessor */
    readonly groups = shallowRef<Group[]>([]);
    private availableGroups: GroupData[] = [];

    constructor(
        private readonly control: GraphControl,
    ) {
        control.cytoscape.on('tap', 'node', (event: EventObject) => {
            const node = (event.target as NodeSingular).data('schemaData') as Node;
            this.clickNode(node);
        });
    }

    reset(availableGroups: GroupData[]) {
        this.availableGroups = availableGroups;
        this._groups.clear();
        this.groups.value = [];
    }

    // API

    getOrCreateGroup(groupId: string): Group {
        const group = this._groups.get(groupId);
        if (group)
            return group;

        const groupData = this.availableGroups.find(group => group.id === groupId);
        if (!groupData)
            throw new Error('Group not found: ' + groupId);

        const id = groupData.id;

        const newGroup = {
            ...groupData,
            node: this.control.cytoscape.add({
                data: {
                    id: 'group_' + id,
                    label: groupData.logicalModel.label,
                },
                classes: 'group ' + 'group-' + id,
            }),
        };

        this._groups.set(groupId, newGroup);
        this.groups.value = [ ...this._groups.values() ];

        return newGroup;
    }

    clickGroup(groupId: string): GraphHighlightState {
        if (this.state?.groupId === groupId)
            this.setState(undefined);
        else
            this.setState({ groupId });

        return this.state;
    }

    clickNode(node: Node): GraphHighlightState {
        if (!this.state)
            return;

        const mappings = this._groups.get(this.state.groupId)?.mappings.filter(mapping => mapping.root.key.equals(node.schemaObject.key));
        if (!mappings || mappings.length === 0)
            // The node is not a root of any mapping in the current active group.
            return;

        if (this.state.mappingIds?.includes(mappings[0].mapping.id))
            // Deactivate the current mapping.
            this.setState({ groupId: this.state.groupId });
        else
            // Activate a new mapping.
            this.setState({ groupId: this.state.groupId, mappingIds: mappings.map(m => m.mapping.id) });
    }

    // Inner state logic

    private state: GraphHighlightState = undefined;

    private setState(newState: GraphHighlightState) {
        this.toggleState(this.state, false);
        this.toggleState(newState, true);
        this.state = newState;
    }

    private toggleState(state: GraphHighlightState, value: boolean) {
        if (!state)
            return;

        if (!state.mappingIds) {
            this.toggleGroup(state.groupId, value);
            return;
        }

        state.mappingIds.map(id => this.toggleMapping(state.groupId, id, value));
    }

    private toggleGroup(groupId: string, value: boolean) {
        const group = this._groups.get(groupId);
        if (!group)
            return;

        console.log(group);

        group.mappings.forEach(mapping => {
            if (!mapping.root)
                return;

            this.control.nodes.get(mapping.root.key)?.highlights.select(group.id, 'root', value);
        });
    }

    private toggleMapping(groupId: string, mappingId: Id, value: boolean) {
        const mapping = this._groups.get(groupId)?.mappings.find(mapping => mapping.mapping.id === mappingId);
        if (!mapping)
            return;

        this.control.nodes.get(mapping.root.key)?.highlights.select(groupId, 'root', value);
        mapping.properties.forEach(property => this.control.nodes.get(property.key)?.highlights.select(groupId, 'property', value));
    }

    /** Workaround for some cytoscape BS. */
    resetLayout(isFirst: boolean) {
        if (isFirst) {
            this._groups.forEach(group => group.node.remove());
        }
        else {
            this._groups.forEach(group => group.node.restore());
            this.control.nodes.forEach(node => node.refreshGroupPlaceholders());
        }
    }
}
