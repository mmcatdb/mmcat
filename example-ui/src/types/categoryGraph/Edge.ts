import type { EdgeSingular } from "cytoscape";
import type { SchemaMorphism } from "../schema";
import type { Node } from "./Node";

export class Edge {
    schemaMorphism: SchemaMorphism;
    edge?: EdgeSingular;
    domainNode: Node;
    codomainNode: Node;
    _dual!: Edge;

    // This is important for the pathMarker algorithm.
    isTraversible = false;

    constructor(schemaMorphism: SchemaMorphism, domainNode: Node, codomainNode: Node) {
        this.schemaMorphism = schemaMorphism;
        this.domainNode = domainNode;
        this.codomainNode = codomainNode;
    }

    setCytoscapeEdge(edge: EdgeSingular) {
        this.edge = edge;
    }

    get dual(): Edge {
        return this._dual;
    }

    set dual(edge: Edge) {
        this._dual = edge;
    }

    get label(): string {
        return this.schemaMorphism.signature +
            (this.schemaMorphism.label === '' ? '' : ' - ' + this.schemaMorphism.label) +
            (this.schemaMorphism.tags.length === 0 ? '' : ' - ' + this.schemaMorphism.tags.map(tag => '#' + tag).join(' '));
    }

    equals(other: Edge | null | undefined): boolean {
        return !!other && this.schemaMorphism.id === other.schemaMorphism.id;
    }

    unselect(): void {
        this.domainNode.unselect();
        this.codomainNode.unselect();
    }
}
