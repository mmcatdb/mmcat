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

export type GraphResponseData = {
    elementId: string;
    properties: Record<string, unknown>;
    lables: string[];
    [key: string]: unknown;
} | {
    elementId: string;
    properties: Record<string, unknown>;
    startNodeId: string;
    endNodeId: string;
    [key: string]: unknown;
};
