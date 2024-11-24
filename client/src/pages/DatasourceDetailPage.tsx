import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { ErrorPage } from '@/pages/errorPages';
import { Spinner } from '@nextui-org/react';
import { Mapping } from '@/types/mapping';
import { MappingsTable } from '@/components/schema-categories/MappingsTable';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';

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
    const [ mappings, setMappings ] = useState<Mapping[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        if (!categoryId || !id) 
            return;

        const fetchMappings = async () => {
            try {
                setLoading(true);
                const response = await api.mappings.getAllMappingsInCategory({}, {
                    categoryId,
                    datasourceId: id,
                });
                if (response.status && response.data)
                    setMappings(response.data.map(Mapping.fromServer));
                else
                    setError('Failed to load data');
            }
            catch (e) {
                setError('Failed to load mappings.');
            }
            finally {
                setLoading(false);
            }
        };

        fetchMappings();
    }, [ categoryId, id ]);

    if (!categoryId || !id) 
        return <ErrorPage />;

    const handleAddMapping = () => {
        toast.error('Add mapping functionality not implemented yet');
    };

    return (
        <div>
            <DatasourceDetail datasourceId={id} />
            <div className='mt-6'>
                <p className='text-xl pb-6'>Mappings Table</p>
                {loading ? (
                    <Spinner />
                ) : mappings.length > 0 ? (
                    <MappingsTable
                        mappings={mappings}
                        loading={loading}
                        error={error}
                    />
                ) : (
                    <EmptyState 
                        message='This datasource does not have a mapping yet.'
                        buttonText='+ Add Mapping'
                        onButtonClick={handleAddMapping}
                    />
                )}
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
