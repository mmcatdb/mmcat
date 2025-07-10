export type DataResponse = TableResponse | DocumentResponse | GraphResponse;

export type TableResponse = {
    type: 'table';
    metadata: {
        itemCount: number;
        propertyNames: string[];
        [key: string]: unknown;
    };
    data: string[][];
};

export type DocumentResponse = {
    type: 'document';
    metadata: {
        itemCount: number;
        propertyNames: string[];
        [key: string]: unknown;
    };
    data: Record<string, unknown>[];
};

export type GraphResponse = {
    type: 'graph';
    metadata: {
        itemCount: number;
        propertyNames: string[];
        [key: string]: unknown;
    };
    data: GraphResponseData;
};

export type GraphResponseData = {
    nodes: GraphNode[];
    relationships: GraphRelationship[];
};

export type GraphNode = {
    type: 'node';
    id: string;
    properties: Record<string, unknown>;
};

export type GraphRelationship = {
    type: 'relationship';
    id: string;
    fromNodeId: string;
    toNodeId: string;
    properties: Record<string, unknown>;
};

export type GraphRelationshipWithNodes = {
    id: string;
    properties: Record<string, unknown>;
    from: GraphNode;
    to: GraphNode;
}

export type ErrorResponse = {
    message: string;
};
