import type { TableResponse, GraphResponse, GraphRelationshipWithNodes, GraphNode, DocumentResponse, GraphResponseData } from '@/types/adminer/DataResponse';

const ID = '#id';
const LABELS = '#labels';
const FROM_NODE_PREFIX = 'from.';
const TO_NODE_PREFIX = 'to.';

export function getTableFromGraphData(graphData: GraphResponse): { data: TableResponse, columnNames: string[]} {
    const modifiedData = { type: 'table', metadata: graphData.metadata, data: [] } as TableResponse;

    const fetchedPropertyNames: string[] = graphData.metadata.propertyNames;

    if (graphData.data.relationships.length === 0){
        modifiedData.metadata.propertyNames = [ ID, LABELS ];

        for (const propertyName of fetchedPropertyNames) {
            if (!modifiedData.metadata.propertyNames.includes(propertyName))
                modifiedData.metadata.propertyNames.push(propertyName);
        }

        const tableColumnNames = modifiedData.metadata.propertyNames.filter(name => !name.includes(`${LABELS} - `));

        const data: string[][] = getNodesData(graphData.data.nodes, tableColumnNames);
        modifiedData.data = data;
        modifiedData.metadata.propertyNames = modifiedData.metadata.propertyNames.filter(name =>
            name != LABELS,
        );

        return { data: modifiedData, columnNames: tableColumnNames };
    }
    else {
        const { graph, propertyNames } = getRelationshipsWithNodes(fetchedPropertyNames, graphData.data);

        modifiedData.metadata.propertyNames = propertyNames;

        const tableColumnNames = propertyNames.filter(name => !name.includes(`${LABELS} - `));

        const data: string[][] = getRelationshipsData(graph, tableColumnNames);

        modifiedData.data = data;
        modifiedData.metadata.propertyNames = modifiedData.metadata.propertyNames.filter(name =>
            name != `${FROM_NODE_PREFIX}${LABELS}` && name != `${TO_NODE_PREFIX}${LABELS}`,
        );

        return { data: modifiedData, columnNames: tableColumnNames };
    }
}

export function getDocumentFromGraphData(graphData: GraphResponse): DocumentResponse {
    const modifiedData = { type: 'document', metadata: graphData.metadata, data: [] } as DocumentResponse;

    const fetchedPropertyNames = graphData.metadata.propertyNames;

    if (graphData.data.relationships.length === 0){
        modifiedData.metadata.propertyNames = [ ID ];

        for (const propertyName of fetchedPropertyNames) {
            if (!modifiedData.metadata.propertyNames.includes(propertyName))
                modifiedData.metadata.propertyNames.push(propertyName);
        }

        modifiedData.data = graphData.data.nodes;
    }
    else {
        const { graph, propertyNames } = getRelationshipsWithNodes(fetchedPropertyNames, graphData.data);

        modifiedData.metadata.propertyNames = propertyNames;

        modifiedData.data = graph;
    }

    return modifiedData;
}

function getNodesData(nodes: GraphNode[], propertyNames: string[]): string[][] {
    const data: string[][] = [];

    for (const node of nodes) {
        const nodeData: string[] = [];
        for (const propertyName of propertyNames) {
            if (propertyName === ID)
                nodeData.push(node.id);
            else if (Object.keys(node.properties).includes(propertyName))
                nodeData.push(node.properties[propertyName] as string);
            else
                nodeData.push('');
        }
        data.push(nodeData);
    }

    return data;
}

function getRelationshipsWithNodes(fetchedPropertyNames: string[], data: GraphResponseData):
    { graph: GraphRelationshipWithNodes[], propertyNames: string[] } {
    const graph: GraphRelationshipWithNodes[] = [];
    const propertyNames: string[] = [ ID, `${FROM_NODE_PREFIX}${ID}`, `${TO_NODE_PREFIX}${ID}` ];

    for (const propertyName of fetchedPropertyNames) {
        if (!propertyNames.includes(propertyName))
            propertyNames.push(propertyName);
    }

    for (const relationship of data.relationships) {
        const fromNode: GraphNode = data.nodes.find(node => node.id === relationship.fromNodeId)
            ?? { type: 'node', id: relationship.fromNodeId, properties: {} };
        const toNode: GraphNode = data.nodes.find(node => node.id === relationship.toNodeId)
            ?? { type: 'node', id: relationship.toNodeId, properties: {} };
        const relationshipWithNodes: GraphRelationshipWithNodes = {
            id: relationship.id,
            properties: relationship.properties,
            from: fromNode,
            to: toNode,
        };
        graph.push(relationshipWithNodes);

        for (const property of Object.keys(fromNode.properties)) {
            const fullPropName = `${FROM_NODE_PREFIX}${property}`;
            if (!propertyNames.includes(fullPropName))
                propertyNames.push(fullPropName);
        }

        for (const property of Object.keys(toNode.properties)) {
            const fullPropName = `${TO_NODE_PREFIX}${property}`;
            if (!propertyNames.includes(fullPropName))
                propertyNames.push(fullPropName);
        }
    }

    return { graph: graph, propertyNames: propertyNames };
}

function getRelationshipsData(graph: GraphRelationshipWithNodes[], propertyNames: string[]): string[][] {
    const data: string[][] = [];

    for (const relationship of graph) {
        const relationshipData: string[] = [];

        for (const property of propertyNames)
            relationshipData.push(getPropertyValue(property, relationship));


        data.push(relationshipData);
    }

    return data;
}

function getPropertyValue(property: string, relationship: GraphRelationshipWithNodes): string {
    if (property === ID)
        return relationship.id;


    if (property.startsWith(FROM_NODE_PREFIX)){
        const propertyName = property.substring(FROM_NODE_PREFIX.length);

        if (propertyName === ID)
            return relationship.from.id;

        return relationship.from.properties[propertyName] as string ?? '';

    }

    if (property.startsWith(TO_NODE_PREFIX)){
        const propertyName = property.substring(TO_NODE_PREFIX.length);

        if (propertyName === ID)
            return relationship.to.id;

        return relationship.to.properties[propertyName] as string ?? '';
    }

    return relationship.properties[property] as string ?? '';
}
