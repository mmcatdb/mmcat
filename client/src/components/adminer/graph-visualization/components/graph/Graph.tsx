import { Component, createRef, type RefObject, type JSX } from 'react';
import { type GraphModel } from '@/components/adminer/graph-visualization/types/Graph';
import { GraphEventHandlerModel, type GraphInteractionCallBack } from './GraphEventHandlerModel';
import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';
import { type GetNodeNeighborsFn, type VizItem, type ZoomLimitsReached, ZoomType, type BasicNode, type BasicRelationship } from '@/components/adminer/graph-visualization/types/types';
import { type GraphStats, createGraph, getGraphStats } from '@/components/adminer/graph-visualization/utils/mapper';
import { Visualization } from './visualization/Visualization';
import { StyledSvgWrapper, StyledZoomButton, StyledZoomHolder } from './styled';
import { ResizeObserver } from '@juggle/resize-observer';
import { ZoomInIcon, ZoomOutIcon, ZoomToFitIcon } from '@/components/adminer/graph-visualization/components/Icons';

export type GraphProps = {
  isFullscreen: boolean;
  relationships: BasicRelationship[];
  nodes: BasicNode[];
  getNodeNeighbors: GetNodeNeighborsFn;
  onItemMouseOver: (item: VizItem) => void;
  onItemSelect: (item: VizItem) => void;
  graphStyle: GraphStyleModel;
  styleVersion: number;
  onGraphModelChange: (stats: GraphStats) => void;
  setGraph: (graph: GraphModel) => void;
  initialZoomToFit?: boolean;
  onGraphInteraction?: GraphInteractionCallBack;
}

type GraphState = {
  zoomInLimitReached: boolean;
  zoomOutLimitReached: boolean;
  displayingWheelZoomInfoMessage: boolean;
}

export class Graph extends Component<GraphProps, GraphState> {
    svgElement: RefObject<SVGSVGElement>;
    wrapperElement: RefObject<HTMLDivElement>;
    wrapperResizeObserver: ResizeObserver;
    visualization: Visualization | null = null;

    constructor(props: GraphProps) {
        super(props);
        this.state = {
            zoomInLimitReached: false,
            zoomOutLimitReached: false,
            displayingWheelZoomInfoMessage: false,
        };
        this.svgElement = createRef<SVGSVGElement>();
        this.wrapperElement = createRef<HTMLDivElement>();

        this.wrapperResizeObserver = new ResizeObserver(() => {
            this.visualization?.resize(this.props.isFullscreen);
        });
    }

    componentDidMount(): void {
        const {
            getNodeNeighbors,
            graphStyle,
            initialZoomToFit,
            isFullscreen,
            nodes,
            onGraphInteraction,
            onGraphModelChange,
            onItemMouseOver,
            onItemSelect,
            relationships,
            setGraph,
        } = this.props;

        if (!this.svgElement.current)
            return;

        const measureSize = () => ({
            width: this.svgElement.current?.parentElement?.clientWidth ?? 200,
            height: this.svgElement.current?.parentElement?.clientHeight ?? 200,
        });

        const graph = createGraph(nodes, relationships);
        this.visualization = new Visualization(
            this.svgElement.current,
            measureSize,
            this.handleZoomEvent,
            graph,
            graphStyle,
            isFullscreen,
            initialZoomToFit,
        );

        const graphEventHandler = new GraphEventHandlerModel(
            graph,
            this.visualization,
            getNodeNeighbors,
            onItemMouseOver,
            onItemSelect,
            onGraphModelChange,
            onGraphInteraction,
        );
        graphEventHandler.bindEventHandlers();

        onGraphModelChange(getGraphStats(graph));
        this.visualization.resize(isFullscreen);

        if (setGraph)
            setGraph(graph);

        this.visualization?.init();
        this.visualization?.precomputeAndStart();

        this.wrapperResizeObserver.observe(this.svgElement.current);
    }

    componentDidUpdate(prevProps: GraphProps): void {
        if (this.props.isFullscreen !== prevProps.isFullscreen)
            this.visualization?.resize(this.props.isFullscreen);

        if (this.props.styleVersion !== prevProps.styleVersion
            || this.props.nodes !== prevProps.nodes
            || this.props.relationships !== prevProps.relationships
        ) {
            this.visualization?.updateGraph(
                createGraph(this.props.nodes, this.props.relationships),
            );
            this.visualization?.update({
                updateNodes: true,
                updateRelationships: true,
                restartSimulation: false,
            });
        }
    }

    componentWillUnmount(): void {
        this.wrapperResizeObserver.disconnect();
    }

    handleZoomEvent = (limitsReached: ZoomLimitsReached): void => {
        if (
            limitsReached.zoomInLimitReached !== this.state.zoomInLimitReached ||
      limitsReached.zoomOutLimitReached !== this.state.zoomOutLimitReached
        ) {
            this.setState({
                zoomInLimitReached: limitsReached.zoomInLimitReached,
                zoomOutLimitReached: limitsReached.zoomOutLimitReached,
            });
        }
    };

    zoomInClicked = (): void => {
        this.visualization?.zoomByType(ZoomType.IN);
    };

    zoomOutClicked = (): void => {
        this.visualization?.zoomByType(ZoomType.OUT);
    };

    zoomToFitClicked = (): void => {
        this.visualization?.zoomByType(ZoomType.FIT);
    };

    render(): JSX.Element {
        const { isFullscreen } = this.props;
        const { zoomInLimitReached, zoomOutLimitReached } = this.state;

        return (
            <StyledSvgWrapper ref={this.wrapperElement}>
                <svg className='neod3viz' ref={this.svgElement} />

                <StyledZoomHolder isFullscreen={isFullscreen}>
                    <StyledZoomButton
                        aria-label={'zoom-in'}
                        className={'zoom-in'}
                        disabled={zoomInLimitReached}
                        onClick={this.zoomInClicked}
                    >
                        <ZoomInIcon large={isFullscreen} />
                    </StyledZoomButton>
                    <StyledZoomButton
                        aria-label={'zoom-out'}
                        className={'zoom-out'}
                        disabled={zoomOutLimitReached}
                        onClick={this.zoomOutClicked}
                    >
                        <ZoomOutIcon large={isFullscreen} />
                    </StyledZoomButton>
                    <StyledZoomButton
                        aria-label={'zoom-to-fit'}
                        onClick={this.zoomToFitClicked}
                    >
                        <ZoomToFitIcon large={isFullscreen} />
                    </StyledZoomButton>
                </StyledZoomHolder>
            </StyledSvgWrapper>
        );
    }
}
