export type BackendResponse = BackendTableResponse | BackendDocumentResponse | BackendGraphResponse;

export type BackendTableResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: string[];
};

type BackendDocumentResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: Record<string, unknown>[];
};

type BackendGraphResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: {
        properties: Record<string, unknown>;
        [key: string]: unknown;
    }[];
};
