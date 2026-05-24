import { type SimulationLinkDatum, type SimulationNodeDatum, forceCollide, forceLink, forceManyBody, forceSimulation, forceX, forceY } from 'd3-force';
import type { Graph } from './graphEngine';

export enum LayoutType {
    force = 'force',
}

export function layoutGraph(graph: Graph, type: LayoutType): void {
    const nodes = graph.nodes.values().map(node => ({
        id: node.id,
        x: node.x,
        y: node.y,
    } satisfies ForceNode)).toArray();

    const edges = graph.edges.values().map(edge => ({
        source: edge.from,
        target: edge.to,
    } satisfies ForceEdge)).toArray();

    switch (type) {
    case LayoutType.force:
        forceLayout(nodes, edges);
        break;
    }

    for (const node of nodes) {
        const originalgNode = graph.nodes.get(node.id)!;
        originalgNode.x = node.x;
        originalgNode.y = node.y;
    }
}

type ForceNode = SimulationNodeDatum & {
    id: string;
};

type ForceEdge = SimulationLinkDatum<ForceNode>;

function forceLayout(nodes: ForceNode[], edges: ForceEdge[]): void {
    const simulation = forceSimulation(nodes)
        .force('charge', forceManyBody().strength(-400))
        .force('centerX', forceX(0).strength(0.05))
        .force('centerY', forceY(0).strength(0.05))
        .force('collide', forceCollide().radius(35))
        .force('link', forceLink<ForceNode, ForceEdge>(edges).id(d => d.id).distance(150).strength(0.2));

    simulation.tick(300);
}
