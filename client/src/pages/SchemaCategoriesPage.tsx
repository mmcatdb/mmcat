import { useEffect, useState, useCallback } from 'react';
import { SchemaCategoriesTable } from '@/components/schema-categories/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button } from '@nextui-org/react';
import { LoadingPage, ReloadPage } from './errorPages';

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function SchemaCategoriesPage() {
    const [ schemaCategories, setSchemaCategories ] = useState<SchemaCategoryInfo[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<boolean>(false);
    const [ isCreatingSchema, setIsCreatingSchema ] = useState<boolean>(false);

    const fetchSchemaCategories = async () => {
        setLoading(true);
        setError(false);
        const response = await api.schemas.getAllCategoryInfos({});
        setLoading(false);

        if (!response.status) {
            setError(true);
            return;
        }

        setSchemaCategories(response.data.map(SchemaCategoryInfo.fromServer));
    };

    useEffect(() => {
        void fetchSchemaCategories();
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

    if (loading)
        return <LoadingPage />;

    if (error)
        return <ReloadPage onReload={fetchSchemaCategories} title='Schema categories' message='Failed to load Schema categories.' />;

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
