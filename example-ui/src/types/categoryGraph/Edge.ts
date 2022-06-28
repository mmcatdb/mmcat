import type { EdgeSingular } from "cytoscape";
import type { SchemaMorphism } from "../schema";
import type { Node } from "./Node";

export class Edge {
    schemaMorphism: SchemaMorphism;
    edge!: EdgeSingular;
    domainNode: Node;
    codomainNode: Node;
    _dual!: Edge;

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

    equals(other: Edge | null): boolean {
        return !!other && this.schemaMorphism.id === other.schemaMorphism.id;
    }

    unselect(): void {
        this.domainNode.unselect();
        this.codomainNode.unselect();
    }
}
