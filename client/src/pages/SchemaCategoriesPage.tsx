import { useEffect, useState, useCallback } from 'react';
import { SchemaCategoriesTable } from '@/components/schema-categories/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button } from '@nextui-org/react';
import { LoadingPage, ReloadPage } from './errorPages';
import { AddSchemaModal } from './Home';

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function SchemaCategoriesPage() {
    const [ schemaCategories, setSchemaCategories ] = useState<SchemaCategoryInfo[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<boolean>(false);
    const [ isCreatingSchema, setIsCreatingSchema ] = useState<boolean>(false);
    const [ isCreatingExampleSchema, setIsCreatingExampleSchema ] = useState(false);
    const [ isModalOpen, setIsModalOpen ] = useState(false);

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

    const handleCreateSchema = useCallback(
        async (name: string, isExample = false) => {
            isExample ? setIsCreatingExampleSchema(true) : setIsCreatingSchema(true);

            const response = isExample
                ? await api.schemas.createExampleCategory({ name })
                : await api.schemas.createNewCategory({}, { label: name });

            isExample ? setIsCreatingExampleSchema(false) : setIsCreatingSchema(false);

            if (!response.status) {
                toast.error('Error creating schema category.');
                return;
            }

            const newCategory = SchemaCategoryInfo.fromServer(response.data);
            setSchemaCategories((categories) =>
                categories ? [ ...categories, newCategory ] : [ newCategory ],
            );

            toast.success(
                `${isExample ? 'Example schema' : 'Schema'} '${newCategory.label}' created successfully!`,
            );
        },
        [],
    );

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
                            onPress={() => handleCreateSchema(example, true)}
                            isLoading={isCreatingExampleSchema}
                            color='primary'
                            variant='bordered'
                            isDisabled={loading}
                        >
                            + Add {example} Schema
                        </Button>
                    ))}
                    <Button
                        key={'newSchema'} 
                        onPress={() => setIsModalOpen(true)}
                        isLoading={isCreatingSchema}
                        color='primary'
                        className='ml-2'
                    >
                        + Add schema
                    </Button>
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

            <AddSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={(label) => handleCreateSchema(label, false)}
                isSubmitting={isCreatingSchema}
            />
        </div>
    );
}
