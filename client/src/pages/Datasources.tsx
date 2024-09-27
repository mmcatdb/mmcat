import { useEffect, useState } from 'react';
import { CommonPage } from '@/components/CommonPage';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';

export function DatasourcesPage() {
    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

    // console.log("On render", datasources);

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

    return (
        <CommonPage>
            <div className='flex items-center justify-between'>
                <h1 className='text-3xl font-bold leading-tight'>Datasources</h1>
                <DatasourceModal onDatasourceCreated={handleAddDatasource} />
            </div>

            <div className='mt-5'>
                <DatasourcesTable datasources={datasources} loading={loading} error={error} />
            </div>
        </CommonPage>
    );
}
