import { api } from '@/api';
import { PageLayout } from '@/components/RootLayout';
import { useSearchParams, type Params } from 'react-router-dom';
import { QueryDisplay } from '@/components/querying/QueryDisplay';

const QUERY_PARAM = 'query';

export function NewQueryPage() {
    const [ searchParams ] = useSearchParams();
    const defaultQueryString = searchParams.get(QUERY_PARAM) ?? undefined;

    function onOutput(queryString: string) {
        if (queryString === defaultQueryString)
            return;

        const next = new URLSearchParams(searchParams);
        next.set(QUERY_PARAM, queryString);
        window.history.pushState({}, '', '?' + next);
    }

    return (
        <PageLayout className='space-y-2'>
            <h1 className='text-xl font-semibold'>New Query</h1>

            <QueryDisplay defaultQueryString={defaultQueryString} onOutput={onOutput} />
        </PageLayout>
    );
}

NewQueryPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }) => {
    if (!categoryId)
        throw new Error('Category ID is required');

    const response = await api.datasources.getAllDatasources({}, { categoryId });

    if (!response.status)
        throw new Error('Failed to load datasources');

    return {
        datasources: response.data,
    };
};
