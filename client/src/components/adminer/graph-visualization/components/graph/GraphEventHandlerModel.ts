import { type GraphModel } from '@/components/adminer/graph-visualization/types/Graph';
import { type GetNodeNeighborsFn } from '@/components/adminer/graph-visualization/types/types';
import { type GraphStats, getGraphStats } from '@/components/adminer/graph-visualization/utils/mapper';
import { type Visualization } from './visualization/Visualization';

export class GraphEventHandlerModel {
    getNodeNeighbors: GetNodeNeighborsFn;
    graph: GraphModel;
    visualization: Visualization;
    onGraphModelChange: (stats: GraphStats) => void;

    constructor(
        graph: GraphModel,
        visualization: Visualization,
        getNodeNeighbors: GetNodeNeighborsFn,
        onGraphModelChange: (stats: GraphStats) => void,
    ) {
        this.graph = graph;
        this.visualization = visualization;
        this.getNodeNeighbors = getNodeNeighbors;
        this.onGraphModelChange = onGraphModelChange;
    }

    graphModelChanged(): void {
        this.onGraphModelChange(getGraphStats(this.graph));
    }
}
