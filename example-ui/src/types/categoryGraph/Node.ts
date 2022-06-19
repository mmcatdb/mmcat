import { ComparableMap } from "@/utils/ComparableMap";
import { TwoWayComparableMap } from "@/utils/TwoWayComparableMap";
import type { NodeSingular } from "cytoscape";
import type { SchemaId, Signature } from "../identifiers";
import type { SchemaObject } from "../schema";
import type { Edge } from "./Edge";
import { PathMarker, type Filter, type MorphismData } from "./PathMarker";

export enum NodeTag {
    Root = 'tag-root'
}

export enum AvailabilityStatus {
    Default = 'availability-default',
    Available = 'availability-available',
    CertainlyAvailable = 'availability-certainly-available',
    Maybe = 'availability-maybe',
    Removable = 'availability-removable',
    NotAvailable = 'availability-not-available'
}

export enum SelectionType {
    Default = 'selection-default',
    Root = 'selection-root',
    Selected = 'selection-selected'
}

export type SelectionStatus = {
    type: SelectionType,
    level: number
};

const defaultSelectionStatus = {
    type: SelectionType.Default,
    level: 0
};

function getStatusClass(status: SelectionStatus) {
    return status.type;
}

export enum PropertyType {
    Simple = 'Simple',
    Complex = 'Complex'
}

export class Node {
    schemaObject: SchemaObject;
    node!: NodeSingular;
    _tags = new Set() as Set<NodeTag>;
    availablePathData = null as MorphismData | null;

    neighbours = new TwoWayComparableMap<Node, number, Edge, string>(
        node => node.schemaObject.key.value,
        edge => edge.schemaMorphism.signature.toString()
    );
    _signatureToEdge = new ComparableMap<Signature, string, Edge>(signature => signature.toString());

    constructor(schemaObject: SchemaObject) {
        this.schemaObject = schemaObject;
    }

    setCytoscapeNode(node: NodeSingular) {
        this.node = node;
        node.toggleClass('no-ids', this.schemaObject.schemaIds.length === 0);
    }

    addNeighbour(node: Node, edge: Edge): void {
        this.neighbours.set(node, edge);
        this._signatureToEdge.set(edge.schemaMorphism.signature, edge);
    }

    removeNeighbour(node: Node): void {
        const edge = this.neighbours.get(node);
        this.neighbours.delete(node);
        if (edge)
            this._signatureToEdge.delete(edge.schemaMorphism.signature);
    }

    getNeighbour(signature: Signature): Node | undefined {
        if (signature.isEmpty)
            return this;

        const split = signature.getFirstBase();
        if (!split)
            return undefined;

        const edge = this._signatureToEdge.get(split.first);
        if (!edge)
            return undefined;

        const nextNeighbour = this.neighbours.getKey(edge);
        return !nextNeighbour ? undefined : split.rest.isEmpty ? nextNeighbour : nextNeighbour.getNeighbour(split.rest);
    }

    get determinedPropertyType(): PropertyType | null {
        return this.schemaObject.canBeSimpleProperty ? null : PropertyType.Complex;
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

    equals(other: Node | null): boolean {
        return !!other && this.schemaObject.id === other.schemaObject.id;
    }

    markAvailablePaths(filters: Filter | Filter[]): void {
        const pathMarker = new PathMarker(this, filters);
        pathMarker.markPathsFromRootNode();
    }

    addSchemaId(schemaId: SchemaId): void {
        this.schemaObject.addSchemaId(schemaId);
        this.node.removeClass('no-ids');
    }
}

