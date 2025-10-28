import { useCallback, useState } from 'react';
import { QueriesTable } from '@/components/querying/QueriesTable';
import { api } from '@/api';
import { Query } from '@/types/query';
import { EmptyState } from '@/components/TableCommon';
import { Button, Tooltip } from '@heroui/react';
import { Link, type Params, useLoaderData } from 'react-router-dom';
// import { GoDotFill } from 'react-icons/go';
import { useBannerState } from '@/types/utils/useBannerState';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { InfoBanner } from '@/components/common';
import { FaPlus } from 'react-icons/fa';
import { PageLayout } from '@/components/RootLayout';
import { type Id } from '@/types/id';
import { routes } from '@/routes/routes';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';

export function QueriesPage() {
    const { category } = useCategoryInfo();
    const data = useLoaderData() as QueriesLoaderData;
    const [ queries, setQueries ] = useState(data.queries);
    const { isVisible, dismissBanner, restoreBanner } = useBannerState('queries-page');

    const onDelete = useCallback((id: Id) => {
        setQueries(prev => prev.filter(query => query.id !== id));
    }, []);

    return (
        <PageLayout>
            {/* Header Section */}
            <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                    <h1 className='text-xl font-semibold'>Queries</h1>
                    <Tooltip content={isVisible ? 'Hide info' : 'Show info'}>
                        <button
                            onClick={isVisible ? dismissBanner : restoreBanner}
                            className='text-primary-500 hover:text-primary-700 transition'
                        >
                            <IoInformationCircleOutline className='size-6' />
                        </button>
                    </Tooltip>
                </div>


                <Button
                    as={Link}
                    to={routes.category.queries.new.resolve({ categoryId: category.id })}
                    color='primary'
                    startContent={<FaPlus className='size-3' />}
                >
                    Start querying
                </Button>
            </div>

            {isVisible && <QueriesInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

            {/* Table Section */}
            {queries.length > 0 ? (
                <QueriesTable
                    queries={queries}
                    onDelete={onDelete}
                />
            ) : (
                <EmptyState
                    message='No queries available. Create one to get started.'
                    buttonText='+ Start Quering'
                    to={routes.category.queries.new.resolve({ categoryId: category.id })}
                />
            )}
        </PageLayout>
    );
}

type QueriesLoaderData = {
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

type QueriesInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

function QueriesInfoBanner({ className, dismissBanner }: QueriesInfoBannerProps) {
    return (
        <InfoBanner className={className} dismissBanner={dismissBanner}>
            {/* TODO */}
            {/* <h2 className='text-lg font-semibold mb-2'>Understanding Data Sources</h2>
            <p className='text-sm'>
                A <strong>Datasource</strong> represents where your data is stored. You can <strong>import from</strong> or <strong>export to</strong> different sources, including databases and files.
            </p>

            <ul className='mt-3 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Databases:</strong> MongoDB, PostgreSQL, Neo4j.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Files:</strong> CSV, JSON, JSON-LD.
                </li>
            </ul>

            <p className='text-sm mt-3'>
                Click <strong>&quot;+ Add Datasource&quot;</strong> to connect a new source. Once added, it will appear in the table below.
            </p> */}

            <p>
                TODO
            </p>
        </InfoBanner>
    );
}
