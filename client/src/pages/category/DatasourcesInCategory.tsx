import { useEffect, useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { EmptyState } from '@/components/TableCommon';
import { LoadingPage, ReloadPage } from '../errorPages';

export function DatasourcesInCategoryPage() {
    const { category } = useCategoryInfo();
    const [ datasourcesInCategory, setDatasourcesInCategory ] = useState<Datasource[]>([]);
    const [ otherDatasources, setOtherDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<boolean>(false);
    const [ isModalOpen, setModalOpen ] = useState(false);

    async function fetchDatasources() {
        // TODO: no fetch, data via loader
        setLoading(true);
        setError(false);
        // Fetch datasources in category, and then all datasources
        const inCategoryResponse = await api.datasources.getAllDatasources({}, { categoryId: category.id });
        const allDatasourcesResponse = await api.datasources.getAllDatasources({});
        setLoading(false);

        if (!inCategoryResponse.status) {
            setError(true);
            return;
        }
        setDatasourcesInCategory(inCategoryResponse.data);

        // filter out datasources in category   
        if (!allDatasourcesResponse.status) {
            setError(true);
            return;
        }
            
        const notInCategory = allDatasourcesResponse.data.filter(
            ds => !inCategoryResponse.data.some(inCat => inCat.id === ds.id),
        );
        setOtherDatasources(notInCategory);
    }

    useEffect(() => {
        void fetchDatasources();
    }, [ category.id ]);

    // callback to add new datasource
    function handleAddDatasource(newDatasource: Datasource) {
        setOtherDatasources(prev => [ ...prev, newDatasource ]);
    }

    // callback to delete a datasource
    async function handleDeleteDatasource(id: string) {
        try {
            const response = await api.datasources.deleteDatasource({ id });

            if (response.status) {
                setDatasourcesInCategory(prev => prev.filter(ds => ds.id !== id));
                setOtherDatasources(prev => prev.filter(ds => ds.id !== id));
            }
            else {
                toast.error('Failed to delete datasource. Please try again.');
            }
        }
        catch (error) {
            console.error('Error deleting datasource:', error);
            toast.error('An error occurred while deleting the datasource.');
        }
    }

    if (loading) 
        return <LoadingPage />;

    if (error)
        return <ReloadPage onReload={fetchDatasources} title='Datasources' message='Failed to load Datasources.' />;

    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1 className='text-xl'>Datasources in {category.label} (with mapping)</h1>
            </div>

            <div className='mt-5'>
                {datasourcesInCategory.length > 0 ? (
                    <DatasourcesTable
                        datasources={datasourcesInCategory}
                        loading={loading}
                        error={error}
                        onDeleteDatasource={handleDeleteDatasource}
                    />
                ) : (
                    <div className = 'text-center border border-zinc-500 p-6'>
                        There is no datasources with mappings available. You can add mapping via Other Datasources.
                    </div>
                )}
            </div>

            <div className='flex items-center justify-between mt-10'>
                <h1 className='text-xl'>Other Datasources</h1>
            </div>

            <div className='mt-5'>
                {otherDatasources.length > 0 ? (
                    <DatasourcesTable
                        datasources={otherDatasources}
                        loading={loading}
                        error={error}
                        onDeleteDatasource={handleDeleteDatasource}
                    />
                ) : (
                    <EmptyState
                        message='No other datasources available.'
                        buttonText='+ Add Datasource'
                        onButtonClick={() => setModalOpen(true)}
                    />
                )}
            </div>

            <DatasourceModal 
                isOpen={isModalOpen} 
                onClose={() => setModalOpen(false)} 
                onDatasourceCreated={handleAddDatasource} 
            />
        </div>
    );
}
