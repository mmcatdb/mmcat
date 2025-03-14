import { useState, useCallback } from 'react';
import { SchemaCategoriesTable } from '@/components/category/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button, cn, Input } from '@nextui-org/react';
import { AddSchemaModal } from './Home';
import { useLoaderData } from 'react-router-dom';
import { HiMiniMagnifyingGlass, HiXMark } from 'react-icons/hi2';
import { usePreferences } from '@/components/PreferencesProvider';
import { GoDotFill } from 'react-icons/go';

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

            <SchemaCategoryInfoBanner className='mb-6' />

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
                            + Add Example Schema
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

type SchemaCategoryInfoBannerProps = {
    className?: string;
};

export function SchemaCategoryInfoBanner({ className }: SchemaCategoryInfoBannerProps) {
    const { preferences, setPreferences } = usePreferences();
    const [ isVisible, setIsVisible ] = useState(!preferences.dismissedSchemaCategoryGuide);

    function handleClose() {
        setIsVisible(false);
        setPreferences({ ...preferences, dismissedSchemaCategoryGuide: true });
    }

    if (!isVisible) 
        return null;

    return (
        <div className={cn('relative bg-default-50 text-default-900 p-4 rounded-lg border border-default-300', className)}>
            <button 
                onClick={handleClose} 
                className='absolute top-2 right-2 text-default-500 hover:text-default-700 transition'
            >
                <HiXMark className='w-5 h-5' />
            </button>

            <h2 className='text-lg font-semibold mb-2'>Understanding Schema Categories</h2>
            <p className='text-sm'>
                A <strong>Schema Category</strong> represents the structure of your data at a high level.  
                It is a <em>project</em>, grouping everything related to a specific conceptual schema.  
                Within a Schema Category, you can manage the <em>Schema Category Graph</em> (add objects and morphisms), as well as <em>Mappings, Data Sources, Actions, Runs, and Jobs</em>.
            </p>

            <ul className='mt-3 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Conceptual Schema:</strong> Defines the data model without focusing on storage details.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Instance Category:</strong> Holds concrete data based on the schema.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Logical Model:</strong> Defines how data is stored in tables, documents, or other structures.
                </li>
            </ul>

            <p className='text-sm mt-3'>
                Each Schema Category serves as a <em>workspace</em> where you define how data is structured and processed.
                Start by creating a <em>Graph</em> in editor, then create <em>Mappings</em> and execute <em>Jobs</em> to transform data.
            </p>
        </div>
    );
}
