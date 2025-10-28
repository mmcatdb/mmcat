import { useLoaderData, type Params } from 'react-router-dom';
import { api } from '@/api';
import { PageLayout } from '@/components/RootLayout';
import { Query } from '@/types/query';
import { QueryDisplay } from '@/components/querying/QueryDisplay';
import { Datasource } from '@/types/Datasource';

export function QueryPage() {
    const { query } = useLoaderData() as QueryLoaderData;

    return (
        <PageLayout className='space-y-2'>
            <h1 className='text-xl font-semibold'>{query.label}</h1>

            <QueryDisplay query={query} />
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
        api.datasources.getAllDatasources({}, { categoryId: categoryId }),
        api.queries.getQuery({ queryId }),
    ]);
    if (!datasourcesResponse.status || !queryResponse.status)
        throw new Error('Failed to load query info');

    return {
        datasources: datasourcesResponse.data.map(Datasource.fromResponse),
        query: Query.fromResponse(queryResponse.data),
    };
};
