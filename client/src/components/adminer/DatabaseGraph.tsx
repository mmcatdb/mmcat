import { useState, useEffect } from 'react';
import { ArcThemeProvider } from '@/components/adminer/graph-visualization/components/themes';
import { StyledVisContainer } from '@/components/adminer/graph-visualization/VisualizationView.styled';
import { GraphVisualizer } from '@/components/adminer/graph-visualization/GraphVisualizer';
import type { BasicNode, BasicRelationship } from '@/components/adminer/graph-visualization/types/types';
import type { GraphRelationship, GraphResponse, GraphResponseData } from '@/types/adminer/DataResponse';

function getNodes(data: GraphResponseData[]): BasicNode[] {
    const nodes: BasicNode[] = [];
    for (const element of data) {
        if ('#labels' in element) {
            const properties: Record<string, string> = {};

            for (const [ key, value ] of Object.entries(element.properties))
                properties[key] = value as string;

            const node: BasicNode = {
                id: element['#elementId'],
                elementId: element['#elementId'],
                labels: element['#labels'] as string[],
                properties: properties,
            };
            nodes.push(node);
        }
        else {
            const startNode: BasicNode = {
                id: element['#startNodeId'] as string,
                elementId: element['#startNodeId'] as string,
                labels: element['#labelsStartNode'] as string[],
                properties: {},
            };
            nodes.push(startNode);

            const endNode: BasicNode = {
                id: element['#endNodeId'] as string,
                elementId: element['#endNodeId'] as string,
                labels: element['#labelsEndNode'] as string[],
                properties: {},
            };
            nodes.push(endNode);
        }
    }
    return nodes;
}

function getRelationships(data: GraphResponseData[], type: string): BasicRelationship[] {
    const relationships: BasicRelationship[] = [];
    for (const element of data) {
        if ('#startNodeId' in element) {
            const relation: GraphRelationship = element as GraphRelationship;

            const properties: Record<string, string> = {};
            for (const [ key, value ] of Object.entries(relation.properties))
                properties[key] = value as string;

            const startNodeProperties: Record<string, string> = {};
            for (const [ key, value ] of Object.entries(relation.startNode))
                startNodeProperties[key] = value as string;

            const endNodeProperties: Record<string, string> = {};
            for (const [ key, value ] of Object.entries(relation.endNode))
                endNodeProperties[key] = value as string;

            const relationship: BasicRelationship = {
                id: relation['#elementId'],
                elementId: relation['#elementId'],
                startNodeId: relation['#startNodeId'],
                endNodeId: relation['#endNodeId'],
                type: type,
                properties: properties,
                startNodeLabel: relation['#labelsStartNode'],
                endNodeLabel: relation['#labelsEndNode'],
                startNodeProperties: startNodeProperties,
                endNodeProperties: endNodeProperties,
            };
            relationships.push(relationship);
        }
    }
    return relationships;
}

type DatabaseTableProps = Readonly<{
    fetchedData: GraphResponse;
    kind: string;
}>;

export function DatabaseGraph({ fetchedData, kind }: DatabaseTableProps ) {
    const [ nodes, setNodes ] = useState<BasicNode[]>(getNodes(fetchedData ? fetchedData.data : []));
    const [ relationships, setRelationships ] = useState<BasicRelationship[]>(getRelationships(fetchedData ? fetchedData.data : [], kind));

    useEffect(() => {
        if (fetchedData?.data) {
            setNodes(getNodes(fetchedData.data));
            setRelationships(getRelationships(fetchedData.data, kind));
        }
    }, [ fetchedData ]);

    return (
        <>
            {fetchedData && fetchedData.data.length > 0 ? (
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
