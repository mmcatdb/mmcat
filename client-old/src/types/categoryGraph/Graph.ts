import type { Core, EdgeSingular, EventHandler, EventObject, LayoutOptions, NodeSingular, Position } from 'cytoscape';
import type { GroupData, SchemaMorphism, SchemaObject, VersionedSchemaMorphism, VersionedSchemaObject } from '../schema';
import { Edge } from './Edge';
import { Node } from './Node';
import type { Key, Signature } from '../identifiers';
import { ComparableMap } from '@/utils/ComparableMap';
import type { Id } from '../id';
import { shallowRef } from 'vue';
import { SequenceSignature } from '../accessPath/graph';

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

    /// functions for Mapping editor
    public getChildrenForNode(node: Node): Node[] {
        const outgoingEdges = Array.from(this.edges.values()).filter(edge => edge.domainNode.equals(node));
        return outgoingEdges.map(edge => edge.codomainNode);
    }

    public getSignature(node: Node, parentNode: Node): SequenceSignature {
        const edge = Array.from(this.edges.values())
            .find(edge =>
                ((edge.domainNode.equals(parentNode) && edge.codomainNode.equals(node)) ||
                (edge.domainNode.equals(node) && edge.codomainNode.equals(parentNode))),
            );

        if (!edge) {
            console.warn(`No edge found between parent ${parentNode.schemaObject.key.value} and node ${node.schemaObject.key.value}`);
            return SequenceSignature.empty(node);
        }

        if (edge.domainNode.equals(node))
            return SequenceSignature.fromSignature(edge.schemaMorphism.signature.dual(), parentNode);
        else
            return SequenceSignature.fromSignature(edge.schemaMorphism.signature, parentNode);
    }

    public getEdges(node: Node): Edge[] {
        return Array.from(this.edges.values())
            .filter(edge =>
                edge.domainNode.equals(node) || edge.codomainNode.equals(node),
            );
    }

    public getParentNode(node: Node): Node | undefined {
        const incomingEdges = Array.from(this.edges.values()).filter(edge => edge.codomainNode.equals(node));

        if (incomingEdges.length === 0) {
            console.warn('No incoming edges found for node:', node);
            return undefined;
        }

        return incomingEdges[0].domainNode;
    }
    ///

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

    createNode(object: VersionedSchemaObject, schemaObject: SchemaObject, position: Position, groupIds: string[]): Node {
        const nodeGroups = groupIds.map(groupId => this.highlights.getOrCreateGroup(groupId));
        const node = Node.create(this.cytoscape, object, schemaObject, position, nodeGroups);
        this.nodes.set(schemaObject.key, node);

        return node;
    }

    deleteNode(object: SchemaObject) {
        const node = this.nodes.get(object.key);
        if (!node)
            return;

        node.remove();
        this.nodes.delete(object.key);

        // Only the newly created nodes can be deleted an those can't be in any datasource so we don't have to remove their datasource placeholders.
        // TODO might not be true anymore.
    }

    createEdge(morphism: VersionedSchemaMorphism, schemaMorphism: SchemaMorphism): Edge {
        const dom = this.nodes.get(schemaMorphism.domKey) as Node;
        const cod = this.nodes.get(schemaMorphism.codKey) as Node;

        const edge = Edge.create(this.cytoscape, morphism, schemaMorphism, dom, cod);
        this.edges.set(schemaMorphism.signature, edge);

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

    public toggleEdgeLabels(show: boolean): void {
        const labelStyle = show ? { 'text-opacity': 1 } : { 'text-opacity': 0 };

        this.cytoscape.edges().forEach(edge => {
            const currentLabel = edge.data('label');
            edge.style({
                'label': currentLabel,
                ...labelStyle,
            });
        });
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

    closeSession(sessionId: number) {
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

export class ListenerSession {
    private lastHandlerId = -1;

    constructor(
        private readonly id: number,
        private readonly eventListener: GraphEventListener,
        private readonly cytoscape: Core,
    ) {}

    private eventHandlers: Map<number, EventHandlerObject> = new Map();

    close() {
        [ ...this.eventHandlers.keys() ].forEach(handler => this.removeEventHandler(handler));
        this.eventListener.closeSession(this.id);
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
                    label: groupData.logicalModel.datasource.label,
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
