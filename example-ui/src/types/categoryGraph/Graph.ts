import type { Core, EventHandler, EventObject, NodeSingular } from "cytoscape";
import type { SchemaMorphism, SchemaObject, SchemaCategory } from "../schema";
import { Node } from "./Node";

export type NodeEventFunction = (node: Node) => void;

export class Graph {
    _cytoscape: Core;
    readonly schemaCategory: SchemaCategory;

    constructor(cytoscape: Core, schemaCategory: SchemaCategory) {
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

        this._cytoscape.add({
            data: {
                id: object.id.toString(),
                label: object.label,
                schemaData: node
            },
            position: object.position
        });

        node.setCytoscapeNode(this._cytoscape.nodes('#' + node.schemaObject.id).first());
    }

    createEdge(morphism: SchemaMorphism, dualMorphism: SchemaMorphism): void {
        this._cytoscape.add({
            data: {
                id: 'm' + morphism.id.toString(),
                source: morphism.domId,
                target: morphism.codId,
                label: ((value: string) => value.startsWith('-') ? undefined : value )(morphism.signature.toString())
            }
        });

        const domNode = this._cytoscape.nodes('#' + morphism.domId).first().data('schemaData') as Node;
        const codNode = this._cytoscape.nodes('#' + morphism.codId).first().data('schemaData') as Node;

        domNode.addNeighbour(codNode, morphism);
        codNode.addNeighbour(domNode, dualMorphism);
    }
}
