import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { ErrorPage } from '@/pages/errorPages';
import { LoadingComponent } from '@/pages/errorPages';
import { CommonPage } from '@/components/CommonPage';
import { Breadcrumbs, BreadcrumbItem } from '@nextui-org/react';

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

    if (loading) 
        return <LoadingComponent className='w-screen h-screen text-foreground bg-background' />;
    if (error || !datasource) 
        return <ErrorPage />;

    return (
        <CommonPage>
            <Breadcrumbs>
                <BreadcrumbItem>
                    <Link
                        to='/datasources'
                        className='text-blue-600 hover:text-blue-700 font-bold'
                    >
                        Datasources
                    </Link></BreadcrumbItem>
                <BreadcrumbItem>Detail</BreadcrumbItem>
            </Breadcrumbs>
            <div>
                <h1 className='text-2xl font-bold my-5'>{datasource.label}</h1>
                <p className='mb-5'>Type: {datasource.type}</p>
                <pre>{JSON.stringify(datasource.settings, null, 2)}</pre>
                <pre>{JSON.stringify(datasource.configuration, null, 2)}</pre>
            </div>
        </CommonPage>
    );
};

export default DatasourceDetailPage;
