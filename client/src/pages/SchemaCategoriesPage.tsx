import { useEffect, useState, useCallback } from 'react';
import { SchemaCategoriesTable } from '@/components/schema-categories/SchemaCategoriesTable';
import { api } from '@/api';
import { type SchemaCategoryInfoFromServer } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button } from '@nextui-org/react';

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function SchemaCategoriesPage() {
    const [ schemaCategories, setSchemaCategories ] = useState<SchemaCategoryInfoFromServer[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);
    const [ isCreatingSchema, setIsCreatingSchema ] = useState<boolean>(false);

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

    const createExampleSchema = useCallback(async (name: string) => {
        setIsCreatingSchema(true);
        try {
            const response = await api.schemas.createExampleCategory({ name });
            if (response.status && response.data) {
                const newCategory = response.data;
                setSchemaCategories((categories) => [ ...categories, newCategory ]);
                toast.success(`Example schema '${name}' created successfully!`);
            }
            else {
                toast.error('Failed to create example schema.');
            }
        }
        catch (err) {
            toast.error('Error occurred while creating example schema.');
        }
        finally {
            setIsCreatingSchema(false);
        }
    }, []);

    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1>Schema Categories</h1>
                <div className='flex'>
                    {EXAMPLE_SCHEMAS.map((example) => (
                        <Button
                            key={example}
                            onPress={() => createExampleSchema(example)}
                            isLoading={isCreatingSchema}
                            color='primary'
                        >
                            + Add {example} Schema
                        </Button>
                    ))}
                </div>
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
