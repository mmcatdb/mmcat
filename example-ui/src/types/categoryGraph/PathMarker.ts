import { DatabaseConfiguration } from "../database";
import type { Signature } from "../identifiers";
import { Cardinality, SchemaMorphism, type Max, type Min } from "../schema";
import { type Node, AvailabilityStatus } from "./Node";

export type MorphismData = {
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

export type CustomPathFilter = (parentNode: Node, childNode: Node, morphism: MorphismData) => boolean;

type FilterFunction = (neighbour: NodeNeigbour) => boolean;

export class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed from in one go.
    rootNode: Node;
    configuration?: DatabaseConfiguration;
    customPathFilter?: CustomPathFilter;

    readonly stack = [] as NodeNeigbour[];

    constructor(rootNode: Node, filterOptions: DatabaseConfiguration | CustomPathFilter) {
        this.rootNode = rootNode;
        if (filterOptions instanceof DatabaseConfiguration)
            this.configuration = filterOptions;
        else
            this.customPathFilter = filterOptions;
    }

    getTraversableNeighbours(parentNode: Node, parentNeighbour?: NodeNeigbour): NodeNeigbour[] {
        const combineFunction = parentNeighbour ?
            (morphism: SchemaMorphism) => combineData(parentNeighbour.morphism, morphism) :
            (morphism: SchemaMorphism) => morphismToData(morphism);

        let neighbours = [ ...parentNode.neighbours.entries() ]
            .map(([ childNode, morphism ]) => ({
                node: childNode,
                previousNode: parentNode,
                morphism: combineFunction(morphism),
                previousNeighbour: parentNeighbour,
                dependentNeighbours: []
            }))
            .filter(neighbour => !neighbour.node.equals(this.rootNode));

        if (this.configuration) {
            const configurationFilterFunction = createConfigurationFilterFunction(this.configuration, parentNeighbour);
            neighbours = neighbours.filter(configurationFilterFunction);
        }

        if (this.customPathFilter) {
            const customFilterFunction = createCustomFilterFunction(this.customPathFilter);
            neighbours = neighbours.filter(customFilterFunction);
        }

        if (parentNeighbour && neighbours.length > 1)
            neighbours = neighbours.filter(entry => !entry.node.equals(parentNode));

        return neighbours;
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

function createConfigurationFilterFunction(configuration: DatabaseConfiguration, parentNeighbour: NodeNeigbour | undefined): FilterFunction {
    return parentNeighbour ?
        createFilterWithEntryMorphismFunction(configuration, parentNeighbour.morphism) :
        (
            (entry: NodeNeigbour) => entry.morphism.max === Cardinality.One ?
                configuration.isPropertyToOneAllowed :
                configuration.isPropertyToManyAllowed
        );
}

function createFilterWithEntryMorphismFunction(configuration: DatabaseConfiguration, entryMorphism: MorphismData): FilterFunction {
    return (neighbour: NodeNeigbour) => {
        // The path backwards is not allowed unless this node is the current root (ie. entry morphism is empty)
        const base = neighbour.morphism.signature.getLastBase();
        const entryBase = entryMorphism.signature.getLastBase();
        if (base && entryBase && base.last.isBaseAndDualOf(entryBase.last))
            return false;

        return neighbour.morphism.max === Cardinality.One ?
            configuration.isInliningToOneAllowed :
            configuration.isInliningToManyAllowed;
    };
}

function createCustomFilterFunction(pathFilter: CustomPathFilter): FilterFunction {
    return (neighbour: NodeNeigbour) => pathFilter(neighbour.previousNode, neighbour.node, neighbour.morphism);
}
