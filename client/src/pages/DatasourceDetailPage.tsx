import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { ErrorPage } from '@/pages/errorPages';
import { CommonPage } from '@/components/CommonPage';
import { Breadcrumbs, BreadcrumbItem, Spinner } from '@nextui-org/react';

export const DatasourceDetailPage = () => {
    const { id } = useParams<{ id: string }>();
    const [ datasource, setDatasource ] = useState<Datasource | null>(null);
    const [ loading, setLoading ] = useState(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        const fetchDatasource = async () => {
            if (!id) {
                setError('Invalid datasource ID.');
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                const response = await api.datasources.getDatasource({ id });

                if (response.status && response.data) 
                    setDatasource(response.data);
                else 
                    setError('Failed to load datasource details.');
                
            }
            catch (err) {
                setError('An error occurred while loading the datasource details.');
            }
            finally {
                setLoading(false);
            }
        };

        fetchDatasource();
    }, [ id ]);

    if (error ?? (!datasource && !loading)) 
        return <ErrorPage />;

    return (
        <CommonPage>
            <Breadcrumbs>
                <BreadcrumbItem>
                    <Link
                        to='/datasources'
                        className='breadcrumb-item-link'
                    >
                        Datasources
                    </Link>
                </BreadcrumbItem>
                <BreadcrumbItem>
                    Detail
                </BreadcrumbItem>
            </Breadcrumbs>
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
        </CommonPage>
    );
};

export default DatasourceDetailPage;
