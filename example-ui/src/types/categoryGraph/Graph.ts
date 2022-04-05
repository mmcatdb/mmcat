import type { Core, EventHandler, EventObject, NodeSingular } from "cytoscape";
import { Signature } from "../identifiers";
import type { SchemaCategory, SchemaMorphism } from "../schema";
import type { Node } from "./Node";

export type NodeEventFunction = (node: Node) => void;

export class Graph {
    private cytoscape: Core;
    public readonly schemaCategory: SchemaCategory;

    public constructor(cytoscape: Core, schemaCategory: SchemaCategory) {
        this.cytoscape = cytoscape;
        this.schemaCategory = schemaCategory;
    }

    private handlers = new Map() as Map<NodeEventFunction, EventHandler>;

    public addNodeListener(event: string, handler: NodeEventFunction) {
        const innerHandler = (event: EventObject) => {
            const node = (event.target as NodeSingular).data('schemaData') as Node;
            handler(node);
        };

        this.cytoscape.addListener(event, 'node', innerHandler);
        this.handlers.set(handler, innerHandler);
    }

    public removeListener(event: string, handler: NodeEventFunction) {
        const innerHandler = this.handlers.get(handler);
        if (innerHandler)
            this.cytoscape.removeListener(event, innerHandler);
    }

    public resetAvailabilityStatus(): void {
        this.cytoscape.nodes().forEach(node => (node.data('schemaData') as Node).resetAvailabilityStatus());
    }
}
