import { Component, createRef, type RefObject, type JSX } from 'react';
import { type GraphModel } from '@/components/adminer/graph-visualization/types/Graph';
import { GraphEventHandlerModel } from './GraphEventHandlerModel';
import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';
import { type GetNodeNeighborsFn, type VizItem, type ZoomLimitsReached, ZoomType, type BasicNode, type BasicRelationship } from '@/components/adminer/graph-visualization/types/types';
import { createGraph } from '@/components/adminer/graph-visualization/utils/mapper';
import { Visualization } from './Visualization';
import { ResizeObserver } from '@juggle/resize-observer';
import { ZoomInIcon, ZoomOutIcon, ZoomToFitIcon } from '@/components/adminer/graph-visualization/components/Icons';
import { twJoin } from 'tailwind-merge';

export type GraphProps = {
    isFullscreen: boolean;
    relationships: BasicRelationship[];
    nodes: BasicNode[];
    getNodeNeighbors: GetNodeNeighborsFn;
    onItemMouseOver: (item: VizItem) => void;
    onItemSelect: (item: VizItem) => void;
    graphStyle: GraphStyleModel;
    styleVersion: number;
    setGraph: (graph: GraphModel) => void;
    initialZoomToFit?: boolean;
};

type GraphState = {
    zoomInLimitReached: boolean;
    zoomOutLimitReached: boolean;
    displayingWheelZoomInfoMessage: boolean;
};

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
        );
        graphEventHandler.bindEventHandlers();

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

    render(): JSX.Element {
        const { isFullscreen } = this.props;
        const { zoomInLimitReached, zoomOutLimitReached } = this.state;

        return (
            <div ref={this.wrapperElement} className='relative h-full leading-0'>
                <svg className='size-full' ref={this.svgElement} />

                <div className={twJoin('bottom-2 left-2 flex flex-col bg-default-50', isFullscreen ? 'fixed' : 'absolute')}>
                    <ZoomButton
                        aria-label='zoom-in'
                        onClick={() => this.visualization?.zoomByType(ZoomType.IN)}
                        disabled={zoomInLimitReached}
                    >
                        <ZoomInIcon large={isFullscreen} />
                    </ZoomButton>
                    <ZoomButton
                        aria-label='zoom-out'
                        onClick={() => this.visualization?.zoomByType(ZoomType.OUT)}
                        disabled={zoomOutLimitReached}
                    >
                        <ZoomOutIcon large={isFullscreen} />
                    </ZoomButton>
                    <ZoomButton
                        aria-label='zoom-to-fit'
                        onClick={() => this.visualization?.zoomByType(ZoomType.FIT)}
                    >
                        <ZoomToFitIcon large={isFullscreen} />
                    </ZoomButton>
                </div>
            </div>
        );
    }
}

type ZoomButtonProps = {
    'aria-label': string;
    onClick: () => void;
    disabled?: boolean;
    children: JSX.Element;
};

function ZoomButton(props: ZoomButtonProps) {
    return (
        <button {...props} className='p-2 hover:enabled:bg-default-300 active:enabled:bg-default-200 disabled:opacity-30' />
    );
}
