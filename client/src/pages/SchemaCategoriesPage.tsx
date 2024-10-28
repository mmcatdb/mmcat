import { useEffect, useState } from 'react';
import { SchemaCategoriesTable } from '@/components/schema-categories/SchemaCategoriesTable';
// import { SchemaCategoryModal } from '@/components/schemaCategories/SchemaCategoryModal';
import { api } from '@/api';
import { type SchemaCategoryInfoFromServer } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button } from '@nextui-org/react';

export function SchemaCategoriesPage() {
    const [ schemaCategories, setSchemaCategories ] = useState<SchemaCategoryInfoFromServer[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        const fetchSchemaCategories = async () => {
            try {
                setLoading(true);
                const response = await api.schemas.getAllCategoryInfos({});
                if (response.status && response.data) 
                    setSchemaCategories(response.data);
                else 
                    setError('Failed to load schema categories');
                
            }
            catch (err) {
                setError('Failed to load schema categories');
            }
            finally {
                setLoading(false);
            }
        };

        fetchSchemaCategories();
    }, []);

    const handleDeleteCategory = () => {
        toast.error('Not handled yet. TODO this.');
    };

    function handleAddCategory(): void {
        toast.error('Not implemented yet. Can\'t add datasource.');
    }

    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1 className='heading-large'>Schema Categories</h1>
                <Button onClick={handleAddCategory} color='primary'>+ Add Schema Category</Button>
            </div>
            <div className='mt-5'>
                <SchemaCategoriesTable
                    categories={schemaCategories}
                    loading={loading}
                    error={error}
                    onDeleteCategory={handleDeleteCategory}
                />
            </div>
        </div>
    );
}
