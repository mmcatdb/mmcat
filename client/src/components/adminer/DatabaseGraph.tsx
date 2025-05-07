import { useMemo } from 'react';
import { usePreferences } from '@/components/PreferencesProvider';
import { ArcThemeProvider } from '@/components/adminer/graph-visualization/components/themes';
import { StyledVisContainer } from '@/components/adminer/graph-visualization/VisualizationView.styled';
import { GraphVisualizer } from '@/components/adminer/graph-visualization/GraphVisualizer';
import type { BasicNode, BasicRelationship } from '@/components/adminer/graph-visualization/types/types';
import type { GraphNode, GraphResponse, GraphResponseData } from '@/types/adminer/DataResponse';

/**
 * @param data The data to display
 * @param kindName Name of the current kind
 */
type DatabaseTableProps = Readonly<{
    data: GraphResponse;
    kind: string;
}>;

/**
 * Component for displaying data in graph
 */
export function DatabaseGraph({ data, kind }: DatabaseTableProps ) {
    const { theme } = usePreferences().preferences;

    const graph = useMemo(() => {
        if (!data.data)
            return;

        return {
            nodes: getNodes(data.data),
            relationships: getRelationships(data.data, kind),
        };
    }, [ data, kind ]);

    if (!graph || (!graph.nodes.length && !graph.relationships.length)) {
        return (
            <span>No records to display.</span>
        );
    }

    return (
        <div className='grow text-left'>
            <ArcThemeProvider theme={theme}>
                <StyledVisContainer isFullscreen={false}>
                    <GraphVisualizer
                        nodes={graph.nodes}
                        relationships={graph.relationships}
                        fetchedData={data}
                    />
                </StyledVisContainer>
            </ArcThemeProvider>
        </div>
    );
}

function getNodes(data: GraphResponseData): BasicNode[] {
    const nodes: BasicNode[] = [];

    for (const node of data.nodes) {
        const { labels, properties } = getNodeLabelsAndProperties(node.properties);

        const basicNode: BasicNode = {
            id: node.id,
            elementId: node.id,
            labels: labels,
            properties: properties,
        };
        nodes.push(basicNode);
    }

    return nodes;
}

function getRelationships(data: GraphResponseData, type: string): BasicRelationship[] {
    const relationships: BasicRelationship[] = [];
    for (const relationship of data.relationships) {
        const properties: Record<string, string> = {};
        for (const [ key, value ] of Object.entries(relationship.properties))
            properties[key] = value as string;

        const fromNode: GraphNode | undefined = data.nodes.find(node => node.id === relationship.fromNodeId);
        const toNode: GraphNode | undefined = data.nodes.find(node => node.id === relationship.toNodeId);

        const fromNodeProps = fromNode ? getNodeLabelsAndProperties(fromNode.properties) : undefined;
        const toNodeProps = toNode ? getNodeLabelsAndProperties(toNode.properties) : undefined;

        const basicRelationship: BasicRelationship = {
            id: relationship.id,
            elementId: relationship.id,
            startNodeId: relationship.fromNodeId,
            endNodeId: relationship.toNodeId,
            type: type,
            properties: properties,
            startNodeLabel: fromNodeProps?.labels ?? [],
            endNodeLabel: toNodeProps?.labels ?? [],
            startNodeProperties: fromNodeProps?.properties ?? {},
            endNodeProperties: toNodeProps?.properties ?? {},
        };
        relationships.push(basicRelationship);
    }
    return relationships;
}

function getNodeLabelsAndProperties(properties: Record<string, unknown>):
    { labels: string[], properties: Record<string, string> } {
    const props: Record<string, string> = {};
    let labels: string[] = [];

    for (const [ key, value ] of Object.entries(properties)) {
        if (key === '#labels')
            labels = value as string[];
        else
            props[key] = value as string;
    }
    return { labels: labels, properties: props };
}
