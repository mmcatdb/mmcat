import type { Core, EdgeSingular, ElementDefinition } from 'cytoscape';
import type { SchemaMorphism } from '../schema';
import type { Node } from './Node';
import type { Signature } from '../identifiers';

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
    private edge!: EdgeSingular;

    // This is important for the pathMarker algorithm.
    private _isTraversible = false;
    private _isTraversibleDual = false;

    isTraversible(direction: boolean): boolean {
        return direction ? this._isTraversible : this._isTraversibleDual;
    }

    setTraversible(direction: boolean, value: boolean): void {
        if (direction)
            this._isTraversible = value;
        else
            this._isTraversibleDual = value;
    }

    private constructor(
        public schemaMorphism: SchemaMorphism,
        readonly domainNode: Node,
        readonly codomainNode: Node,
    ) {}

    static create(cytoscape: Core, morphism: SchemaMorphism, dom: Node, cod: Node): Edge {
        const edge = new Edge(morphism, dom, cod);
        const definition = createEdgeDefinition(morphism, edge, morphism.isNew ? 'new' : '');
        const cytoscapeEdge = cytoscape.add(definition);
        edge.setCytoscapeEdge(cytoscapeEdge);

        dom.addNeighbour(edge, true);
        cod.addNeighbour(edge, false);

        return edge;
    }

    private setCytoscapeEdge(edge: EdgeSingular) {
        this.edge = edge;
    }

    remove() {
        this.edge.remove();

        this.domainNode.removeNeighbour(this.codomainNode);
        this.codomainNode.removeNeighbour(this.domainNode);
    }

    update(schemaMorphism: SchemaMorphism) {
        this.schemaMorphism = schemaMorphism;
        this.edge.data('source', this.schemaMorphism.domKey.value);
        this.edge.data('target', this.schemaMorphism.codKey.value);
        this.edge.data('label', this.schemaMorphism.label);

        if (!this.edge.inside()) {
            this.edge.restore();
            this.domainNode.addNeighbour(this, true);
            this.codomainNode.addNeighbour(this, false);
        }
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

function createEdgeDefinition(morphism: SchemaMorphism, edge: Edge, classes = ''): ElementDefinition {
    return {
        data: {
            id: 'm' + morphism.signature.value,
            source: morphism.domKey.value,
            target: morphism.codKey.value,
            label: edge.label,
            schemaData: edge,
        },
        classes: classes + ' ' + morphism.tags.join(' '),
    };
}
