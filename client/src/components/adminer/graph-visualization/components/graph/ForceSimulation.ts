import { type Simulation, forceCollide, forceLink, forceManyBody, forceSimulation, forceX, forceY } from 'd3-force';
import { DEFAULT_ALPHA, DEFAULT_ALPHA_MIN, FORCE_CENTER_X, FORCE_CENTER_Y, FORCE_CHARGE, FORCE_COLLIDE_RADIUS, FORCE_LINK_DISTANCE, LINK_DISTANCE, MAX_PRECOMPUTED_TICKS, EXTRA_TICKS_PER_RENDER, VELOCITY_DECAY } from '@/components/adminer/graph-visualization/utils/constants';
import { type GraphModel } from '@/components/adminer/graph-visualization/types/Graph';
import { type NodeModel } from '@/components/adminer/graph-visualization/types/Node';
import { type RelationshipModel } from '@/components/adminer/graph-visualization/types/Relationship';

const oneRelationshipPerPairOfNodes = (graph: GraphModel) =>
    Array.from(graph.groupedRelationships()).map(pair => pair.relationships[0]);

export class ForceSimulation {
    simulation: Simulation<NodeModel, RelationshipModel>;
    simulationTimeout: null | number = null;

    constructor(render: () => void) {
        this.simulation = forceSimulation<NodeModel, RelationshipModel>()
            .velocityDecay(VELOCITY_DECAY)
            .force('charge', forceManyBody().strength(FORCE_CHARGE))
            .force('centerX', forceX(0).strength(FORCE_CENTER_X))
            .force('centerY', forceY(0).strength(FORCE_CENTER_Y))
            .alphaMin(DEFAULT_ALPHA_MIN)
            .on('tick', () => {
                this.simulation.tick(EXTRA_TICKS_PER_RENDER);
                render();
            })
            .stop();
    }

    updateNodes(graph: GraphModel): void {
        const nodes = graph.nodes();

        const radius = (nodes.length * LINK_DISTANCE) / (Math.PI * 2);
        const center = {
            x: 0,
            y: 0,
        };
        circularLayout(nodes, center, radius);

        this.simulation
            .nodes(nodes)
            .force('collide', forceCollide<NodeModel>().radius(FORCE_COLLIDE_RADIUS));
    }

    updateRelationships(graph: GraphModel): void {
        const relationships = oneRelationshipPerPairOfNodes(graph);

        this.simulation.force(
            'link',
            forceLink<NodeModel, RelationshipModel>(relationships)
                .id(node => node.id)
                .distance(FORCE_LINK_DISTANCE),
        );
    }

    precomputeAndStart(onEnd: () => void = () => undefined): void {
        this.simulation.stop();

        let precomputeTicks = 0;
        const start = performance.now();
        while (
            performance.now() - start < 250 &&
      precomputeTicks < MAX_PRECOMPUTED_TICKS
        ) {
            this.simulation.tick(1);
            precomputeTicks += 1;
            if (this.simulation.alpha() <= this.simulation.alphaMin())
                break;

        }

        this.simulation.restart().on('end', () => {
            onEnd();
            this.simulation.on('end', null);
        });
    }

    restart(): void {
        this.simulation.alpha(DEFAULT_ALPHA).restart();
    }
}

function circularLayout(
    nodes: NodeModel[],
    center: { x: number, y: number },
    radius: number,
): void {
    const unlocatedNodes = nodes.filter(node => !node.initialPositionCalculated);

    unlocatedNodes.forEach((node, i) => {
        node.x = center.x + radius * Math.sin((2 * Math.PI * i) / unlocatedNodes.length);
        node.y = center.y + radius * Math.cos((2 * Math.PI * i) / unlocatedNodes.length);
        node.initialPositionCalculated = true;
    });
}
