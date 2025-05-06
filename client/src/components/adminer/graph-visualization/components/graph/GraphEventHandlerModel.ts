import { type GraphModel } from '@/components/adminer/graph-visualization/types/Graph';
import { type NodeModel } from '@/components/adminer/graph-visualization/types/Node';
import { type RelationshipModel } from '@/components/adminer/graph-visualization/types/Relationship';
import { type GetNodeNeighborsFn, type VizItem } from '@/components/adminer/graph-visualization/types/types';
import { mapNodes, mapRelationships } from '@/components/adminer/graph-visualization/utils/mapper';
import { type Visualization } from './visualization/Visualization';

export type GraphInteraction =
  | 'NODE_EXPAND'
  | 'NODE_UNPINNED'
  | 'NODE_DISMISSED'

export type GraphInteractionCallBack = (
  event: GraphInteraction,
  properties?: Record<string, unknown>
) => void

export class GraphEventHandlerModel {
    getNodeNeighbors: GetNodeNeighborsFn;
    graph: GraphModel;
    visualization: Visualization;
    onItemMouseOver: (item: VizItem) => void;
    onItemSelected: (item: VizItem) => void;
    onGraphInteraction: GraphInteractionCallBack;
    selectedItem: NodeModel | RelationshipModel | null;

    constructor(
        graph: GraphModel,
        visualization: Visualization,
        getNodeNeighbors: GetNodeNeighborsFn,
        onItemMouseOver: (item: VizItem) => void,
        onItemSelected: (item: VizItem) => void,
        onGraphInteraction?: (event: GraphInteraction) => void,
    ) {
        this.graph = graph;
        this.visualization = visualization;
        this.getNodeNeighbors = getNodeNeighbors;
        this.selectedItem = null;
        this.onItemMouseOver = onItemMouseOver;
        this.onItemSelected = onItemSelected;
        this.onGraphInteraction = onGraphInteraction ?? (() => undefined);
    }

    selectItem(item: NodeModel | RelationshipModel): void {
        if (this.selectedItem)
            this.selectedItem.selected = false;

        this.selectedItem = item;
        item.selected = true;

        this.visualization.update({
            updateNodes: this.selectedItem.isNode,
            updateRelationships: this.selectedItem.isRelationship,
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

            this.selectedItem = null;
        }
        this.onItemSelected({
            type: 'canvas',
            item: {
                nodeCount: this.graph.nodes().length,
                relationshipCount: this.graph.relationships().length,
            },
        });
    }

    nodeClose(d: NodeModel): void {
        this.graph.removeConnectedRelationships(d);
        this.graph.removeNode(d);
        this.deselectItem();
        this.visualization.update({
            updateNodes: true,
            updateRelationships: true,
            restartSimulation: true,
        });
        this.onGraphInteraction('NODE_DISMISSED');
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

    nodeUnlock(d: NodeModel): void {
        if (!d)
            return;

        d.fx = null;
        d.fy = null;
        this.deselectItem();
        this.onGraphInteraction('NODE_UNPINNED');
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
        this.onGraphInteraction('NODE_EXPAND');
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

    onMenuMouseOver(itemWithMenu: NodeModel): void {
        if (!itemWithMenu.contextMenu)
            throw new Error('menuMouseOver triggered without menu');

        this.onItemMouseOver({
            type: 'context-menu-item',
            item: {
                label: itemWithMenu.contextMenu.label,
                content: itemWithMenu.contextMenu.menuContent,
                selection: itemWithMenu.contextMenu.menuSelection,
            },
        });
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
            .on('menuMouseOver', this.onMenuMouseOver.bind(this))
            .on('menuMouseOut', this.onItemMouseOut.bind(this))
            .on('relMouseOver', this.onRelationshipMouseOver.bind(this))
            .on('relMouseOut', this.onItemMouseOut.bind(this))
            .on('relationshipClicked', this.onRelationshipClicked.bind(this))
            .on('canvasClicked', this.onCanvasClicked.bind(this))
            .on('nodeClose', this.nodeClose.bind(this))
            .on('nodeClicked', this.nodeClicked.bind(this))
            .on('nodeDblClicked', this.nodeDblClicked.bind(this))
            .on('nodeUnlock', this.nodeUnlock.bind(this));
        this.onItemMouseOut();
    }
}
