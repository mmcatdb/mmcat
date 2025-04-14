import { useState, useCallback, type ReactNode } from 'react';
import { SchemaCategoriesTable } from '@/components/category/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button, cn, Input, Tooltip } from '@nextui-org/react';
import { AddSchemaModal } from './Home';
import { Outlet, useLoaderData, useNavigate } from 'react-router-dom';
import { HiMiniMagnifyingGlass, HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { useBannerState } from '@/types/utils/useBannerState';
import { type Id } from '@/types/id';
import { FaPlus } from 'react-icons/fa';
import { routes } from '@/routes/routes';
import { Card, CardBody } from '@nextui-org/react';

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function SchemaCategoriesPage() {
    return (
        <Outlet />
    );
}

export function CategoriesPage() {
    const { categories: loadedCategories } = useLoaderData() as CategoriesLoaderData;
    const [ categories, setCategories ] = useState(loadedCategories);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    // const [ isFetching, setIsFetching ] = useState(false);
    const [ isCreatingSchema, setIsCreatingSchema ] = useState(false);
    const [ isCreatingExampleSchema, setIsCreatingExampleSchema ] = useState(false);
    const [ searchTerm, setSearchTerm ] = useState('');
    const { isVisible, dismissBanner, restoreBanner } = useBannerState('categories-page');
    const navigate = useNavigate();

    const categoryDeleted = useCallback((id: Id) => {
        setCategories(prev => prev?.filter(category => category.id !== id) ?? []);
    }, []);

    const createSchema = useCallback(async (name: string, isExample = false) => {
        // setIsFetching(true);
        isExample ? setIsCreatingExampleSchema(true) : setIsCreatingSchema(true);

        const response = isExample
            ? await api.schemas.createExampleCategory({ name })
            : await api.schemas.createNewCategory({}, { label: name });

        // setIsFetching(false);
        isExample ? setIsCreatingExampleSchema(false) : setIsCreatingSchema(false);

        if (!response.status) {
            toast.error('Error creating schema category.');
            return;
        }

        const newCategory = SchemaCategoryInfo.fromServer(response.data);
        setCategories(prev => [ newCategory, ...(prev ?? []) ]);

        toast.success(`${isExample ? 'Example schema' : 'Schema'} '${newCategory.label}' created successfully!`);

        navigate(routes.category.index.resolve({ categoryId: newCategory.id }));
    }, [ navigate ]);

    const filteredCategories = categories.filter(category =>
        category.label.toLowerCase().includes(searchTerm.toLowerCase()),
    );

    return (
        <div className='pt-4'>
            {/* Header Section with Info button */}
            <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                    <h1 className='text-xl font-bold'>
                        Schema Categories
                    </h1>
                    <Tooltip content={isVisible ? 'Hide info' : 'Show info'}>
                        <button
                            onClick={isVisible ? dismissBanner : restoreBanner}
                            className='text-primary-500 hover:text-primary-700 transition'
                        >
                            <IoInformationCircleOutline className='w-6 h-6' />
                        </button>
                    </Tooltip>
                </div>
                
                <div className='flex gap-2'>
                    <Button
                        onPress={() => setIsModalOpen(true)}
                        isLoading={isCreatingSchema}
                        color='primary'
                        // size='sm'
                        startContent={<FaPlus className='w-3 h-3' />}
                    >
                        New Schema
                    </Button>
                    {EXAMPLE_SCHEMAS.map(example => (
                        <Button
                            key={example}
                            onPress={() => {
                                void createSchema(example, true); 
                            }}
                            isLoading={isCreatingExampleSchema}
                            color='secondary'
                            variant='flat'
                            // size='sm'
                            startContent={<FaPlus className='w-3 h-3' />}
                        >
                            Example
                        </Button>
                    ))}
                </div>
            </div>

            {isVisible && <SchemaCategoryInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

            {/* Action Bar (Search + Buttons) */}
            <div className='flex flex-col md:flex-row md:items-center justify-between gap-4 bg-default-50 p-4 rounded-lg shadow-sm mb-4'>
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
            </div>

            <div className='space-y-6'>
                {categories.length > 0 ? (
                    filteredCategories.length > 0 ? (
                        <SchemaCategoriesTable
                            categories={filteredCategories}
                            onDeleteCategory={categoryDeleted}
                        />
                    ) : (
                        <p className='text-default-500 text-center'>No matching categories.</p>
                    )
                ) : (
                    <div className='text-center border-2 border-dashed border-default-200 p-12 rounded-xl'>
                        <FaPlus className='w-8 h-8 mx-auto text-default-300' />
                        <p className='mt-4 text-default-500'>No schema categories yet. Create your first one to get started!</p>
                    </div>
                )}
            </div>

            <AddSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={label => {
                    void createSchema(label, false);
                }}
                isSubmitting={isCreatingSchema}
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

type InfoBannerProps = {
    children: ReactNode;
    className?: string;
    dismissBanner: () => void;
};

export function InfoBanner({ children, className, dismissBanner }: InfoBannerProps) {
    return (
        <Card 
            shadow='sm' 
            radius='lg' 
            className={cn('relative bg-content1', className)}
        >
            <CardBody className='text-sm text-foreground px-4 py-3 relative'>
                <button
                    onClick={dismissBanner}
                    className='absolute top-2 right-2 text-default-500 hover:text-foreground transition'
                >
                    <HiXMark className='w-5 h-5' />
                </button>
                {children}
            </CardBody>
        </Card>
    );
}


type SchemaCategoryInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

export function SchemaCategoryInfoBanner({ className, dismissBanner }: SchemaCategoryInfoBannerProps) {
    return (
        <InfoBanner className={className} dismissBanner={dismissBanner}>
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
        </InfoBanner>
    );
}
