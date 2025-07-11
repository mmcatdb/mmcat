import { ComparableMap } from '@/utils/ComparableMap';
import type { Core, ElementDefinition, NodeSingular, Position } from 'cytoscape';
import type { Key, Signature } from '../identifiers';
import type { MetadataObjex, SchemaObjex, VersionedSchemaObjex } from '../schema';
import { DirectedEdge, type Edge } from './Edge';
import { PathMarker, type MorphismData, type Filter } from './PathMarker';
import { groupHighlightColorToClass } from '@/components/category/defaultGraphStyle';
import type { Group } from './Graph';

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

export class Neighbor {
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
    private node!: NodeSingular;
    private tags: Set<NodeTag> = new Set();
    availablePathData?: MorphismData;
    isFixed = false;

    private _neighbors = new ComparableMap<Signature, string, Neighbor>(signature => signature.value);

    private constructor(
        readonly objex: VersionedSchemaObjex,
        public schemaObjex: SchemaObjex,
        readonly highlights: NodeHighlights,
    ) {}

    static create(cytoscape: Core, objex: VersionedSchemaObjex, schemaObjex: SchemaObjex, position: Position, groups: Group[]): Node {
        // This objex is shared between all placeholder nodes. And its mutated when dragged ... yes, cytoscape is a bit weird.
        const positionObjex = { ...position };

        const highlights = new NodeHighlights(cytoscape, groups, schemaObjex.key, positionObjex);
        const node = new Node(objex, schemaObjex, highlights);
        const classes = (schemaObjex.isNew ? 'new' : '') + ' ' + (!schemaObjex.ids ? 'no-ids' : '');
        const nodeDefinition = createNodeDefinition(schemaObjex, positionObjex, node, classes);
        const cytoscapeNode = cytoscape.add(nodeDefinition);
        node.setCytoscapeNode(cytoscapeNode);

        return node;
    }

    private setCytoscapeNode(node: NodeSingular) {
        this.node = node;
        node.on('drag', () => this.refreshGroupPlaceholders());
    }

    remove() {
        this.highlights.remove();
        this.node.remove();
    }

    update(schemaObjex: SchemaObjex) {
        this.schemaObjex = schemaObjex;
        this.node.data('label', this.label);
        this.node.toggleClass('no-ids', !this.schemaObjex.ids);

        this.node.position({ ...this.metadata.position });

        if (!this.node.inside()) {
            this.node.restore();
            this.highlights.restore();
        }
    }

    get metadata(): MetadataObjex {
        return this.objex.metadata;
    }

    get cytoscapeIdAndPosition() {
        return {
            nodeId: this.node.id(),
            position: { ...this.node.position() },
        };
    }

    refreshGroupPlaceholders() {
        this.highlights.refresh();
    }

    addNeighbor(edge: Edge, direction: boolean): void {
        const thisNode = direction ? edge.domainNode : edge.codomainNode;
        if (!thisNode.equals(this))
            throw new Error(`Cannot add edge with signature: ${edge.schemaMorphism.signature} and direction: ${direction} to node with key: ${this.objex.key}`);

        const node = direction ? edge.codomainNode : edge.domainNode;

        const edgeSignature = edge.schemaMorphism.signature;
        const signature = direction ? edgeSignature : edgeSignature.dual();

        const neighbor = new Neighbor(edge, node, signature);
        this._neighbors.set(signature, neighbor);
    }

    removeNeighbor(node: Node): void {
        [ ...this._neighbors.values() ]
            .filter(neighbor => neighbor.node.equals(node))
            .forEach(neighbor => this._neighbors.delete(neighbor.signature));
    }

    getNeighborNode(signature: Signature): Node | undefined {
        if (signature.isEmpty)
            return this;

        const split = signature.getFirstBase();
        if (!split)
            return undefined;

        const neighbor = this._neighbors.get(split.first);
        if (!neighbor)
            return undefined;

        return split.rest.isEmpty ? neighbor.node : neighbor.node.getNeighborNode(split.rest);
    }

    get neighbors(): Neighbor[] {
        return [ ...this._neighbors.values() ];
    }

    get determinedPropertyType(): PropertyType | null {
        if (!this.schemaObjex.ids)
            return null;

        return this.schemaObjex.ids.isSignatures ? PropertyType.Complex : null;
    }

    private _availabilityStatus = AvailabilityStatus.Default;
    private _selectionStatus = defaultSelectionStatus;

    get availabilityStatus(): AvailabilityStatus {
        return this._availabilityStatus;
    }

    get label(): string {
        return this.metadata.label + (
            this._selectionStatus.type === SelectionType.Selected
                ? ` (${this._selectionStatus.level + 1})`
                : ''
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
        this.tags.add(NodeTag.Root);
        this.node.addClass(NodeTag.Root);
    }

    removeRoot(): void {
        if (this.tags.has(NodeTag.Root)) {
            this.tags.delete(NodeTag.Root);
            this.node.removeClass(NodeTag.Root);
        }
    }

    equals(other: Node | null | undefined): boolean {
        return !!other && this.schemaObjex.equals(other.schemaObjex);
    }

    markAvailablePaths(filter: Filter): void {
        const pathMarker = new PathMarker(this, filter);
        pathMarker.markPathsFromRootNode();
    }

    /// To highlight nodes in Mapping Editor
    highlight(): void {
        this.node.addClass('highlighted');
    }

    unhighlight(): void {
        this.node.removeClass('highlighted');
    }
    ///
}

function createNodeDefinition(objex: SchemaObjex, position: Position, node: Node, classes?: string): ElementDefinition {
    return {
        data: {
            id: '' + objex.key.value,
            label: node.label,
            schemaData: node,
        },
        position,
        ...classes ? { classes } : {},
    };
}

type GroupPlaceholder = {
    group: Group;
    node: NodeSingular;
    state: 'root' | 'property' | undefined;
};

class NodeHighlights {
    private readonly placeholders: Map<string, GroupPlaceholder> = new Map();

    public constructor(
        private readonly cytoscape: Core,
        groups: Group[],
        key: Key,
        position: Position,
    ) {
        groups.forEach(group => {
            this.placeholders.set(group.id, {
                group,
                node: this.cytoscape.add(createGroupPlaceholderDefinition(key, position, group.id)),
                state: undefined,
            });
        });
    }

    select(groupId: string, type: 'root' | 'property', value: boolean) {
        const placeholder = this.placeholders.get(groupId);
        if (!placeholder)
            return;

        if (placeholder.state !== undefined)
            placeholder.node.removeClass(groupHighlightColorToClass(placeholder.group.id, placeholder.state));

        placeholder.state = value ? type : undefined;
        if (placeholder.state !== undefined)
            placeholder.node.addClass(groupHighlightColorToClass(placeholder.group.id, placeholder.state));

        // We have to hide all other so that the selected one is visible.
        this.placeholders.forEach(p => p.node.toggleClass('group-placeholder-hidden', !value || p.group.id !== groupId));
    }

    remove() {
        this.placeholders.forEach(placeholder => {
            placeholder.node.remove();
            if (placeholder.group.node.children().length === 0)
                placeholder.group.node.remove();
        });
    }

    restore() {
        this.placeholders.forEach(placeholder => {
            if (!placeholder.group.node.inside())
                placeholder.group.node.restore();
            placeholder.node.restore();
        });
    }

    refresh() {
        this.placeholders.forEach(placeholder => {
            placeholder.node.remove();
            placeholder.node.restore();
        });
    }
}

function createGroupPlaceholderDefinition(key: Key, position: Position, groupId: string): ElementDefinition {
    return {
        data: {
            id: groupId + '_' + key.value,
            parent: 'group_' + groupId,
        },
        position,
        classes: 'group-placeholder group-placeholder-hidden',
    };
}

export function isPositionEqual(a: Position, b: Position): boolean {
    return a.x === b.x && a.y === b.y;
}
