import { useEffect, useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import { Datasource } from '@/types/Datasource';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';
import { Button, Tooltip } from '@heroui/react';
import { useLoaderData, useLocation, useNavigate } from 'react-router-dom';
import { GoDotFill } from 'react-icons/go';
import { useBannerState } from '@/types/utils/useBannerState';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { InfoBanner } from '@/components/common';
import { FaPlus } from 'react-icons/fa';
import { PageLayout } from '@/components/RootLayout';

export function DatasourcesPage() {
    const data = useLoaderData() as DatasourcesLoaderData;
    const [ datasources, setDatasources ] = useState(data.datasources);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const { isVisible, dismissBanner, restoreBanner } = useBannerState('datasources-page');
    const location = useLocation();
    const navigate = useNavigate();

    // Open modal if navigated with state.openModal
    useEffect(() => {
        if (location.state?.openModal) {
            setIsModalOpen(true);
            // Clear state to prevent re-opening on refresh
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [ location, navigate ]);

    function onDatasourceCreated(newDatasource: Datasource) {
        setDatasources(prev => [ ...prev, newDatasource ]);
    }

    async function deleteDatasource(id: string) {
        const response = await api.datasources.deleteDatasource({ id });

        if (!response.status) {
            toast.error('Failed to delete datasource.');
            return;
        }

        setDatasources(prev => prev.filter(datasource => datasource.id !== id));
    }

    return (
        <PageLayout>
            {/* Header Section */}
            <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                    <h1 className='text-xl font-semibold'>Datasources</h1>
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
                    onPress={() => setIsModalOpen(true)}
                    color='primary'
                    startContent={<FaPlus className='size-3' />}
                >
                    Add Datasource
                </Button>
            </div>

            {isVisible && <DatasourcesInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

            {/* Table Section */}
            {datasources.length > 0 ? (
                <DatasourcesTable
                    datasources={datasources}
                    deleteDatasource={id => void deleteDatasource(id)}
                    datasourcesWithMappings={data.datasourcesWithMappings}
                />
            ) : (
                <EmptyState
                    message='No datasources available. Create one to get started.'
                    buttonText='+ Add Datasource'
                    onClick={() => setIsModalOpen(true)}
                />
            )}

            <DatasourceModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onDatasourceCreated={onDatasourceCreated}
            />
        </PageLayout>
    );
}

DatasourcesPage.loader = datasourcesLoader;

export type DatasourcesLoaderData = {
    datasources: Datasource[];
    datasourcesWithMappings: string[]; // IDs of datasources with mappings
};

async function datasourcesLoader(): Promise<DatasourcesLoaderData> {
    const [ datasourcesResponse, mappingsResponse ] = await Promise.all([
        api.datasources.getAllDatasources({}),
        api.mappings.getAllMappings({}),
    ]);

    if (!datasourcesResponse.status || !mappingsResponse.status)
        throw new Error('Failed to load datasources');

    const datasourceIdsWithMappings = new Set(
        mappingsResponse.data.map(m => m.datasourceId),
    );

    return {
        datasources: datasourcesResponse.data.map(Datasource.fromResponse),
        datasourcesWithMappings: Array.from(datasourceIdsWithMappings),
    };
}

type DatasourcesInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

export function DatasourcesInfoBanner({ className, dismissBanner }: DatasourcesInfoBannerProps) {
    return (
        <InfoBanner className={className} dismissBanner={dismissBanner}>
            <h2 className='text-lg font-semibold mb-2'>Understanding Data Sources</h2>
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
            </p>
        </InfoBanner>
    );
}
