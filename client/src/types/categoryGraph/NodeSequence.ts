import { Signature } from '../identifiers';
import type { DirectedEdge, Edge } from './Edge';
import { AvailabilityStatus, type Node } from './Node';

type Config = {
    selectNodes: boolean;
};

const defaultConfig: Config = {
    selectNodes: true,
};

/** @deprecated */
export class NodeSequence {
    _nodes: Node[];
    _edges: DirectedEdge[];
    readonly config: Config;

    private constructor(nodes: Node[], edges: DirectedEdge[], config: Partial<Config>) {
        this._nodes = [ ...nodes ];
        this._edges = [ ...edges ];
        this.config = { ...defaultConfig, ...config };
    }

    copy(): NodeSequence {
        return new NodeSequence(this._nodes, this._edges, this.config);
    }

    static fromRootNode(rootNode: Node, config: Partial<Config> = {}): NodeSequence {
        const sequence = new NodeSequence([ rootNode ], [], config);
        return sequence;
    }

    get rootNode(): Node {
        return this._nodes[0];
    }

    get lastNode(): Node {
        return this._nodes[this._nodes.length - 1];
    }

    get lengthOfMorphisms(): number {
        return this._edges.length;
    }
    /*
    get allNodes(): Node[] {
        return this.nodes;
    }
    */
    addSignature(signature: Signature): void {
        signature.toBases().forEach(baseSignature => this.addBaseSignature(baseSignature));
    }

    addBaseSignature(baseSignature: Signature): boolean {
        for (const neighbor of this.lastNode.neighbors) {
            if (neighbor.signature.equals(baseSignature)) {
                this.addEdge(neighbor.toDirectedEdge(this.lastNode));
                return true;
            }
        }

        return false;
    }

    addEdge(edge: DirectedEdge): void {
        this._edges.push(edge);
        this._nodes.push(edge.targetNode);

        if (this.config.selectNodes)
            edge.targetNode.selectNext();
    }

    unselectAll(): void {
        this._nodes.forEach(node => node.unselect());
    }

    selectAll(): void {
        this.rootNode.unselect();
        this._nodes.forEach(node => node.selectNext());
    }

    tryAddNode(node: Node): boolean {
        /*
        const morphism = this.lastNode.neighbors.get(node);

        if (!morphism || this.nodes.find(o => o === node))
            return false;

        this.nodes.push(node);
        this.morphisms.push(morphism);
        */

        if (node.availabilityStatus !== AvailabilityStatus.Available && node.availabilityStatus !== AvailabilityStatus.CertainlyAvailable)
            return false;

        if (!node.availablePathData)
            return false;

        this.addSignature(node.availablePathData.signature);

        return true;
    }

    tryRemoveNode(node: Node): boolean {
        if (!this.lastNode.equals(node) || this._nodes.length === 1)
            return false;

        if (this.config.selectNodes)
            this.lastNode.unselectPrevious();

        this._nodes.pop();
        this._edges.pop();

        return true;
    }

    tryAddEdge(edge: Edge): boolean {
        const direction = edge.domainNode.equals(this.lastNode);

        if (!edge.getSourceNode(direction).equals(this.lastNode))
            return false;

        if (!edge.isTraversible(direction))
            return false;

        // The edge wasn't filtered out during the pathMarker algorithm so we can add it.
        this.addEdge(edge.toDirectedEdge(direction));
        return true;
    }

    toSignature(): Signature {
        let output = Signature.empty;
        this._edges.forEach(edge => output = output.concatenate(edge.signature));

        return output;
    }

    equals(sequence: NodeSequence): boolean {
        if (this.rootNode !== sequence.rootNode)
            return false;

        if (this._edges.length !== sequence._edges.length)
            return false;

        for (let i = 0; i < this._edges.length; i++) {
            if (!this._edges[i].equals(sequence._edges[i]))
                return false;
        }

        return true;
    }
}
