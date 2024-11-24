import { useEffect, useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import type { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { Button } from '@nextui-org/react';
import { AddIcon } from '@/components/icons/PlusIcon';
import { EmptyState } from '@/components/TableCommon';
import { LoadingPage } from '../errorPages';

export function DatasourcesInCategoryPage() {
    const { category } = useCategoryInfo();
    const [ datasourcesInCategory, setDatasourcesInCategory ] = useState<Datasource[]>([]);
    const [ otherDatasources, setOtherDatasources ] = useState<Datasource[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);
    const [ isModalOpen, setModalOpen ] = useState(false);

    useEffect(() => {
        const fetchDatasources = async () => {
            try {
                setLoading(true);

                // Fetch datasources in category
                const inCategoryResponse = await api.datasources.getAllDatasources({}, { categoryId: category.id });
                if (inCategoryResponse.status && inCategoryResponse.data)  
                    setDatasourcesInCategory(inCategoryResponse.data);
                else 
                    throw new Error('Failed to fetch datasources in category');
                

                // Fetch all datasources and filter out ds in category
                const allDatasourcesResponse = await api.datasources.getAllDatasources({});
                if (allDatasourcesResponse.status && allDatasourcesResponse.data) {
                    const notInCategory = allDatasourcesResponse.data.filter(
                        ds => !inCategoryResponse.data.some(inCat => inCat.id === ds.id),
                    );
                    setOtherDatasources(notInCategory);
                }
                else {
                    throw new Error('Failed to fetch all datasources');
                }
            } 
            catch (err) {
                setError('Failed to load data');
            }
            finally {
                setLoading(false);
            }
        };

        fetchDatasources();
    }, [ category.id ]);

    // callback to add new datasource
    const handleAddDatasource = (newDatasource: Datasource) => {
        setOtherDatasources(prev => [ ...prev, newDatasource ]);
    };

    // callback to delete a datasource
    const handleDeleteDatasource = async (id: string) => {
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
    };

    if (loading) 
        return <LoadingPage />;

    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1 className='text-xl'>Datasources in {category.label} (with mapping)</h1>
            </div>

            <div className='mt-5'>
                <DatasourcesTable
                    datasources={datasourcesInCategory}
                    loading={loading}
                    error={error}
                    onDeleteDatasource={handleDeleteDatasource}
                />
            </div>

            <div className='flex items-center justify-between mt-10'>
                <h1 className='text-xl'>Other Datasources</h1>
                <Button 
                    onPress={() => setModalOpen(true)} 
                    color='primary' 
                    startContent={<AddIcon />}
                >
                    Add Datasource
                </Button>
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
