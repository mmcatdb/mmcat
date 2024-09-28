export type BackendTableResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: string[];
};

export type BackendDocumentResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: Record<string, unknown>[];
};

export type BackendGraphResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: {
        properties: Record<string, unknown>;
        [key: string]: unknown;
    }[];
};
