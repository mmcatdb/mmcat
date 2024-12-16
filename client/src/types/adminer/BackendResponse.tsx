export type DataResponse = TableResponse | DocumentResponse | GraphResponse;

export type TableResponse = {
    metadata: {
        itemCount: number;
        [key: string]: unknown;
    };
    data: string[];
};

type DocumentResponse = {
    metadata: {
        itemCount: number;
        [key: string]: unknown;
    };
    data: Record<string, unknown>[];
};

type GraphResponse = {
    metadata: {
        itemCount: number;
        [key: string]: unknown;
    };
    data: {
        properties: Record<string, unknown>;
        [key: string]: unknown;
    }[];
};
