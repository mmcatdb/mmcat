export type BackendArrayResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: string[];
};

export type BackendObjectResponse = {
    metadata: {
        rowCount: number;
        [key: string]: unknown;
    };
    data: Record<string, unknown>[];
};
