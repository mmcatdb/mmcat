import { ComparableMap } from "@/utils/ComparableMap";
import { TwoWayComparableMap } from "@/utils/TwoWayComparableMap";
import type { NodeSingular } from "cytoscape";
import type { DatabaseConfiguration } from "../database";
import type { Signature } from "../identifiers";
import { Cardinality, SchemaMorphism, type Max, type Min, type SchemaObject } from "../schema";


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

export enum PropertyType {
    Simple = 'Simple',
    Complex = 'Complex'
}

export class Node {
    schemaObject: SchemaObject;
    node!: NodeSingular;
    _tags = new Set() as Set<NodeTag>;
    _availabilityStatus = AvailabilityStatus.Default;
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

    public get determinedPropertyType(): PropertyType | null {
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

    get availabilityStatus(): AvailabilityStatus {
        return this._availabilityStatus;
    }

    select(availabilityStatus: AvailabilityStatus = AvailabilityStatus.Selected): void {
        //this.tags.add(NodeTag.Selected);
        this.node.removeClass(this._availabilityStatus);
        this._availabilityStatus = availabilityStatus;
        this.node.addClass(availabilityStatus);
    }

    unselect(availabilityStatus?: AvailabilityStatus): void {
        //this.tags.delete(NodeTag.Selected);
        if (availabilityStatus === undefined || this._availabilityStatus === availabilityStatus) {
            this.node.removeClass(this._availabilityStatus);
            this._availabilityStatus = AvailabilityStatus.Default;
        }
    }

    resetAvailabilityStatus(): void {
        if (this._availabilityStatus === AvailabilityStatus.Available || this._availabilityStatus === AvailabilityStatus.CertainlyAvailable || this._availabilityStatus === AvailabilityStatus.Maybe) {
            this.node.removeClass(this._availabilityStatus);
            this._availabilityStatus = AvailabilityStatus.Default;
        }
    }

    becomeRoot(): void {
        this._tags.add(NodeTag.Root);
        this.node.addClass('root');
    }

    equals(other: Node | null): boolean {
        return !!other && this.schemaObject.id === other.schemaObject.id;
    }

    markAvailablePaths(configuration: DatabaseConfiguration, temporary = false): void {
        console.log('################################# START #################################');
        if (!temporary)
            this.select(AvailabilityStatus.Root);

        const neighbours = [ ...this.neighbours.entries() ]
            .filter(([ node ]) => node.availabilityStatus !== AvailabilityStatus.Selected)
            .map(([ node, morphism ]) => ({
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

            console.log('entry: ' + entry.node.schemaObject.label + ': ' + entry.morphism.signature.toString());
            //console.log('stack: ' + stack.map(e => '\n' + e.node.schemaObject.label + ': ' + e.morphism.signature.toString()).concat());
            if (entry.previousNode === this && entry.node.availabilityStatus !== AvailabilityStatus.Selected && entry.node.availabilityStatus !== AvailabilityStatus.Root) {
                entry.node.select(AvailabilityStatus.CertainlyAvailable);
                entry.node.availablePathData = entry.morphism;
            }
            else if (entry.node._availabilityStatus === AvailabilityStatus.Default) {
                entry.node.select(AvailabilityStatus.Available);
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

            const stackAddition = nodeNeighbours.filter(data => data.node !== previousNode && data.node.availabilityStatus !== AvailabilityStatus.Selected)
                .filter(data => data.morphism.max === Cardinality.One
                    ? configuration.isInliningToOneAllowed
                    : configuration.isInliningToManyAllowed
                );
            //stackAddition.forEach(a => console.log('add: ' + a.node.schemaObject.label + ' ? ' + entryNode.schemaObject.label + ' ?? ' + (a.node === entryNode)));
            //console.log('stackAdition: ' + stackAddition.map(e => '\n' + e.node.schemaObject.label + ': ' + e.morphism.signature.toString()).concat());
            stack.push(...stackAddition);

            entry = stack.pop();
        }

        console.log('################################# END #################################');
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
