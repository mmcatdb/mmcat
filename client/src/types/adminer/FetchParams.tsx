export type FetchParams = FetchKindParams | FetchDatasourceParams

type FetchDatasourceParams = {
    datasourceId: string;
}

export type FetchKindParams = {
    datasourceId: string;
    kindName: string;
    queryParams: {
        filters?: string[];
        limit: number;
        offset: number;
    };
}
