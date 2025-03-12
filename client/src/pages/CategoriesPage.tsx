import { useState, useCallback } from 'react';
import { SchemaCategoriesTable } from '@/components/category/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button, Input, Table, TableBody, TableColumn, TableHeader } from '@nextui-org/react';
import { AddSchemaModal } from './Home';
import { useLoaderData } from 'react-router-dom';
import { HiMiniMagnifyingGlass, HiXMark } from 'react-icons/hi2';

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function CategoriesPage() {
    const { categories: loadedCategories } = useLoaderData() as CategoriesLoaderData;
    const [ categories, setCategories ] = useState(loadedCategories);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ isFetching, setIsFetching ] = useState(false);
    const [ searchTerm, setSearchTerm ] = useState('');

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
        setCategories(prev => (prev ? [ ...prev, newCategory ] : [ newCategory ]));

        toast.success(`${isExample ? 'Example schema' : 'Schema'} '${newCategory.label}' created successfully!`);
    }, []);

    const filteredCategories = categories.filter(category =>
        category.label.toLowerCase().includes(searchTerm.toLowerCase()),
    );

    return (
        <div className='p-8 space-y-6'>
            {/* Header Section */}
            <div className='flex justify-between items-center'>
                <h1 className='text-2xl font-bold'>Schema Categories</h1>
            </div>

            <p className='text-default-600'>
                Schema categories help you organize and structure your data models. 
                Create a new schema to start modeling your data or explore an example schema.
            </p>

            {/* Action Bar (Search + Buttons) */}
            <div className='flex flex-col md:flex-row md:items-center justify-between gap-4 bg-default-50 p-4 rounded-lg shadow-sm'>
                {/* Search Input */}
                <Input
                    type='text'
                    placeholder='Search categories...'
                    className='w-full md:w-72'
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                    startContent={<HiMiniMagnifyingGlass className='w-5 h-5 text-default-400' />}
                    endContent={
                        searchTerm && (
                            <button
                                onClick={() => setSearchTerm('')}
                                className='text-default-500 hover:text-default-700 transition'
                            >
                                <HiXMark className='w-5 h-5' />
                            </button>
                        )
                    }
                    disabled={categories.length === 0}
                />

                {/* Buttons */}
                <div className='flex gap-3'>
                    {EXAMPLE_SCHEMAS.map(example => (
                        <Button
                            key={example}
                            onPress={() => createSchema(example, true)}
                            isLoading={isFetching}
                            color='secondary'
                            title='Add an example (pre-made) schema category'
                        >
                            + Add {example} Schema
                        </Button>
                    ))}
                    <Button
                        onPress={() => setIsModalOpen(true)}
                        isLoading={isFetching}
                        color='primary'
                        title='Add an empty schema category'
                    >
                        + Add Empty Schema
                    </Button>
                </div>
            </div>

            <div className='space-y-6'>
                {categories.length > 0 ? (
                    filteredCategories.length > 0 ? (
                        <SchemaCategoriesTable
                            categories={filteredCategories}
                            onDeleteCategory={handleDeleteCategory}
                        />
                    ) : (
                        <p className='text-default-500 text-center'>No matching categories.</p>
                    )
                ) : (
                    <div className='text-center border p-6 rounded-lg border-default-200'>
                        <span className='text-4xl'>🐱</span>
                        <p className='mt-2 text-default-500'>No categories available. Create one to get started!</p>
                    </div>
                )}
            </div>

            <AddSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={label => createSchema(label, false)}
                isSubmitting={isFetching}
            />
        </div>
    );
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
