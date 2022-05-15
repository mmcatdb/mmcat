import { ComparableMap } from "@/utils/ComparableMap";
import { TwoWayComparableMap } from "@/utils/TwoWayComparableMap";
import type { NodeSingular } from "cytoscape";
import type { DatabaseConfiguration } from "../database";
import type { Signature } from "../identifiers";
import { Cardinality, SchemaMorphism, type Max, type Min, type SchemaObject } from "../schema";

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
        this.node.data('label', this.label);
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

    markAvailablePaths(configuration: DatabaseConfiguration): void {
        const pathMarker = new PathMarker(configuration, this);
        pathMarker.markPathsFromRootNode();
    }
}

type MorphismData = {
    signature: Signature,
    min: Min,
    max: Max
}

function morphismToData(morphism: SchemaMorphism): MorphismData {
    return {
        signature: morphism.signature,
        min: morphism.min,
        max: morphism.max
    };
}

function combineData(first: MorphismData, second: MorphismData): MorphismData {
    return {
        signature: first.signature.concatenate(second.signature),
        min: first.min === Cardinality.One && second.min === Cardinality.One ? Cardinality.One : Cardinality.Zero,
        max: first.max === Cardinality.One && second.max === Cardinality.One ? Cardinality.One : Cardinality.Star
    };
}

type NodeNeigbour = {
    node: Node,
    previousNode: Node,
    morphism: MorphismData,
    previousNeighbour?: NodeNeigbour,
    dependentNeighbours: NodeNeigbour[]
}

class PathMarker {
    configuration: DatabaseConfiguration;
    // It's actually important this is a stack and not a queue, because the paths has to be traversed from in one go.
    readonly stack = [] as NodeNeigbour[];
    rootNode: Node;

    constructor(configuration: DatabaseConfiguration, rootNode: Node) {
        this.configuration = configuration;
        this.rootNode = rootNode;
    }

    getTraversableNeighbours(parentNode: Node, parentNeighbour?: NodeNeigbour): NodeNeigbour[] {
        const combineFunction = parentNeighbour ?
            (morphism: SchemaMorphism) => combineData(parentNeighbour.morphism, morphism) :
            (morphism: SchemaMorphism) => morphismToData(morphism);

        const filterFunction = parentNeighbour ?
            this.filterWithEntryMorphismFunction(parentNeighbour.morphism) :
            (
                (entry: NodeNeigbour) => entry.morphism.max === Cardinality.One ?
                    this.configuration.isPropertyToOneAllowed :
                    this.configuration.isPropertyToManyAllowed
            );

        let neighbours = [ ...parentNode.neighbours.entries() ]
            .map(([ childNode, morphism ]) => ({
                node: childNode,
                previousNode: parentNode,
                morphism: combineFunction(morphism),
                previousNeighbour: parentNeighbour,
                dependentNeighbours: []
            }))
            .filter(neighbour => !neighbour.node.equals(this.rootNode))
            .filter(filterFunction);

        if (parentNeighbour && neighbours.length > 1)
            neighbours = neighbours.filter(entry => !entry.node.equals(parentNode));

        return neighbours;
    }

    filterWithEntryMorphismFunction(entryMorphism: MorphismData): (neighbour: NodeNeigbour) => boolean {
        return (neighbour: NodeNeigbour) => {
            // The path backwards is not allowed unless this node is the current root (ie. entry morphism is empty)
            const base = neighbour.morphism.signature.getLastBase();
            const entryBase = entryMorphism.signature.getLastBase();
            if (base && entryBase && base.last.isBaseAndDualOf(entryBase.last))
                return false;

            return neighbour.morphism.max === Cardinality.One ?
                this.configuration.isInliningToOneAllowed :
                this.configuration.isInliningToManyAllowed;
        };
    }

    processNeighbour(neighbour: NodeNeigbour, isDirect = false): void {
        // If the previous node was the root node, this node is definitely available so we mark it this way.
        if (isDirect) {
            neighbour.node.setAvailabilityStatus(AvailabilityStatus.CertainlyAvailable);
            neighbour.node.availablePathData = neighbour.morphism;
        }
        // If the node hasn't been traversed yet, it becomes available.
        // Unless it's previous node has been found ambigous - then this node is also ambiguous.
        else if (neighbour.node.availabilityStatus === AvailabilityStatus.Default) {
            if (neighbour.previousNode?.availabilityStatus === AvailabilityStatus.Maybe) {
                neighbour.node.setAvailabilityStatus(AvailabilityStatus.Maybe);
            }
            else {
                neighbour.node.setAvailabilityStatus(AvailabilityStatus.Available);
                neighbour.node.availablePathData = neighbour.morphism;

                // If the previous neighbour is found to be ambiguous, this one is also ambiguous.
                if (neighbour.previousNeighbour)
                    neighbour.previousNeighbour.dependentNeighbours.push(neighbour);
            }
        }
        // Already traversed path detected. This means we have mark all potentially ambiguous nodes.
        else {
            // If the node is already in the maybe state, it means we have already processed all its ambiguous paths
            if (neighbour.node.availabilityStatus !== AvailabilityStatus.Maybe)
                this.processAmbiguousPath(neighbour);

            return;
        }

        const addition = this.getTraversableNeighbours(neighbour.node, neighbour);
        this.stack.push(...addition);
    }

    processAmbiguousPath(lastNeighbour: NodeNeigbour): void {
        let currentNeigbour = lastNeighbour.previousNeighbour;

        while (
            currentNeigbour &&
            !currentNeigbour.node.equals(lastNeighbour.node) &&
            currentNeigbour.node.availabilityStatus !== AvailabilityStatus.CertainlyAvailable
        ) {
            currentNeigbour.node.setAvailabilityStatus(AvailabilityStatus.Maybe);
            this.processAmbiguousDependentNeighbours(currentNeigbour);
            currentNeigbour = currentNeigbour.previousNeighbour;
        }
    }

    processAmbiguousDependentNeighbours(neigbour: NodeNeigbour): void {
        neigbour.dependentNeighbours.forEach(dependentNeighbour => {
            if (dependentNeighbour.node.availabilityStatus !== AvailabilityStatus.Maybe) {
                dependentNeighbour.node.setAvailabilityStatus(AvailabilityStatus.Maybe);
                this.processAmbiguousDependentNeighbours(dependentNeighbour);
            }
        });
    }

    markPathsFromRootNode(): void {
        this.rootNode.setAvailabilityStatus(AvailabilityStatus.Removable);

        /*
        if (isCenteredOnRootNode)
            this.rootNode.select({ type: SelectionType.Root, level: 0 });
        */

        // For the direct neighbours of the root the queue is needed.
        const queue = this.getTraversableNeighbours(this.rootNode);

        let directNeighbour = queue.shift();
        while (directNeighbour) {
            this.processNeighbour(directNeighbour, true);
            directNeighbour = queue.shift();
        }

        let neighbour = this.stack.pop();
        while (neighbour) {
            this.processNeighbour(neighbour);
            neighbour = this.stack.pop();
        }
    }
}
