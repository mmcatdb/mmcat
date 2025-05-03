import { type JSX } from 'react';
import { Component } from 'react';
import { Graph } from './components/graph/Graph';
import { GraphStyleModel } from './types/GraphStyle';
import { type GraphStats } from './utils/mapper';
import { type GraphModel } from './types/Graph';
import { type GraphInteractionCallBack } from './components/graph/GraphEventHandlerModel';
import { type GetNodeNeighborsFn, type VizItem, type BasicNode, type BasicNodesAndRels, type BasicRelationship } from './types/types';
import { debounce } from './utils/debounce';
import { StyledFullSizeContainer } from './VisualizationView.styled';
import { type GraphResponse } from '@/types/adminer/DataResponse';
import { NodeInspectorPanel } from './components/panel/NodeInspectorPanel';

const DEFAULT_MAX_NEIGHBORS = 100;

type GraphVisualizerDefaultProps = {
  maxNeighbors: number;
  isFullscreen: boolean;
  setGraph: (graph: GraphModel) => void;
  hasTruncatedFields: boolean;
  initialZoomToFit?: boolean;
  useGeneratedDefaultColors: boolean;
}

type GraphVisualizerProps = GraphVisualizerDefaultProps & {
  relationships: BasicRelationship[];
  nodes: BasicNode[];
  maxNeighbors?: number;
  getNeighbors?: (
    id: string,
    currentNeighborIds: string[] | undefined
  ) => Promise<BasicNodesAndRels & { allNeighborsCount: number }>;
  isFullscreen?: boolean;
  setGraph?: (graph: GraphModel) => void;
  hasTruncatedFields?: boolean;
  nodeLimitHit?: boolean;
  onGraphInteraction?: GraphInteractionCallBack;
  useGeneratedDefaultColors?: boolean;
  fetchedData: GraphResponse;
}

type GraphVisualizerState = {
  graphStyle: GraphStyleModel;
  hoveredItem: VizItem;
  nodes: BasicNode[];
  relationships: BasicRelationship[];
  selectedItem: VizItem;
  stats: GraphStats;
  styleVersion: number;
  freezeLegend: boolean;
  expanded: boolean;
}

export class GraphVisualizer extends Component<GraphVisualizerProps, GraphVisualizerState> {
    static defaultProps: GraphVisualizerDefaultProps = {
        maxNeighbors: DEFAULT_MAX_NEIGHBORS,
        isFullscreen: false,
        setGraph: () => undefined,
        hasTruncatedFields: false,
        useGeneratedDefaultColors: true,
    };

    constructor(props: GraphVisualizerProps) {
        super(props);
        const graphStyle = new GraphStyleModel(this.props.useGeneratedDefaultColors);
        const {
            nodeLimitHit,
            nodes,
            relationships,
        } = this.props;

        const selectedItem: VizItem = nodeLimitHit
            ? {
                type: 'status-item',
                item: `Not all return nodes are being displayed due to Initial Node Display setting. Only first ${this.props.nodes.length} nodes are displayed.`,
            }
            : {
                type: 'canvas',
                item: {
                    nodeCount: nodes.length,
                    relationshipCount: relationships.length,
                },
            };

        this.state = {
            stats: {
                labels: {},
                relTypes: {},
            },
            graphStyle,
            styleVersion: 0,
            nodes,
            relationships,
            selectedItem,
            hoveredItem: selectedItem,
            freezeLegend: false,
            expanded: true,
        };
    }

    getNodeNeighbors: GetNodeNeighborsFn = (
        node,
        currentNeighborIds,
        callback,
    ) => {
        if (currentNeighborIds.length > this.props.maxNeighbors)
            callback({ nodes: [], relationships: [] });

        if (this.props.getNeighbors) {
            this.props.getNeighbors(node.id, currentNeighborIds).then(
                ({ nodes, relationships, allNeighborsCount }) => {
                    if (allNeighborsCount > this.props.maxNeighbors) {
                        this.setState({
                            selectedItem: {
                                type: 'status-item',
                                item: `Rendering was limited to ${this.props.maxNeighbors} of the node's total ${allNeighborsCount} neighbors due to browser config maxNeighbors.`,
                            },
                        });
                    }
                    callback({ nodes, relationships });
                },
                () => {
                    callback({ nodes: [], relationships: [] });
                },
            );
        }
    };

    onItemMouseOver(item: VizItem): void {
        this.setHoveredItem(item);
    }

    setHoveredItem = debounce((hoveredItem: VizItem) => {
        this.setState({ hoveredItem });
    }, 200);

    onItemSelect(selectedItem: VizItem): void {
        this.setState({ selectedItem });
    }

    onGraphModelChange(stats: GraphStats): void {
        this.setState({ stats });
    }

    render(): JSX.Element {
        // This is a workaround to make the style reset to the same colors as when starting the browser with an empty style
        // If the legend component has the style it will ask the neoGraphStyle object for styling before the graph component,
        // and also doing this in a different order from the graph. This leads to different default colors being assigned to different labels.
        const graphStyle = this.state.freezeLegend
            ? new GraphStyleModel(this.props.useGeneratedDefaultColors)
            : this.state.graphStyle;

        return (
            <StyledFullSizeContainer id='svg-vis'>
                <Graph
                    isFullscreen={this.props.isFullscreen}
                    relationships={this.props.relationships}
                    nodes={this.props.nodes}
                    getNodeNeighbors={this.getNodeNeighbors.bind(this)}
                    onItemMouseOver={this.onItemMouseOver.bind(this)}
                    onItemSelect={this.onItemSelect.bind(this)}
                    graphStyle={graphStyle}
                    styleVersion={this.state.styleVersion} // cheap way for child to check style updates
                    onGraphModelChange={this.onGraphModelChange.bind(this)}
                    setGraph={this.props.setGraph}
                    initialZoomToFit={this.props.initialZoomToFit}
                    onGraphInteraction={this.props.onGraphInteraction}
                />
                <NodeInspectorPanel
                    graphStyle={graphStyle}
                    hasTruncatedFields={this.props.hasTruncatedFields}
                    hoveredItem={this.state.hoveredItem}
                    selectedItem={this.state.selectedItem}
                    stats={this.state.stats}
                    data={this.props.fetchedData}
                />
            </StyledFullSizeContainer>
        );
    }
}
