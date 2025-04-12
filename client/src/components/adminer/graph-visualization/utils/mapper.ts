import { type BasicNode, type BasicRelationship } from '../types/types';
import { GraphModel } from '../types/Graph';
import { NodeModel } from '../types/Node';
import { RelationshipModel } from '../types/Relationship';

const mapProperties = (_: Record<string, string>): Record<string, string> => Object.assign({}, ...stringifyValues(_));
const stringifyValues = (obj: Record<string, string>) =>
    Object.keys(obj).map(k => ({
        [k]: obj[k] === null ? 'null' : optionalToString(obj[k]),
    }));

function optionalToString(value: unknown): unknown {
    return value && typeof value?.toString === 'function'
        ? value.toString()
        : value;
}

export function createGraph(
    nodes: BasicNode[],
    relationships: BasicRelationship[],
): GraphModel {
    const graph = new GraphModel();
    graph.addNodes(mapNodes(nodes));
    graph.addRelationships(mapRelationships(relationships, graph));
    return graph;
}

export function mapNodes(nodes: BasicNode[]): NodeModel[] {
    return nodes.map(node => new NodeModel(
        node.id,
        node.labels,
        mapProperties(node.properties),
        node.elementId,
    ));
}

export function mapRelationships(
    relationships: BasicRelationship[],
    graph: GraphModel,
): RelationshipModel[] {
    return relationships.map(rel => {
        const source = graph.findNode(rel.startNodeId);
        const target = graph.findNode(rel.endNodeId);
        return new RelationshipModel(
            rel.id,
            source,
            target,
            rel.type,
            mapProperties(rel.properties),
            rel.elementId,
        );
    });
}

type GraphStatsLabels = Record<string, { count: number, properties: Record<string, string> }>;
type GraphStatsRelationshipTypes = Record<string, { count: number, properties: Record<string, string> }>;

export type GraphStats = {
  labels?: GraphStatsLabels;
  relTypes?: GraphStatsRelationshipTypes;
}

export function getGraphStats(graph: GraphModel): GraphStats {
    const labelStats: GraphStatsLabels = {};
    const relTypeStats: GraphStatsRelationshipTypes = {};
    graph.nodes().forEach(node => {
        node.labels.forEach(label => {
            if (labelStats['*']) {
                labelStats['*'].count = labelStats['*'].count + 1;
            }
            else {
                labelStats['*'] = {
                    count: 1,
                    properties: {},
                };
            }
            if (labelStats[label]) {
                labelStats[label].count = labelStats[label].count + 1;
                labelStats[label].properties = {
                    ...labelStats[label].properties,
                    ...node.propertyMap,
                };
            }
            else {
                labelStats[label] = {
                    count: 1,
                    properties: node.propertyMap,
                };
            }
        });
    });
    graph.relationships().forEach(rel => {
        if (relTypeStats['*']) {
            relTypeStats['*'].count = relTypeStats['*'].count + 1;
        }
        else {
            relTypeStats['*'] = {
                count: 1,
                properties: {},
            };
        }
        if (relTypeStats[rel.type]) {
            relTypeStats[rel.type].count = relTypeStats[rel.type].count + 1;
            relTypeStats[rel.type].properties = {
                ...relTypeStats[rel.type].properties,
                ...rel.propertyMap,
            };
        }
        else {
            relTypeStats[rel.type] = {
                count: 1,
                properties: rel.propertyMap,
            };
        }
    });
    return { labels: labelStats, relTypes: relTypeStats };
}
