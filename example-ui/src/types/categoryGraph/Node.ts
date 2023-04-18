import { ComparableMap } from "@/utils/ComparableMap";
import type { NodeSingular } from "cytoscape";
import type { SignatureId, Signature, NonSignaturesType } from "../identifiers";
import type { SchemaObject } from "../schema";
import { DirectedEdge, type Edge } from "./Edge";
import { PathMarker, type MorphismData, type Filter } from "./PathMarker";

export enum NodeTag {
    Root = 'tag-root'
}

export enum AvailabilityStatus {
    Default = 'availability-default',
    Available = 'availability-available',
    CertainlyAvailable = 'availability-certainly-available',
    Ambiguous = 'availability-ambiguous',
    Removable = 'availability-removable',
    NotAvailable = 'availability-not-available'
}

export enum SelectionType {
    Default = 'selection-default',
    Root = 'selection-root',
    Selected = 'selection-selected'
}

export type SelectionStatus = {
    type: SelectionType;
    level: number;
};

const defaultSelectionStatus = {
    type: SelectionType.Default,
    level: 0,
};

function getStatusClass(status: SelectionStatus) {
    return status.type;
}

export enum PropertyType {
    Simple = 'Simple',
    Complex = 'Complex'
}

export class Neighbour {
    constructor(
        readonly edge: Edge,
        readonly node: Node,
        readonly signature: Signature,
    ) {}

    get direction(): boolean {
        return !this.signature.isBaseDual;
    }

    toDirectedEdge(sourceNode: Node): DirectedEdge {
        return new DirectedEdge(
            this.edge,
            this.signature,
            sourceNode,
            this.node,
        );
    }
}

export class Node {
    schemaObject: SchemaObject;
    node!: NodeSingular;
    _tags = new Set() as Set<NodeTag>;
    availablePathData = null as MorphismData | null;

    _neighbours = new ComparableMap<Signature, string, Neighbour>(signature => signature.toString());

    _groupPlaceholders: NodeSingular[];
    _noGroupPlaceholder = undefined as NodeSingular | undefined;

    constructor(schemaObject: SchemaObject, groupPlaceholders: NodeSingular[], noGroupPlaceholder?: NodeSingular) {
        this.schemaObject = schemaObject;
        this._groupPlaceholders = groupPlaceholders;
        this._noGroupPlaceholder = noGroupPlaceholder;
    }

    remove() {
        this._noGroupPlaceholder?.remove();
        this._groupPlaceholders.forEach(placeholder => placeholder.remove());
        this.node.remove();
    }

    refreshGroupPlaceholders() {
        this._groupPlaceholders.forEach(placeholder => {
            placeholder.remove();
            placeholder.restore();
        });
    }

    setCytoscapeNode(node: NodeSingular) {
        this.node = node;
        this._updateNoIdsClass();
    }

    addNeighbour(edge: Edge, direction: boolean): void {
        const thisNode = direction ? edge.domainNode : edge.codomainNode;
        if (!thisNode.equals(this))
            throw new Error(`Cannot add edge with signature: ${edge.schemaMorphism.signature} and direction: ${direction} to node with key: ${this.schemaObject.key}`);

        const node = direction ? edge.codomainNode : edge.domainNode;

        const edgeSignature = edge.schemaMorphism.signature;
        const signature = direction ? edgeSignature : edgeSignature.dual();

        const neighbour = new Neighbour(edge, node, signature);
        this._neighbours.set(signature, neighbour);
    }

    removeNeighbour(node: Node): void {
        [ ...this._neighbours.values() ]
            .filter(neighbour => neighbour.node.equals(node))
            .forEach(neighbour => this._neighbours.delete(neighbour.signature));
    }

    getNeighbourNode(signature: Signature): Node | undefined {
        if (signature.isEmpty)
            return this;

        const split = signature.getFirstBase();
        if (!split)
            return undefined;

        const neighbour = this._neighbours.get(split.first);
        if (!neighbour)
            return undefined;

        return split.rest.isEmpty ? neighbour.node : neighbour.node.getNeighbourNode(split.rest);
    }

    get neighbours(): Neighbour[] {
        return [ ...this._neighbours.values() ];
    }

    get determinedPropertyType(): PropertyType | null {
        if (!this.schemaObject.ids)
            return null;

        return this.schemaObject.ids.isSignatures ? PropertyType.Complex : null;
    }

    _availabilityStatus = AvailabilityStatus.Default;
    _selectionStatus = defaultSelectionStatus;

    get availabilityStatus(): AvailabilityStatus {
        return this._availabilityStatus;
    }

    get label(): string {
        return this.schemaObject.label + (
            this._selectionStatus.type === SelectionType.Selected ?
                ` (${this._selectionStatus.level + 1})` :
                ''
        );
    }

    select(status: SelectionStatus): void {
        this.node.removeClass(getStatusClass(this._selectionStatus));
        this._selectionStatus = status;
        this.node.addClass(getStatusClass(status));
        this.node.css({ content: this.label });
    }

    selectNext(): void {
        const newLevel = this._selectionStatus.type === SelectionType.Selected ? this._selectionStatus.level + 1 : 0;
        this.select({ type: SelectionType.Selected, level: newLevel });
    }

    unselect(): void {
        this.select(defaultSelectionStatus);
    }

    unselectPrevious(): void {
        const supposedNewLevel = this._selectionStatus.type === SelectionType.Selected ? this._selectionStatus.level - 1 : -1;
        const newType = supposedNewLevel >= 0 ? SelectionType.Selected : SelectionType.Default;
        const newLevel = Math.max(supposedNewLevel, 0);
        this.select({ type: newType, level: newLevel });
    }

    setAvailabilityStatus(status: AvailabilityStatus): void {
        this.node.removeClass(this._availabilityStatus);
        this._availabilityStatus = status;
        this.node.addClass(status);
    }

    resetAvailabilityStatus(): void {
        if (this._availabilityStatus !== AvailabilityStatus.Default) {
            this.node.removeClass(this._availabilityStatus);
            this._availabilityStatus = AvailabilityStatus.Default;
        }
    }

    becomeRoot(): void {
        this._tags.add(NodeTag.Root);
        this.node.addClass(NodeTag.Root);
    }

    equals(other: Node | null | undefined): boolean {
        return !!other && this.schemaObject.equals(other.schemaObject);
    }

    markAvailablePaths(filter: Filter): void {
        const pathMarker = new PathMarker(this, filter);
        pathMarker.markPathsFromRootNode();
    }

    _updateNoIdsClass(): void {
        this.node.toggleClass('no-ids', !this.schemaObject.ids);
    }

    addSignatureId(signatureId: SignatureId): void {
        this.schemaObject.addSignatureId(signatureId);
        this._updateNoIdsClass();
    }

    addNonSignatureId(type: NonSignaturesType): void {
        this.schemaObject.addNonSignatureId(type);
        this._updateNoIdsClass();
    }

    deleteSignatureId(index: number): void {
        this.schemaObject.deleteSignatureId(index);
        this._updateNoIdsClass();
    }

    deleteNonSignatureId() {
        this.schemaObject.deleteNonSignatureId();
        this._updateNoIdsClass();
    }
}

