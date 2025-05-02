import { useState, useEffect } from 'react';
import { ArcThemeProvider } from '@/components/adminer/graph-visualization/components/themes';
import { StyledVisContainer } from '@/components/adminer/graph-visualization/VisualizationView.styled';
import { GraphVisualizer } from '@/components/adminer/graph-visualization/GraphVisualizer';
import type { BasicNode, BasicRelationship } from '@/components/adminer/graph-visualization/types/types';
import type { GraphNode, GraphResponse, GraphResponseData } from '@/types/adminer/DataResponse';

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
        if (key === 'labels')
            labels = value as string[];
        else
            props[key] = value as string;
    }
    return { labels: labels, properties: props };
}

type DatabaseTableProps = Readonly<{
    fetchedData: GraphResponse;
    kind: string;
}>;

export function DatabaseGraph({ fetchedData, kind }: DatabaseTableProps ) {
    const [ nodes, setNodes ] = useState<BasicNode[]>(fetchedData?.data ? getNodes(fetchedData.data) : []);
    const [ relationships, setRelationships ] = useState<BasicRelationship[]>(fetchedData?.data ? getRelationships(fetchedData.data, kind) : []);

    useEffect(() => {
        if (fetchedData?.data) {
            setNodes(getNodes(fetchedData.data));
            setRelationships(getRelationships(fetchedData.data, kind));
        }
    }, [ fetchedData ]);

    return (
        <>
            {fetchedData && (fetchedData.data.nodes.length > 0 || fetchedData.data.relationships.length > 0) ? (
                <div className='grow text-left'>
                    <ArcThemeProvider theme={'dark'}>
                        <StyledVisContainer isFullscreen={false}>
                            <GraphVisualizer
                                nodes={nodes}
                                relationships={relationships}
                                fetchedData={fetchedData}
                            />
                        </StyledVisContainer>
                    </ArcThemeProvider>
                </div>
            ) : (
                <span>No records to display.</span>
            )}
        </>
    );
}
