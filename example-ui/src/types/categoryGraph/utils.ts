import type { Core } from "cytoscape";
import type { NodeSchemaData } from "./NodeSchemaData";

export function resetAvailabilityStatus(cytoscape: Core): void {
    cytoscape.nodes().forEach(node => (node.data('schemaData') as NodeSchemaData).resetAvailabilityStatus());
}
