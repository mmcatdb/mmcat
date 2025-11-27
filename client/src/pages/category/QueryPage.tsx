import { useLoaderData, useRouteLoaderData, type Params } from 'react-router-dom';
import { api } from '@/api';
import { PageLayout } from '@/components/RootLayout';
import { Query } from '@/types/query';
import { QueryDisplay } from '@/components/querying/QueryDisplay';
import { Datasource } from '@/types/Datasource';
import { routes } from '@/routes/routes';
import { type QueriesLoaderData } from './QueriesPage';
import { useMemo } from 'react';

export function QueryPage() {
    const { queries } = useRouteLoaderData(routes.category.queries.list.id) as QueriesLoaderData;
    const { query } = useLoaderData() as QueryLoaderData;

    const otherWeights = useMemo(() => queries.reduce((ans, q) => ans + q.finalWeight, 0), [ queries ]);

    return (
        <PageLayout className='space-y-2'>
            <h1 className='text-xl font-semibold'>{query.label}</h1>

            <QueryDisplay query={query} otherWeights={otherWeights} />
        </PageLayout>
    );
}

export type QueryLoaderData = {
    datasources: Datasource[];
    query: Query;
};

QueryPage.loader = async ({ params: { categoryId, queryId } }: { params: Params<'categoryId' | 'queryId'> }): Promise<QueryLoaderData> => {
    if (!categoryId)
        throw new Error('Category ID is required');
    if (!queryId)
        throw new Error('Query ID is required');

    const [ datasourcesResponse, queryResponse ] = await Promise.all([
        api.datasources.getAllDatasources({}, { categoryId }),
        api.queries.getQuery({ queryId }),
    ]);
    if (!datasourcesResponse.status || !queryResponse.status)
        throw new Error('Failed to load query info');

    return {
        datasources: datasourcesResponse.data.map(Datasource.fromResponse),
        query: Query.fromResponse(queryResponse.data),
    };
};
