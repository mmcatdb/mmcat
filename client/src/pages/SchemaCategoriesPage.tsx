import { useEffect, useState, useCallback } from 'react';
import { SchemaCategoriesTable } from '@/components/schema-categories/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button } from '@nextui-org/react';
import { LoadingPage } from './errorPages';

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function SchemaCategoriesPage() {
    const [ schemaCategories, setSchemaCategories ] = useState<SchemaCategoryInfo[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string>();
    const [ isCreatingSchema, setIsCreatingSchema ] = useState<boolean>(false);

    useEffect(() => {
        const fetchSchemaCategories = async () => {
            setLoading(true);
            const response = await api.schemas.getAllCategoryInfos({});
            if (response.status && response.data)
                setSchemaCategories(response.data.map(SchemaCategoryInfo.fromServer));
            else 
                setError('Failed to load schema categories');

            setLoading(false);
        };

        fetchSchemaCategories();
    }, []);

    function handleDeleteCategory() {
        toast.error('Not handled yet. TODO this.');
    }

    const createExampleSchema = useCallback(async (name: string) => {
        setIsCreatingSchema(true);
        const response = await api.schemas.createExampleCategory({ name });
        setIsCreatingSchema(false);

        if (response.status && response.data) {
            const newCategory = SchemaCategoryInfo.fromServer(response.data);
            setSchemaCategories((categories) => [ ...categories, newCategory ]);
            toast.success(`Example schema '${newCategory.label}' created successfully!`);
        }
        else {
            toast.error('Failed to create example schema.');
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
                            isDisabled={loading}
                        >
                            + Add {example} Schema
                        </Button>
                    ))}
                </div>
            </div>

            {loading ? (
                <LoadingPage />
            ) : 
                <div className='mt-5'>
                    <SchemaCategoriesTable
                        categories={schemaCategories}
                        loading={loading}
                        error={error}
                        onDeleteCategory={handleDeleteCategory}
                    />
                </div>
            }
        </div>
    );
}
