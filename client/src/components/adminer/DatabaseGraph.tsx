import { useState, useEffect } from 'react';
import { ArcThemeProvider } from '@/components/adminer/graph-visualization/components/themes';
import { StyledVisContainer } from '@/components/adminer/graph-visualization/VisualizationView.styled';
import { GraphVisualizer } from '@/components/adminer/graph-visualization/GraphVisualizer';
import type { BasicNode, BasicRelationship } from '@/components/adminer/graph-visualization/types/types';
import type { GraphResponse, GraphResponseData } from '@/types/adminer/DataResponse';

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
                labels: [ element['#startNodeId'] as string ],
                properties: {},
            };
            nodes.push(startNode);

            const endNode: BasicNode = {
                id: element['#endNodeId'] as string,
                elementId: element['#endNodeId'] as string,
                labels: [ element['#endNodeId'] as string ],
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
            const properties: Record<string, string> = {};

            for (const [ key, value ] of Object.entries(element.properties))
                properties[key] = value as string;

            const relationship: BasicRelationship = {
                id: element['#elementId'],
                elementId: element['#elementId'],
                startNodeId: element['#startNodeId'] as string,
                endNodeId: element['#endNodeId'] as string,
                type: type,
                properties: properties,
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
    const [ nodes, setNodes ] = useState<BasicNode[]>(getNodes(fetchedData.data));
    const [ relationships, setRelationships ] = useState<BasicRelationship[]>(getRelationships(fetchedData.data, kind));

    useEffect(() => {
        setNodes(getNodes(fetchedData.data));
        setRelationships(getRelationships(fetchedData.data, kind));
    }, [ fetchedData ]);

    return (
        <div>
            {fetchedData && fetchedData.data.length > 0 ? (
                <div className='max-w-[1280px] text-left h-[400px]'>
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
        </div>
    );
}
