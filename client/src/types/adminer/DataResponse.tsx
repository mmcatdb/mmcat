// FIXME Opět to samé co na BE. Navíc tu máte pouze GraphNode a GraphRelationship, ne ExtendedGraphRelationship. Proč jsou na BE tři typy a tady pouze dva?
// Nejspíš bych ještě do každého typu přidal property "type", která bude buď table, document, nebo graph. Teď se to dá sice vyčíst z dat, ale nejde to úplně snadno.

export type DataResponse = TableResponse | DocumentResponse | GraphResponse;

export type KindNameResponse = {
    data: string[];
}

export type TableResponse = {
    metadata: {
        itemCount: number;
        propertyNames: string[] | undefined;
        [key: string]: unknown;
    };
    data: Record<string, string>[];
};

export type DocumentResponse = {
    metadata: {
        itemCount: number;
        propertyNames: string[] | undefined;
        [key: string]: unknown;
    };
    data: Record<string, unknown>[];
};

export type GraphResponse = {
    metadata: {
        itemCount: number;
        propertyNames: string[] | undefined;
        [key: string]: unknown;
    };
    data: GraphResponseData[];
};

export type GraphResponseData = GraphNode | GraphRelationship;

type GraphNode = {
    '#elementId': string;
    properties: Record<string, unknown>;
    '#lables': string[];
    [key: string]: unknown;
};

export type GraphRelationship = {
    '#elementId': string;
    properties: Record<string, unknown>;
    '#startNodeId': string;
    '#endNodeId': string;
    '#labelsStartNode': string[];
    '#labelsEndNode': string[];
    startNode: Record<string, unknown>;
    endNode: Record<string, unknown>;
    [key: string]: unknown;
};

export type ErrorResponse = {
    message: string;
};
