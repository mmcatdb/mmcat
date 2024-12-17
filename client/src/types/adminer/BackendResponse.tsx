export type DataResponse = TableResponse | DocumentResponse | GraphResponse;

export type TableResponse = {
    metadata: {
        itemCount: number;
        propertyNames: string[] | undefined;
        [key: string]: unknown;
    };
    data: string[];
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
    data: {
        properties: Record<string, unknown>;
        [key: string]: unknown;
    }[];
};
