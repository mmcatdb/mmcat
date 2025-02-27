import type { MetadataMorphism, SchemaMorphism, Morphism } from '../schema';
import type { Node } from './Node';
import type { Signature } from '../identifiers';

/** @deprecated */
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

    get isTraversable(): boolean {
        return this.raw.isTraversable(this.direction);
    }

    equals(other: DirectedEdge | null | undefined): boolean {
        return !!other && this.signature.equals(other.signature);
    }
}

/** @deprecated */
export class Edge {
    // This is important for the pathMarker algorithm.
    private _isTraversable = false;
    private _isTraversableDual = false;

    isTraversable(direction: boolean): boolean {
        return direction ? this._isTraversable : this._isTraversableDual;
    }

    setTraversable(direction: boolean, value: boolean): void {
        if (direction)
            this._isTraversable = value;
        else
            this._isTraversableDual = value;
    }

    private constructor(
        readonly morphism: Morphism,
        readonly schemaMorphism: SchemaMorphism,
        readonly domainNode: Node,
        readonly codomainNode: Node,
    ) {}

    get metadata(): MetadataMorphism {
        return this.morphism.metadata;
    }

    get label(): string {
        return this.schemaMorphism.signature +
            (this.metadata.label === '' ? '' : ' - ' + this.metadata.label) +
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
