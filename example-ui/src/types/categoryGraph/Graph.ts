import type { Core, EventHandler, EventObject, NodeSingular } from "cytoscape";
import type { SchemaCategory } from "../schema";
import type { Node } from "./Node";

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
}
