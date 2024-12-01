import { useEffect, useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { Outlet } from 'react-router-dom';
import { EmptyState } from '@/components/TableCommon';
import { Button } from '@nextui-org/react';
import { AddIcon } from '@/components/icons/PlusIcon';
import { LoadingPage } from './errorPages';

export function DatasourcesPage() {
    return (
        <div>
            <Outlet />
        </div>
    );
}

export function DatasourcesPageOverview() {
    const {
        datasources,
        loading,
        error,
        isModalOpen,
        setModalOpen,
        addDatasource,
        deleteDatasource,
    } = useDatasources();

    return (
        <DatasourcesPageOverviewUI
            datasources={datasources}
            loading={loading}
            error={error}
            isModalOpen={isModalOpen}
            onAddDatasource={addDatasource}
            onDeleteDatasource={deleteDatasource}
            onOpenModal={() => setModalOpen(true)}
            onCloseModal={() => setModalOpen(false)}
        />
    );
}

function useDatasources() {
    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);
    const [ isModalOpen, setModalOpen ] = useState(false);

    useEffect(() => {
        const fetchDatasources = async () => {
            setLoading(true);
            const response = await api.datasources.getAllDatasources({});
            
            if (response.status && response.data)
                setDatasources(response.data);
            else
                setError('Failed to load data');
            
            setLoading(false);
        };

        fetchDatasources();
    }, []);

    function addDatasource(newDatasource: Datasource) {
        setDatasources((prevDatasources) => [ ...prevDatasources, newDatasource ]);
    }

    async function deleteDatasource(id: string) {
        const response = await api.datasources.deleteDatasource({ id });

        if (!response.status) {
            toast.error('Failed to delete datasource.');
            return;
        }

        setDatasources((prevDatasources) =>
            prevDatasources.filter((datasource) => datasource.id !== id),
        );
    }

    return {
        datasources,
        loading,
        error,
        isModalOpen,
        setModalOpen,
        addDatasource,
        deleteDatasource,
    };
}

type DatasourcesPageOverviewProps = {
    datasources: Datasource[];
    loading: boolean;
    error: string | null;
    isModalOpen: boolean;
    onAddDatasource: (newDatasource: Datasource) => void;
    onDeleteDatasource: (id: string) => void;
    onOpenModal: () => void;
    onCloseModal: () => void;
};

function DatasourcesPageOverviewUI({
    datasources,
    loading,
    error,
    isModalOpen,
    onAddDatasource,
    onDeleteDatasource,
    onOpenModal,
    onCloseModal,
}: DatasourcesPageOverviewProps) {
    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1>Datasources</h1>
                <Button 
                    onPress={onOpenModal}
                    color='primary' 
                    startContent={<AddIcon />}
                    isDisabled={loading}
                >
                    Add Datasource
                </Button>
            </div>

            <div className='mt-5'>
                {loading ? (
                    <LoadingPage />
                ) : datasources.length > 0 ? (
                    <DatasourcesTable
                        datasources={datasources}
                        loading={loading}
                        error={error}
                        onDeleteDatasource={onDeleteDatasource}
                    />
                ) : (
                    <EmptyState
                        message='No datasources available.'
                        buttonText='+ Add Datasource'
                        onButtonClick={onOpenModal}
                    />
                )}
            </div>

            <DatasourceModal 
                isOpen={isModalOpen} 
                onClose={onCloseModal}
                onDatasourceCreated={onAddDatasource}
            />
        </div>
    );
}
