import { useEffect, useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { Outlet } from 'react-router-dom';

export function DatasourcesPage() {
    return (
        <div>
            <Outlet />
        </div>
    );
}

export function DatasourcesPageOverview() {
    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        const fetchDatasources = async () => {
            try {
                setLoading(true);
                const response = await api.datasources.getAllDatasources({});
                if (response.status && response.data)
                    setDatasources(response.data);
                else
                    setError('Failed to load data');
            }
            catch (err) {
                setError('Failed to load data');
            }
            finally {
                setLoading(false);
            }
        };

        fetchDatasources();
    }, []);

    // callback to add new datasource
    const handleAddDatasource = (newDatasource: Datasource) => {
        setDatasources((prevDatasources) => [ ...prevDatasources, newDatasource ]);
    };

    // callback to delete a datasource
    const handleDeleteDatasource = async (id: string) => {
        try {
            const response = await api.datasources.deleteDatasource({ id });

            if (response.status) {
                setDatasources((prevDatasources) =>
                    prevDatasources.filter((datasource) => datasource.id !== id),
                );
            }
            else {
                toast.error('Failed to delete datasource. Please try again.');
            }
        }
        catch (error) {
            console.error('Error deleting datasource:', error);
            toast.error('An error occurred while deleting the datasource.');
        }
    };

    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1>Datasources</h1>
                <DatasourceModal onDatasourceCreated={handleAddDatasource} />
            </div>

            <div className='mt-5'>
                <DatasourcesTable
                    datasources={datasources}
                    loading={loading}
                    error={error}
                    onDeleteDatasource={handleDeleteDatasource}
                />
            </div>
        </div>
    );
}
