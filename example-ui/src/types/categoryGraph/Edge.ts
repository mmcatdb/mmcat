import type { EdgeSingular } from "cytoscape";
import type { SchemaMorphism } from "../schema";
import type { Node } from "./Node";
import type { Signature } from "../identifiers";

export class DirectedEdge {
    constructor(
        public readonly raw: Edge,
        public readonly signature: Signature,
        public readonly sourceNode: Node,
        public readonly targetNode: Node,
    ) {}

    get direction(): boolean {
        return !this.signature.isBaseDual;
    }

    get isTraversible(): boolean {
        return this.raw.isTraversible(this.direction);
    }

    equals(other: DirectedEdge | null | undefined): boolean {
        return !!other && this.signature.equals(other.signature);
    }
}

export class Edge {
    schemaMorphism: SchemaMorphism;
    edge?: EdgeSingular;
    domainNode: Node;
    codomainNode: Node;

    // This is important for the pathMarker algorithm.
    _isTraversible = false;
    _isTraversibleDual = false;

    isTraversible(direction: boolean): boolean {
        return direction ? this._isTraversible : this._isTraversibleDual;
    }

    setTraversible(direction: boolean, value: boolean): void {
        if (direction)
            this._isTraversible = value;
        else
            this._isTraversibleDual = value;
    }

    constructor(schemaMorphism: SchemaMorphism, domainNode: Node, codomainNode: Node) {
        this.schemaMorphism = schemaMorphism;
        this.domainNode = domainNode;
        this.codomainNode = codomainNode;
    }

    setCytoscapeEdge(edge: EdgeSingular) {
        this.edge = edge;
    }

    get label(): string {
        return this.schemaMorphism.signature +
            (this.schemaMorphism.label === '' ? '' : ' - ' + this.schemaMorphism.label) +
            (this.schemaMorphism.tags.length === 0 ? '' : ' - ' + this.schemaMorphism.tags.map(tag => '#' + tag).join(' '));
    }

    getSourceNode(direction: boolean): Node {
        return direction ? this.domainNode : this.codomainNode;
    }

    getTargetNode(direction: boolean): Node {
        return direction ? this.codomainNode : this.domainNode;
    }

    equals(other: Edge | null | undefined): boolean {
        return !!other && this.schemaMorphism.equals(other.schemaMorphism);
    }

    unselect(): void {
        this.domainNode.unselect();
        this.codomainNode.unselect();
    }

    toDirectedEdge(direction: boolean): DirectedEdge {
        const signature = this.schemaMorphism.signature;

        return new DirectedEdge(
            this,
            direction ? signature : signature.dual(),
            direction ? this.domainNode : this.codomainNode,
            direction ? this.codomainNode : this.domainNode,
        );
    }
}
