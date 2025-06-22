import { type GraphModel } from '@/components/adminer/graph-visualization/types/Graph';
import { type NodeModel } from '@/components/adminer/graph-visualization/types/Node';
import { type RelationshipModel } from '@/components/adminer/graph-visualization/types/Relationship';
import { type GetNodeNeighborsFn, type VizItem } from '@/components/adminer/graph-visualization/types/types';
import { mapNodes, mapRelationships } from '@/components/adminer/graph-visualization/utils/mapper';
import { type Visualization } from './Visualization';

export class GraphEventHandlerModel {
    selectedItem: NodeModel | RelationshipModel | undefined = undefined;

    constructor(
        readonly graph: GraphModel,
        readonly visualization: Visualization,
        readonly getNodeNeighbors: GetNodeNeighborsFn,
        readonly onItemMouseOver: (item: VizItem) => void,
        readonly onItemSelected: (item: VizItem) => void,
    ) {}

    selectItem(item: NodeModel | RelationshipModel): void {
        const prevSelected = this.selectedItem;

        if (this.selectedItem)
            this.selectedItem.selected = false;

        this.selectedItem = item;
        item.selected = true;

        this.visualization.update({
            updateNodes: this.selectedItem.isNode || !!prevSelected?.isNode,
            updateRelationships: this.selectedItem.isRelationship || !!prevSelected?.isRelationship,
            restartSimulation: false,
        });
    }

    deselectItem(): void {
        if (this.selectedItem) {
            this.selectedItem.selected = false;

            this.visualization.update({
                updateNodes: this.selectedItem.isNode,
                updateRelationships: this.selectedItem.isRelationship,
                restartSimulation: false,
            });

            this.selectedItem = undefined;
        }
        this.onItemSelected({
            type: 'canvas',
            item: {
                nodeCount: this.graph.nodes().length,
                relationshipCount: this.graph.relationships().length,
            },
        });
    }

    nodeClicked(node: NodeModel): void {
        if (!node)
            return;

        node.hoverFixed = false;
        node.fx = node.x;
        node.fy = node.y;
        if (!node.selected) {
            this.selectItem(node);
            this.onItemSelected({
                type: 'node',
                item: node,
            });
        }
        else {
            this.deselectItem();
        }
    }

    nodeDblClicked(d: NodeModel): void {
        if (d.expanded) {
            this.nodeCollapse(d);
            return;
        }
        d.expanded = true;
        const graph = this.graph;
        const visualization = this.visualization;
        this.getNodeNeighbors(
            d,
            this.graph.findNodeNeighborIds(d.id),
            ({ nodes, relationships }) => {
                graph.addExpandedNodes(d, mapNodes(nodes));
                graph.addRelationships(mapRelationships(relationships, graph));
                visualization.update({ updateNodes: true, updateRelationships: true });
            },
        );
    }

    nodeCollapse(d: NodeModel): void {
        d.expanded = false;
        this.graph.collapseNode(d);
        this.visualization.update({ updateNodes: true, updateRelationships: true });
    }

    onNodeMouseOver(node: NodeModel): void {
        if (!node.contextMenu) {
            this.onItemMouseOver({
                type: 'node',
                item: node,
            });
        }
    }

    onRelationshipMouseOver(relationship: RelationshipModel): void {
        this.onItemMouseOver({
            type: 'relationship',
            item: relationship,
        });
    }

    onRelationshipClicked(relationship: RelationshipModel): void {
        if (!relationship.selected) {
            this.selectItem(relationship);
            this.onItemSelected({
                type: 'relationship',
                item: relationship,
            });
        }
        else {
            this.deselectItem();
        }
    }

    onCanvasClicked(): void {
        this.deselectItem();
    }

    onItemMouseOut(): void {
        this.onItemMouseOver({
            type: 'canvas',
            item: {
                nodeCount: this.graph.nodes().length,
                relationshipCount: this.graph.relationships().length,
            },
        });
    }

    bindEventHandlers(): void {
        this.visualization
            .on('nodeMouseOver', this.onNodeMouseOver.bind(this))
            .on('nodeMouseOut', this.onItemMouseOut.bind(this))
            .on('relMouseOver', this.onRelationshipMouseOver.bind(this))
            .on('relMouseOut', this.onItemMouseOut.bind(this))
            .on('relationshipClicked', this.onRelationshipClicked.bind(this))
            .on('canvasClicked', this.onCanvasClicked.bind(this))
            .on('nodeClicked', this.nodeClicked.bind(this))
            .on('nodeDblClicked', this.nodeDblClicked.bind(this));
        this.onItemMouseOut();
    }
}
