import { useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';
import { Button, Tooltip } from '@nextui-org/react';
import { AddIcon } from '@/components/icons/PlusIcon';
import { useLoaderData } from 'react-router-dom';
import { HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';
import { cn } from '@/components/utils';
import { useBannerState } from '@/types/utils/useBannerState';
import { IoInformationCircleOutline } from 'react-icons/io5';

export function DatasourcesPage() {
    const data = useLoaderData() as DatasourcesLoaderData;
    const [ datasources, setDatasources ] = useState(data.datasources);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const { isVisible, dismissBanner, restoreBanner } = useBannerState('datasources-page');

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
        <div className='p-4'>
            {/* Header Section */}
            <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                    <h1 className='text-2xl font-semibold'>Datasources</h1>
                    <Tooltip content={isVisible ? 'Hide info' : 'Show info'}>
                        <button
                            onClick={isVisible ? dismissBanner : restoreBanner}
                            className='text-primary-500 hover:text-primary-700 transition'
                        >
                            <IoInformationCircleOutline className='w-6 h-6' />
                        </button>
                    </Tooltip>
                </div>
                <Button
                    onPress={() => setIsModalOpen(true)}
                    color='primary'
                    startContent={<AddIcon />}
                >
                    Add Datasource
                </Button>
            </div>

            {isVisible && <DatasourcesInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

            {/* Table Section */}
            {datasources.length > 0 ? (
                <DatasourcesTable datasources={datasources} deleteDatasource={deleteDatasource} />
            ) : (
                <EmptyState
                    message='No datasources available.'
                    buttonText='+ Add Datasource'
                    onButtonClick={() => setIsModalOpen(true)}
                />
            )}

            <DatasourceModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onDatasourceCreated={onDatasourceCreated}
            />
        </div>
    );
}

DatasourcesPage.loader = datasourcesLoader;

export type DatasourcesLoaderData = {
    datasources: Datasource[];
};

async function datasourcesLoader(): Promise<DatasourcesLoaderData> {
    const response = await api.datasources.getAllDatasources({});
    if (!response.status)
        throw new Error('Failed to load datasources');

    return {
        datasources: response.data.map(Datasource.fromServer),
    };
}

type DatasourcesInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

export function DatasourcesInfoBanner({ className, dismissBanner }: DatasourcesInfoBannerProps) {
    return (
        <div className={cn('relative', className)}>
            <div className={cn('relative bg-default-50 text-default-900 p-4 rounded-lg border border-default-300')}>
                <button 
                    onClick={dismissBanner}
                    className='absolute top-2 right-2 text-default-500 hover:text-default-700 transition'
                >
                    <HiXMark className='w-5 h-5' />
                </button>

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
            </div>
        </div>
    );
}