import type { Core, EventHandler, EventObject, NodeSingular } from "cytoscape";
import type { SchemaMorphism, SchemaObject, SchemaCategory } from "../schema";
import { Node } from "./Node";

export type NodeEventFunction = (node: Node) => void;

export type TemporaryEdge = {
    delete: () => void;
}

export class Graph {
    _cytoscape: Core;
    readonly schemaCategory: SchemaCategory;

    constructor(cytoscape: Core, schemaCategory: SchemaCategory) {
        console.log('NEW GRAPH CREATED', cytoscape);
        this._cytoscape = cytoscape;
        this.schemaCategory = schemaCategory;
    }

    _handlers = new Map() as Map<NodeEventFunction, EventHandler>;

    addNodeListener(event: string, handler: NodeEventFunction) {
        const innerHandler = (event: EventObject) => {
            const node = (event.target as NodeSingular).data('schemaData') as Node;
            handler(node);
        };

        this._cytoscape.addListener(event, 'node', innerHandler);
        this._handlers.set(handler, innerHandler);
    }

    removeListener(event: string, handler: NodeEventFunction) {
        const innerHandler = this._handlers.get(handler);
        if (innerHandler)
            this._cytoscape.removeListener(event, innerHandler);
    }

    resetAvailabilityStatus(): void {
        this._cytoscape.nodes().forEach(node => (node.data('schemaData') as Node).resetAvailabilityStatus());
    }

    createNode(object: SchemaObject): void {
        const node = new Node(object);

        const cytoscapeNode = this._cytoscape.add({
            data: {
                id: object.id.toString(),
                label: object.label,
                schemaData: node
            },
            position: object.position
        });
        cytoscapeNode.addClass('new');

        node.setCytoscapeNode(cytoscapeNode);
    }

    createEdge(morphism: SchemaMorphism, dualMorphism: SchemaMorphism): void {
        const edge = this._cytoscape.add({
            data: {
                id: 'm' + morphism.id.toString(),
                source: morphism.domId,
                target: morphism.codId,
                label: ((value: string) => value.startsWith('-') ? undefined : value )(morphism.signature.toString())
            }
        });
        edge.addClass('new');

        const domNode = this._cytoscape.nodes('#' + morphism.domId).first().data('schemaData') as Node;
        const codNode = this._cytoscape.nodes('#' + morphism.codId).first().data('schemaData') as Node;

        domNode.addNeighbour(codNode, morphism);
        codNode.addNeighbour(domNode, dualMorphism);
    }

    lastTemporaryEdgeId = 0;

    createTemporaryEdge(node1: Node, node2: Node): TemporaryEdge {
        const id = 'te' + this.lastTemporaryEdgeId;
        this.lastTemporaryEdgeId++;

        const edge = this._cytoscape.add({
            data: {
                id,
                source: node1.schemaObject.id,
                target: node2.schemaObject.id,
                label: ''
            }
        });
        edge.addClass('temporary');

        return { delete: () => edge.remove() };
    }
}
