import { useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';
import { Button } from '@nextui-org/react';
import { AddIcon } from '@/components/icons/PlusIcon';
import { useLoaderData } from 'react-router-dom';
import { HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';
import { cn } from '@/components/utils';

export function DatasourcesPage() {
    const data = useLoaderData() as DatasourcesLoaderData;
    const [ datasources, setDatasources ] = useState(data.datasources);
    const [ isModalOpen, setIsModalOpen ] = useState(false);

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
        <div className='p-8 space-y-8'>
            {/* Header Section */}
            <div className='flex items-center justify-between'>
                <h1 className='text-2xl font-bold'>Datasources</h1>
                <Button
                    onPress={() => setIsModalOpen(true)}
                    color='primary'
                    startContent={<AddIcon />}
                >
                    Add Datasource
                </Button>
            </div>

            <DatasourcesInfoBanner className='mb-6' />

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
};

export function DatasourcesInfoBanner({ className }: DatasourcesInfoBannerProps) {
    // const { preferences, setPreferences } = usePreferences();
    // const [ isVisible, setIsVisible ] = useState(!preferences.dismissedDatasourcesGuide);

    // function handleClose() {
    //     setIsVisible(false);
    //     setPreferences({ ...preferences, dismissedDatasourcesGuide: true });
    // }

    // if (!isVisible) 
    //     return null;

    return (
        <div className={cn('relative bg-default-50 text-default-900 p-4 rounded-lg border border-default-300', className)}>
            <button 
                // onClick={handleClose} 
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
                Click <strong>"Add Datasource"</strong> to connect a new source. Once added, it will appear in the table below.
            </p>
        </div>
    );
}