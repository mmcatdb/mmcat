import { useState, useCallback } from 'react';
import { SchemaCategoriesTable } from '@/components/category/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button } from '@nextui-org/react';
import { AddSchemaModal } from './Home';
import { useLoaderData } from 'react-router-dom';

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function CategoriesPage() {
    const { categories: loadedCategories } = useLoaderData() as CategoriesLoaderData;
    const [ categories, setCategories ] = useState(loadedCategories);

    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ isFetching, setIsFetching ] = useState(false);

    function handleDeleteCategory() {
        toast.error('Not handled yet. TODO this.');
    }

    const createSchema = useCallback(async (name: string, isExample = false) => {
        setIsFetching(true);

        const response = isExample
            ? await api.schemas.createExampleCategory({ name })
            : await api.schemas.createNewCategory({}, { label: name });

        setIsFetching(false);

        if (!response.status) {
            toast.error('Error creating schema category.');
            return;
        }

        const newCategory = SchemaCategoryInfo.fromServer(response.data);
        setCategories(prev => prev ? [ ...prev, newCategory ] : [ newCategory ]);

        toast.success(`${isExample ? 'Example schema' : 'Schema'} '${newCategory.label}' created successfully!`);
    }, []);

    return (<>
        <div className='flex items-center justify-between'>
            <h1 className='text-xl font-semibold'>Schema Categories</h1>
            <div className='flex'>
                {EXAMPLE_SCHEMAS.map(example => (
                    <Button
                        key={example}
                        onPress={() => createSchema(example, true)}
                        isLoading={isFetching}
                        color='primary'
                        variant='bordered'
                        title='Add an example (pre-made) schema category'
                    >
                        + Add {example} Schema
                    </Button>
                ))}
                <Button
                    key={'newSchema'}
                    onPress={() => setIsModalOpen(true)}
                    isLoading={isFetching}
                    color='primary'
                    className='ml-2'
                    title='Add an empty schema category'
                >
                    + Add schema
                </Button>
            </div>
        </div>
        <div className='mt-5'>
            <SchemaCategoriesTable
                categories={categories}
                onDeleteCategory={handleDeleteCategory}
            />
        </div>

        <AddSchemaModal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            onSubmit={label => createSchema(label, false)}
            isSubmitting={isFetching}
        />
    </>);
}

CategoriesPage.loader = categoriesLoader;

export type CategoriesLoaderData = {
    categories: SchemaCategoryInfo[];
};

async function categoriesLoader(): Promise<CategoriesLoaderData> {
    const response = await api.schemas.getAllCategoryInfos({});
    if (!response.status)
        throw new Error('Failed to load schema categories');

    return {
        categories: response.data.map(SchemaCategoryInfo.fromServer),
    };
}
