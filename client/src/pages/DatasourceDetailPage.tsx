import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { ErrorPage } from '@/pages/errorPages';
import { Spinner } from '@nextui-org/react';

type DatasourceDetailProps = {
    datasourceId: string;
}

export const DatasourceDetailPage = () => {
    const { id } = useParams<{ id: string }>();

    if (!id) 
        return <ErrorPage />;

    return <DatasourceDetail datasourceId={id} />;
};

export const DatasourceInCategoryDetailPage = () => {
    const { categoryId, id } = useParams<{ categoryId: string, id: string }>();

    if (!categoryId || !id) 
        return <ErrorPage />;

    return (
        <div>
            <DatasourceDetail datasourceId={id} />
            {/* TODO: add mappings table */}
            <div className='mt-6'>
                <p className='text-xl'>Mappings table</p>
            </div>
        </div>
    );
};

export const DatasourceDetail = ({ datasourceId }: DatasourceDetailProps) => {
    const [ datasource, setDatasource ] = useState<Datasource | null>(null);
    const [ loading, setLoading ] = useState(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        const fetchDatasource = async () => {
            try {
                setLoading(true);
                const response = await api.datasources.getDatasource({ id: datasourceId });

                if (response.status && response.data) 
                    setDatasource(response.data);
                else 
                    setError('Failed to load datasource details.');
                
            }
            catch {
                setError('An error occurred while loading the datasource details.');
            }
            finally {
                setLoading(false);
            }
        };

        fetchDatasource();
    }, [ datasourceId ]);

    if (error ?? (!datasource && !loading)) 
        return <ErrorPage />;

    return (
        <div>
            <div className='mt-5'>
                {loading ? (
                    <div>
                        <Spinner />
                    </div>
                ) : (
                    <div>
                        <h1 className='heading-main my-5'>{datasource?.label}</h1>
                        <p className='mb-5'>Type: {datasource?.type}</p>
                        <pre>{JSON.stringify(datasource?.settings, null, 2)}</pre>
                        <pre className='text-zinc-400'>{JSON.stringify(datasource?.configuration, null, 2)}</pre>
                    </div>
                )}
            </div>
        </div>
    );
};
