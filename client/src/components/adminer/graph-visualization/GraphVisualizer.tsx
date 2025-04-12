import { type JSX } from 'react';
import { Component } from 'react';
import { Graph } from './components/graph/Graph';
import { GraphStyleModel } from './types/GraphStyle';
import { type GraphStats } from './utils/mapper';
import { type GraphModel } from './types/Graph';
import { type BasicNode, type BasicNodesAndRels, type BasicRelationship } from './types/types';
import { StyledFullSizeContainer } from './VisualizationView.styled';

const DEFAULT_MAX_NEIGHBORS = 100;

type GraphVisualizerDefaultProps = {
  maxNeighbors: number;
  isFullscreen: boolean;
  setGraph: (graph: GraphModel) => void;
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
  useGeneratedDefaultColors?: boolean;
}

type GraphVisualizerState = {
  graphStyle: GraphStyleModel;
  nodes: BasicNode[];
  relationships: BasicRelationship[];
  stats: GraphStats;
  styleVersion: number;
  freezeLegend: boolean;
}

export class GraphVisualizer extends Component<GraphVisualizerProps, GraphVisualizerState> {
    static defaultProps: GraphVisualizerDefaultProps = {
        maxNeighbors: DEFAULT_MAX_NEIGHBORS,
        isFullscreen: false,
        setGraph: () => undefined,
        useGeneratedDefaultColors: true,
    };

    constructor(props: GraphVisualizerProps) {
        super(props);
        const graphStyle = new GraphStyleModel(this.props.useGeneratedDefaultColors);
        const {
            nodes,
            relationships,
        } = this.props;

        this.state = {
            stats: {
                labels: {},
                relTypes: {},
            },
            graphStyle,
            styleVersion: 0,
            nodes,
            relationships,
            freezeLegend: false,
        };
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
                    relationships={this.state.relationships}
                    nodes={this.state.nodes}
                    graphStyle={graphStyle}
                    styleVersion={this.state.styleVersion} // cheap way for child to check style updates
                    onGraphModelChange={this.onGraphModelChange.bind(this)}
                    setGraph={this.props.setGraph}
                    initialZoomToFit={this.props.initialZoomToFit}
                />
            </StyledFullSizeContainer>
        );
    }
}
