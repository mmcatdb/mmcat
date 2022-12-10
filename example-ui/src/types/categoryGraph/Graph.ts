import type { Core, EdgeSingular, ElementDefinition, EventHandler, EventObject, NodeSingular } from "cytoscape";
import type { DatabaseWithConfiguration } from "../database";
import type { SchemaMorphism, SchemaObject, SchemaCategory } from "../schema";
import { Edge } from "./Edge";
import { Node } from "./Node";

export type NodeEventFunction = (node: Node) => void;
export type EdgeEventFunction = (edge: Edge) => void;

export type TemporaryEdge = {
    delete: () => void;
}

type Group = { id: number, database: DatabaseWithConfiguration, node: NodeSingular };

export class Graph {
    _cytoscape: Core;
    _nodes = [] as Node[];
    // How many nodes have fixed positions.
    _fixedNodes = 0;
    readonly schemaCategory: SchemaCategory;

    constructor(cytoscape: Core, schemaCategory: SchemaCategory) {
        this._cytoscape = cytoscape;
        this.schemaCategory = schemaCategory;
    }

    _nodeHandlers = new Map() as Map<NodeEventFunction, EventHandler>;
    _edgeHandlers = new Map() as Map<EdgeEventFunction, EventHandler>;

    addNodeListener(event: string, handler: NodeEventFunction) {
        const innerHandler = (event: EventObject) => {
            const node = (event.target as NodeSingular).data('schemaData') as Node;
            handler(node);
        };

        this._cytoscape.addListener(event, 'node', innerHandler);
        this._nodeHandlers.set(handler, innerHandler);
    }

    removeNodeListener(event: string, handler: NodeEventFunction) {
        const innerHandler = this._nodeHandlers.get(handler);
        if (innerHandler)
            this._cytoscape.removeListener(event, innerHandler);
    }

    addEdgeListener(event: string, handler: EdgeEventFunction) {
        const innerHandler = (event: EventObject) => {
            const edge = (event.target as EdgeSingular).data('schemaData') as Edge;
            handler(edge);
        };

        this._cytoscape.addListener(event, 'edge', innerHandler);
        this._edgeHandlers.set(handler, innerHandler);
    }

    removeEdgeListener(event: string, handler: EdgeEventFunction) {
        const innerHandler = this._edgeHandlers.get(handler);
        if (innerHandler)
            this._cytoscape.removeListener(event, innerHandler);
    }

    resetAvailabilityStatus(): void {
        this._nodes.forEach(node => node.resetAvailabilityStatus());
    }

    groups = [] as Group[];

    getGroupOrAddIt(database: DatabaseWithConfiguration): Group {
        const results = this.groups.filter(group => group.database.id === database.id);
        if (results[0])
            return results[0];

        const id = this.groups.length + 1;
        const newGroup = {
            id,
            database,
            node: this._cytoscape.add({
                data: {
                    id: 'group_' + id,
                    label: database.label
                },
                classes: 'group ' + 'group-' + id
            })
        };

        this.groups.push(newGroup);
        return newGroup;
    }

    createNode(object: SchemaObject, classes?: string): Node {
        const node = new Node(object);
        this._nodes.push(node);

        const groupObjects = object.databases.map(databaseId => this.getGroupOrAddIt(databaseId));

        const groupPlaceholders = [] as NodeSingular[];
        groupObjects.forEach(group => groupPlaceholders.push(this._cytoscape.add(createGroupPlaceholderDefinition(object, group.id))));

        if (groupObjects.length === 0)
            node.noGroupPlaceholder = this._cytoscape.add(createNoGroupDefinition(object));

        //const coloringNode = this._cytoscape.add(createColoringNodeDefinition(object, Math.random() < 0.5 ? Type.mongodb : Type.postgresql));
        const cytoscapeNode = this._cytoscape.add(createNodeDefinition(object, node, classes));
        node.setCytoscapeNode(cytoscapeNode);

        //cytoscapeNode.json();

        cytoscapeNode.on('drag', () => {
            groupPlaceholders.forEach(placeholder => {
                placeholder.remove();
                placeholder.restore();
            });
        });

        return node;
    }

    deleteNode(node: Node) {
        this._cytoscape.remove(node.node);
        this._nodes = this._nodes.filter(n => !n.equals(node));

        // Only the newly created nodes can be deleted an those can't be in any database so we don't have to remove their database placeholders.
        // However, the no group placeholder has to be removed.
        if (node.noGroupPlaceholder)
            this._cytoscape.remove(node.noGroupPlaceholder);
    }

    createEdgeWithDual(morphism: SchemaMorphism, classes?: string): void {
        const domNode = this._nodes.find(node => node.schemaObject.id === morphism.domId) as Node;
        const codNode = this._nodes.find(node => node.schemaObject.id === morphism.codId) as Node;

        const edges = [ new Edge(morphism, domNode, codNode), new Edge(morphism.dual, codNode, domNode) ];
        edges[0].dual = edges[1];
        edges[1].dual = edges[0];

        //const definitions = [ createEdgeDefinition(morphism, edges[0], classes), createEdgeDefinition(morphism.dual, edges[1], classes) ];

        // This ensures the Bezier morphism pairs have allways the same chirality.
        //const noSwitchNeeded = morphism.domId > morphism.codId;

        /*
        const cytoscapeEdges = this._cytoscape.add(noSwitchNeeded ? definitions : definitions.reverse());
        const orderedCytoscapeEdges = noSwitchNeeded ? cytoscapeEdges : cytoscapeEdges;
        edges[0].setCytoscapeEdge(orderedCytoscapeEdges[0]);
        edges[1].setCytoscapeEdge(orderedCytoscapeEdges[1]);
        */

        const definition = createEdgeDefinition(morphism, edges[0], classes);
        const cytoscapeEdge = this._cytoscape.add(definition);
        edges[0].setCytoscapeEdge(cytoscapeEdge);

        domNode.addNeighbour(edges[0]);
        codNode.addNeighbour(edges[1]);
    }

    deleteEdgeWithDual(edge: Edge) {
        //this._cytoscape.remove(edge.edge);
        //this._cytoscape.remove(edge.dual.edge);
        const cytoscapeEdge = edge.edge ? edge.edge : edge.dual.edge;
        this._cytoscape.remove(cytoscapeEdge);

        edge.domainNode.removeNeighbour(edge.codomainNode);
        edge.codomainNode.removeNeighbour(edge.domainNode);
    }

    _lastTemporaryEdgeId = 0;

    createTemporaryEdge(node1: Node, node2: Node): TemporaryEdge {
        const id = 'te' + this._lastTemporaryEdgeId;
        this._lastTemporaryEdgeId++;

        this._cytoscape.add({
            data: {
                id,
                source: node1.schemaObject.id,
                target: node2.schemaObject.id,
                label: ''
            },
            classes: 'temporary'
        });

        return { delete: () => this._cytoscape.remove('#' + id) };
    }

    center() {
        this._cytoscape.center();
    }

    layout() {
        this._cytoscape.layout({
            //name: 'dagre',
            //name: 'cola',
            name: 'fcose',
            animate: false,
            fixedNodeConstraint: this._nodes.slice(0, this._fixedNodes).map(node => ({
                nodeId: node.node.id(),
                position: node.node.position()
            })),
            //randomize: false,
            //quality: 'proof',
            nodeDimensionsIncludeLabels: true
            //boundingBox: { x1: 0, x2: 1000, y1: 0, y2: 500 }
        }).run();

        this.fixLayout();
    }

    fixLayout() {
        this._fixedNodes = this._nodes.length;
    }

    resetLayout() {
        this._fixedNodes = 0;
        this.layout();
    }

    getNode(object: SchemaObject): Node | undefined {
        return this._nodes.find(node => node.schemaObject.key.equals(object.key));
    }
}

function createNodeDefinition(object: SchemaObject, node: Node, classes?: string): ElementDefinition {
    return {
        data: {
            id: object.id.toString(),
            label: node.label,
            schemaData: node
        },
        position: object.position,
        ...classes ? { classes } : {}
    };
}

function createGroupPlaceholderDefinition(object: SchemaObject, groupId: number): ElementDefinition {
    return {
        data: {
            id: groupId + '_' + object.id.toString(),
            parent: 'group_' + groupId
        },
        position: object.position,
        classes: 'group-placeholder'
    };
}

function createNoGroupDefinition(object: SchemaObject): ElementDefinition {
    return {
        data: {
            id: 'no-group_' + object.id.toString()
        },
        position: object.position,
        classes: 'no-group'
    };
}

function createEdgeDefinition(morphism: SchemaMorphism, edge: Edge, classes?: string): ElementDefinition {
    return {
        data: {
            id: 'm' + morphism.id.toString(),
            source: morphism.domId,
            target: morphism.codId,
            label: edge.label,
            schemaData: edge
        },
        ...classes ? { classes } : {}
    };
}
