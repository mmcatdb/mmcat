import { ComparableMap } from "@/utils/ComparableMap";
import { TwoWayComparableMap } from "@/utils/TwoWayComparableMap";
import type { NodeSingular } from "cytoscape";
import { DatabaseConfiguration } from "../database";
import type { SchemaId, Signature } from "../identifiers";
import type { SchemaMorphism, SchemaObject } from "../schema";
import { PathMarker, type CustomPathFilter, type MorphismData } from "./PathMarker";

export enum NodeTag {
    Root = 'tag-root'
}

export enum AvailabilityStatus {
    Default = 'availability-default',
    Available = 'availability-available',
    CertainlyAvailable = 'availability-certainlyAvailable',
    Maybe = 'availability-maybe',
    Removable = 'availability-removable'
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
    //return status.type + (status.type !== SelectionType.Default ? '-' + status.level : '');
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

    neighbours = new TwoWayComparableMap<Node, number, SchemaMorphism, string>(
        node => node.schemaObject.key.value,
        morphism => morphism.signature.toString()
    );
    _signatureToMorphism = new ComparableMap<Signature, string, SchemaMorphism>(signature => signature.toString());

    constructor(schemaObject: SchemaObject) {
    //constructor(schemaObject: SchemaObject, nodeObject: NodeSingular) {
        this.schemaObject = schemaObject;
        //this.nodeObject = nodeObject;

        //nodeObject.
    }

    setCytoscapeNode(node: NodeSingular) {
        this.node = node;
        node.toggleClass('no-ids', this.schemaObject.schemaIds.length === 0);
    }

    addNeighbour(object: Node, morphism: SchemaMorphism): void {
        this.neighbours.set(object, morphism);
        this._signatureToMorphism.set(morphism.signature, morphism);
    }

    getNeighbour(signature: Signature): Node | undefined {
        if (signature.isEmpty)
            return this;

        const split = signature.getFirstBase();
        if (!split)
            return undefined;

        const morphism = this._signatureToMorphism.get(split.first);
        if (!morphism)
            return undefined;

        const nextNeighbour = this.neighbours.getKey(morphism);
        return !nextNeighbour ? undefined : split.rest.isEmpty ? nextNeighbour : nextNeighbour.getNeighbour(split.rest);
    }

    /*
    public get isLeaf(): boolean {
        // TODO this condition should be for all morphisms (i.e. also for their duals).
        // There aren't any leaves under current setting.
        return this.neighbours.size < 2;
    }
    */

    get determinedPropertyType(): PropertyType | null {
        /*
        if (this.isLeaf)
            return PropertyType.Simple;
        */

        return this.schemaObject.canBeSimpleProperty ? null : PropertyType.Complex;
    }

    /*
    addTag(tag: NodeTag): void {
        this.tags.add(tag);
    }

    removeTag(tag: NodeTag): void {
        this.tags.delete(tag);
    }

    get style(): string {
        let output = '';

        if (this.tags.has(NodeTag.Root))
            output += 'background-color: red';

        return output;
    }
    */

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
        //this.node.data('label', this.label); // This is only lazily evaluated due to performance reasons
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

    markAvailablePaths(constraint: DatabaseConfiguration | { filter: CustomPathFilter }): void {
        const filterOptions = constraint instanceof DatabaseConfiguration ? constraint : constraint.filter;
        const pathMarker = new PathMarker(this, filterOptions);
        pathMarker.markPathsFromRootNode();
    }

    addId(schemaId: SchemaId): void {
        // TODO
        this.schemaObject.schemaIds.push(schemaId);
        this.node.removeClass('no-ids');
    }
}

