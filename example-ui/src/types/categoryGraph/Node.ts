import { TwoWayMap } from "@/utils/TwoWayMap";
import type { NodeSingular } from "cytoscape";
import type { DatabaseConfiguration } from "../database";
import type { Signature } from "../identifiers";
import { Cardinality, type SchemaMorphism, type SchemaObject } from "../schema";


export enum NodeTag {
    Root,
    Selected
}

export enum AvailabilityStatus {
    Default = 'default',
    Available = 'available',
    CertainlyAvailable = 'certainlyAvailable',
    Maybe = 'maybe',
    Selected = 'selected',
    Root = 'root'
}

export class Node {
    public schemaObject: SchemaObject;
    public node!: NodeSingular;
    private tags = new Set() as Set<NodeTag>;
    private _availabilityStatus = AvailabilityStatus.Default;
    public availablePathData = null as MorphismData | null;

    public neighbours = new TwoWayMap<Node, SchemaMorphism>();
    private signatureToMorphism = new Map() as Map<Signature, SchemaMorphism>;

    public constructor(schemaObject: SchemaObject) {
    //public constructor(schemaObject: SchemaObject, nodeObject: NodeSingular) {
        this.schemaObject = schemaObject;
        //this.nodeObject = nodeObject;

        //nodeObject.
    }

    public setNode(node: NodeSingular) {
        this.node = node;
    }

    public addNeighbour(object: Node, morphism: SchemaMorphism): void {
        this.neighbours.set(object, morphism);
        this.signatureToMorphism.set(morphism.signature, morphism);
    }

    public getNeighbour(signature: Signature): Node | undefined {
        const split = signature.getFirstBase();
        if (!split)
            return undefined;

        const morphism = this.signatureToMorphism.get(split.first);
        if (!morphism)
            return undefined;

        const nextNeighbour = this.neighbours.getKey(morphism);
        return !nextNeighbour ? undefined : split.rest.isEmpty ? nextNeighbour : nextNeighbour.getNeighbour(split.rest);
    }

    /*
    public addTag(tag: NodeTag): void {
        this.tags.add(tag);
    }

    public removeTag(tag: NodeTag): void {
        this.tags.delete(tag);
    }

    public get style(): string {
        let output = '';

        if (this.tags.has(NodeTag.Root))
            output += 'background-color: red';

        return output;
    }
    */

    public get availabilityStatus(): AvailabilityStatus {
        return this._availabilityStatus;
    }

    public select(availabilityStatus: AvailabilityStatus = AvailabilityStatus.Selected): void {
        //this.tags.add(NodeTag.Selected);
        this.node.removeClass(this._availabilityStatus);
        this._availabilityStatus = availabilityStatus;
        this.node.addClass(availabilityStatus);
    }

    public unselect(availabilityStatus?: AvailabilityStatus): void {
        //this.tags.delete(NodeTag.Selected);
        if (availabilityStatus === undefined || this._availabilityStatus === availabilityStatus) {
            this.node.removeClass(this._availabilityStatus);
            this._availabilityStatus = AvailabilityStatus.Default;
        }
    }

    public resetAvailabilityStatus(): void {
        if (this._availabilityStatus === AvailabilityStatus.Available || this._availabilityStatus === AvailabilityStatus.CertainlyAvailable || this._availabilityStatus === AvailabilityStatus.Maybe) {
            this.node.removeClass(this._availabilityStatus);
            this._availabilityStatus = AvailabilityStatus.Default;
        }
    }

    public becomeRoot(): void {
        this.tags.add(NodeTag.Root);
        this.node.addClass('root');
    }

    public equals(other: Node | null): boolean {
        return !!other && this.schemaObject.id === other.schemaObject.id;
    }

    public markAvailablePaths(configuration: DatabaseConfiguration, temporary = false): void {
        if (!temporary)
            this.select(AvailabilityStatus.Root);

        const neighbours = [ ...this.neighbours.entries() ].map(([ node, morphism ]) => ({
            node,
            previousNode: this as Node,
            morphism: morphismToData(morphism),
        }));

        const stack = neighbours.filter(data => data.morphism.max === Cardinality.One
            ? configuration.isPropertyToOneAllowed
            : configuration.isPropertyToManyAllowed
        );

        let entry = stack.pop();
        while (entry) {
            //console.log('entry: ' + entry.node.schemaObject.label + ': ' + entry.morphism.signature.toString());
            //console.log('stack: ' + stack.map(e => '\n' + e.node.schemaObject.label + ': ' + e.morphism.signature.toString()).concat());
            if (entry.node._availabilityStatus === AvailabilityStatus.Default) {
                entry.node.select(entry.previousNode === this ? AvailabilityStatus.CertainlyAvailable : AvailabilityStatus.Available);
                //entry.node.availabilityStatus = AvailabilityStatus.Available;
                entry.node.availablePathData = entry.morphism;
            }
            else if (entry.node._availabilityStatus === AvailabilityStatus.Available) {
                entry.node.select(AvailabilityStatus.Maybe);
                //entry.node.availabilityStatus = AvailabilityStatus.Maybe;
            }
            else {
                entry = stack.pop();
                continue;
            }

            const entryNode = entry.node;
            const previousNode = entry.previousNode;
            const entrymorphism = entry.morphism;
            const nodeNeighbours = [ ...entry.node.neighbours.entries() ].map(([ node, morphism ]) => ({
                node,
                previousNode: entryNode,
                morphism: combineData(entrymorphism, morphismToData(morphism))
            }));

            const stackAddition = nodeNeighbours.filter(data => data.node !== previousNode)
                .filter(data => data.morphism.max === Cardinality.One
                    ? configuration.isInliningToOneAllowed
                    : configuration.isInliningToManyAllowed
                );
            //stackAddition.forEach(a => console.log('add: ' + a.node.schemaObject.label + ' ? ' + entryNode.schemaObject.label + ' ?? ' + (a.node === entryNode)));
            //console.log('stackAdition: ' + stackAddition.map(e => '\n' + e.node.schemaObject.label + ': ' + e.morphism.signature.toString()).concat());
            stack.push(...stackAddition);

            entry = stack.pop();
        }
    }
}

type MorphismData = {
    signature: Signature,
    min: Cardinality.Zero | Cardinality.One,
    max: Cardinality.One | Cardinality.Star
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
