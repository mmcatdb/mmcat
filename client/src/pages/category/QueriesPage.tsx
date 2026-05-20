import { useCallback, useState } from 'react';
import { QueriesTable } from '@/components/querying/QueriesTable';
import { api } from '@/api';
import { Query } from '@/types/query';
import { EmptyState } from '@/components/common/tableComponents';
import { Button } from '@heroui/react';
import { Link, type Params, useRouteLoaderData } from 'react-router-dom';
import { useBannerState } from '@/types/utils/useBannerState';
import { InfoBanner, InfoTooltip } from '@/components/common/components';
import { FaPlus } from 'react-icons/fa';
import { PageLayout } from '@/components/RootLayout';
import { type Id } from '@/types/id';
import { routes } from '@/routes/routes';
import { useCategoryInfo } from '@/components/context/CategoryInfoProvider';

export function QueriesPage() {
    const { category } = useCategoryInfo();
    const data = useRouteLoaderData(routes.category.queries.list.id) as QueriesLoaderData;
    const [ queries, setQueries ] = useState(data.queries);
    const banner = useBannerState('queries-page');

    const onDelete = useCallback((id: Id) => {
        setQueries(prev => prev.filter(query => query.id !== id));
    }, []);

    return (
        <PageLayout>
            {/* Header Section */}
            <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                    <h1 className='text-xl font-semibold'>Queries</h1>

                    <InfoTooltip {...banner} />
                </div>

                <Button
                    as={Link}
                    to={routes.category.queries.new.resolve({ categoryId: category.id })}
                    color='primary'
                >
                    <FaPlus className='size-4' /> Add Query
                </Button>
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <QueriesInfoInner />
            </InfoBanner>

            {/* Table Section */}
            {queries.length > 0 ? (
                <QueriesTable queries={queries} onDelete={onDelete} />
            ) : (
                <EmptyState
                    message='No queries available. Create one to get started.'
                    button={<><FaPlus className='size-4' /> Start Quering</>}
                    to={routes.category.queries.new.resolve({ categoryId: category.id })}
                />
            )}
        </PageLayout>
    );
}

export type QueriesLoaderData = {
    queries: Query[];
};

QueriesPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<QueriesLoaderData> => {
    if (!categoryId)
        throw new Error('Category ID is required');

    const response = await api.queries.getQueriesInCategory({ categoryId });
    if (!response.status)
        throw new Error('Failed to load queries');

    return {
        queries: response.data.map(Query.fromResponse),
    };
};

function QueriesInfoInner() {
    return (<>
        <h2>Understanding Queries</h2>

        <p>
            A <span className='font-bold'>Query</span> is a an expression in <span className='font-bold'>MMQL</span> (Multi-Model Query Language), which is a SPARQL-like language over the <span className='font-bold'>Schema Category</span>.
        </p>

        <ul>
            {/* TODO Maybe something about the version? However, we would need to change how it works - because now it's red if its < system version, so only one query can be up-to-date at a time. We should make it red only if it's not compatible with the current system version instead.

<li>
<span className='font-bold'>Version:</span> The
</li> */}
            <li>
                <span className='font-bold'>Weight:</span> Indicates the importance of the query during adaptation. By default corresponds to the number of times the query has been executed, but can be adjusted manually.
            </li>
        </ul>

        <p>
            Click on <span className='font-bold'>&quot;+ Add Query&quot;</span> to create a new query. You can then save the query to re-execute it later as needed. Saved queries will be automatically updated whenever the schema category changes.
        </p>
    </>);
}
