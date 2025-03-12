import { useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';
import { Button, Card } from '@nextui-org/react';
import { AddIcon } from '@/components/icons/PlusIcon';
import { useLoaderData } from 'react-router-dom';

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
