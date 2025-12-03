import { useCallback, useState } from 'react';
import { QueriesTable } from '@/components/querying/QueriesTable';
import { api } from '@/api';
import { Query } from '@/types/query';
import { EmptyState } from '@/components/TableCommon';
import { Button } from '@heroui/react';
import { Link, type Params, useRouteLoaderData } from 'react-router-dom';
// import { GoDotFill } from 'react-icons/go';
import { useBannerState } from '@/types/utils/useBannerState';
import { InfoBanner, InfoTooltip } from '@/components/common';
import { FaPlus } from 'react-icons/fa';
import { PageLayout } from '@/components/RootLayout';
import { type Id } from '@/types/id';
import { routes } from '@/routes/routes';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';

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
                    startContent={<FaPlus className='size-4' />}
                >
                    Add Query
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
                    buttonText='Start Quering'
                    buttonStartContent={<FaPlus className='size-4' />}
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
        {/* TODO */}
        {/* <h2 className='text-lg font-semibold mb-2'>Understanding Data Sources</h2>
            <p className='text-sm'>
                A <span className='font-bold'>Datasource</span> represents where your data is stored. You can <span className='font-bold'>import from</span> or <span className='font-bold'>export to</span> different sources, including databases and files.
            </p>

            <ul className='mt-3 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Databases:</span> MongoDB, PostgreSQL, Neo4j.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Files:</span> CSV, JSON, JSON-LD.
                </li>
            </ul>

            <p className='text-sm mt-3'>
                Click <span className='font-bold'>&quot;+ Add Datasource&quot;</span> to connect a new source. Once added, it will appear in the table below.
            </p> */}

        <p>
            TODO
        </p>
    </>);
}
