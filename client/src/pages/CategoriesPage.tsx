import { useState, useCallback, type KeyboardEvent } from 'react';
import { SchemaCategoriesTable } from '@/components/category/SchemaCategoriesTable';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { toast } from 'react-toastify';
import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader } from '@heroui/react';
import { useLoaderData, useNavigate } from 'react-router-dom';
import { HiMiniMagnifyingGlass, HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';
import { useBannerState } from '@/types/utils/useBannerState';
import { type Id } from '@/types/id';
import { FaPlus } from 'react-icons/fa';
import { routes } from '@/routes/routes';
import { InfoBanner, InfoTooltip, SpinnerButton } from '@/components/common/components';
import { PageLayout } from '@/components/RootLayout';

export const EMPTY_CATEGORY = 'empty';
/** List of example schema names available for creation. */
export const EXAMPLE_CATEGORIES = [ 'basic', 'adminer' ] as const;

type NewCategoryType = typeof EMPTY_CATEGORY | typeof EXAMPLE_CATEGORIES[number];

/**
 * Renders the main page for managing schema categories, including creation, search, and display.
 */
export function CategoriesPage() {
    const { categories: loadedCategories } = useLoaderData() as CategoriesLoaderData;
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ searchTerm, setSearchTerm ] = useState('');
    const banner = useBannerState('categories-page');

    const { categories, fetching, createCategory, onDeleteCategory } = useSchemaCategories(loadedCategories);

    const filteredCategories = categories.filter(category =>
        category.label.toLowerCase().includes(searchTerm.toLowerCase()),
    );

    return (
        <PageLayout>
            {/* Header Section with Info button */}
            <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                    <h1 className='text-xl font-bold'>Schema Categories</h1>

                    <InfoTooltip {...banner} />
                </div>

                <div className='flex gap-2'>
                    <SpinnerButton
                        onPress={() => setIsModalOpen(true)}
                        fid={FID_EMPTY}
                        fetching={fetching}
                        color='primary'
                        // size='sm'
                        startContent={<FaPlus className='size-4' />}
                    >
                        New Schema
                    </SpinnerButton>

                    {EXAMPLE_CATEGORIES.map(example => (
                        <SpinnerButton
                            key={example}
                            onPress={() => createCategory(example, example, fidExample(example))}
                            color='secondary'
                            variant='flat'
                            startContent={<FaPlus className='size-4' />}
                            fetching={fetching}
                            fid={fidExample(example)}
                        >
                            Example ({example})
                        </SpinnerButton>
                    ))}
                </div>
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <SchemaCategoryInfoInner />
            </InfoBanner>

            {/* Action Bar (Search + Buttons) */}
            <div className='flex flex-col md:flex-row md:items-center justify-between gap-4 bg-default-50 p-4 rounded-lg shadow-xs mb-4'>
                {/* Search Input */}
                <Input
                    type='text'
                    placeholder='Search categories...'
                    className='w-full md:w-72'
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                    startContent={<HiMiniMagnifyingGlass className='size-5 text-default-400' />}
                    endContent={
                        searchTerm && (
                            <button onClick={() => setSearchTerm('')} className='text-default-500 hover:text-default-700 transition'>
                                <HiXMark className='size-5' />
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
                            onDeleteCategory={onDeleteCategory}
                        />
                    ) : (
                        <p className='text-default-500 text-center'>No matching categories.</p>
                    )
                ) : (
                    <div className='text-center border-2 border-dashed border-default-200 p-12 rounded-xl'>
                        <FaPlus className='size-8 mx-auto text-default-300' />
                        <p className='mt-4 text-default-500'>No schema categories yet. Create your first one to get started!</p>
                    </div>
                )}
            </div>

            <CreateSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                // TODO The fetching animation should be on the button in the modal ...
                onSubmit={label => createCategory(label, EMPTY_CATEGORY, FID_EMPTY)}
            />
        </PageLayout>
    );
}

const FID_EMPTY = 'empty';
function fidExample(example: string) {
    return `example-${example}`;
}

export type CategoriesLoaderData = {
    categories: SchemaCategoryInfo[];
};

CategoriesPage.loader = async (): Promise<CategoriesLoaderData> => {
    const response = await api.schemas.getAllCategoryInfos({});
    if (!response.status)
        throw new Error('Failed to load schema categories');

    return {
        categories: response.data.map(SchemaCategoryInfo.fromResponse),
    };
};

export function useSchemaCategories(loadedCategories: SchemaCategoryInfo[]) {
    const [ categories, setCategories ] = useState(loadedCategories);
    const [ fetching, setFetching ] = useState<string>();
    const navigate = useNavigate();

    const createCategory = useCallback(async (label: string, type: NewCategoryType, fid: string) => {
        const isExample = type !== EMPTY_CATEGORY;

        setFetching(fid);
        const response = isExample
            ? await api.schemas.createExampleCategory({ type })
            : await api.schemas.createNewCategory({}, { label });
        setFetching(undefined);

        if (!response.status) {
            toast.error('Error creating schema category.');
            return;
        }

        const newCategory = SchemaCategoryInfo.fromResponse(response.data);
        setCategories(prev => [ newCategory, ...(prev ?? []) ]);

        toast.success(`${isExample ? 'Example schema' : 'Schema'} '${newCategory.label}' created successfully!`);

        navigate(routes.category.index.resolve({ categoryId: newCategory.id }));
    }, [ navigate ]);

    const onDeleteCategory = useCallback((id: Id) => {
        setCategories(prev => prev?.filter(category => category.id !== id) ?? []);
    }, []);

    return {
        categories,
        fetching,
        createCategory,
        onDeleteCategory,
    };
}

type CreateSchemaModalProps = {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (label: string) => void;
};

export function CreateSchemaModal({ isOpen, onClose, onSubmit }: CreateSchemaModalProps) {
    const [ label, setLabel ] = useState('');

    function submit() {
        if (!label.trim()) {
            toast.error('Please provide a valid label for the schema.');
            return;
        }
        onSubmit(label);
        close();
    }

    function close() {
        setLabel('');
        onClose();
    }

    function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
        if (e.key === 'Enter')
            submit();
    }

    return (
        <Modal isOpen={isOpen} onClose={close} isDismissable={false}>
            <ModalContent>
                <ModalHeader className='flex flex-col gap-1 text-xl font-semibold'>
                    Create New Schema Category
                </ModalHeader>
                <ModalBody>
                    <p className='text-default-500'>
                        Schema categories help you organize your data models and transformations.
                    </p>
                    <Input
                        autoFocus
                        label='Schema Name'
                        value={label}
                        onChange={e => setLabel(e.target.value)}
                        fullWidth
                        onKeyDown={handleKeyDown}
                        classNames={{ input: 'text-lg' }}
                    />
                </ModalBody>
                <ModalFooter>
                    <Button variant='light' onPress={close}>
                        Cancel
                    </Button>
                    <Button color='primary' onPress={submit} isDisabled={!label.trim()}>
                        Create Schema
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}

export function SchemaCategoryInfoInner() {
    return (<>
        <h2 className='text-lg font-semibold mb-2'>Understanding Schema Categories</h2>
        <p className='text-sm'>
            A <span className='font-bold'>Schema Category</span> represents the structure of your data at a high level.
            It is a <em>project</em>, grouping everything related to a specific conceptual schema.
            Within a Schema Category, you can manage the <em>Schema Category Graph</em> (add objects and morphisms), as well as <em>Mappings, Data Sources, Actions, Runs, and Jobs</em>.
        </p>
        <ul className='mt-3 text-sm space-y-2'>
            <li className='flex items-center gap-2'>
                <GoDotFill className='text-primary-500' />
                <span className='font-bold'>Conceptual Schema:</span> Defines the data model without focusing on storage details.
            </li>
            <li className='flex items-center gap-2'>
                <GoDotFill className='text-primary-500' />
                <span className='font-bold'>Instance Category:</span> Holds concrete data based on the schema.
            </li>
            <li className='flex items-center gap-2'>
                <GoDotFill className='text-primary-500' />
                <span className='font-bold'>Logical Model:</span> Defines how data is stored in tables, documents, or other structures.
            </li>
        </ul>
        <p className='text-sm mt-3'>
            Each Schema Category serves as a <em>workspace</em> where you define how data is structured and processed.
            Start by creating a <em>Graph</em> in editor, then create <em>Mappings</em> and execute <em>Jobs</em> to transform data.
        </p>
    </>);
}
