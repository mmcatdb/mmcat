import { type KeyboardEvent, type ReactNode, useCallback, useEffect, useState } from 'react';
import { CustomLink } from '@/components/common';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Card, CardBody } from '@heroui/react';
import { toast } from 'react-toastify';
import { BookOpenIcon } from '@heroicons/react/24/solid';
import { FaDatabase, FaPlus, FaArrowRight } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { PageLayout } from '@/components/RootLayout';

const EXAMPLE_SCHEMAS = [ 'basic', 'adminer' ] as const;
const EMPTY_SCHEMA = 'empty';

type NewSchemaType = typeof EMPTY_SCHEMA | typeof EXAMPLE_SCHEMAS[number];

export function HomePage() {
    const [ categories, setCategories ] = useState<SchemaCategoryInfo[]>();
    const [ creatingSchema, setCreatingSchema ] = useState<NewSchemaType>();
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ showAllCategories, setShowAllCategories ] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        void fetchCategories();
    }, []);

    async function fetchCategories() {
        const result = await api.schemas.getAllCategoryInfos({});
        if (result.status)
            setCategories(result.data.map(SchemaCategoryInfo.fromResponse));
    }

    const createSchema = useCallback(async (name: string, type: NewSchemaType) => {
        setCreatingSchema(type);

        const response = type === EMPTY_SCHEMA
            ? await api.schemas.createNewCategory({}, { label: name })
            : await api.schemas.createExampleCategory({ name });

        setCreatingSchema(undefined);

        if (!response.status) {
            toast.error('Error creating schema category.');
            return;
        }

        const newCategory = SchemaCategoryInfo.fromResponse(response.data);
        setCategories(prev => [ newCategory, ...(prev ?? []) ]);

        toast.success(`${type === EMPTY_SCHEMA ? 'Schema' : 'Example schema'} '${name}' created successfully!`);

        navigate(routes.category.index.resolve({ categoryId: newCategory.id }));
    }, [ navigate ]);

    return (
        <PageLayout className='max-w-7xl space-y-16'>
            <HeaderSection />

            <GettingStartedSection
                onOpenModal={() => setIsModalOpen(true)}
                creatingSchema={creatingSchema}
                categories={categories}
            />

            <SchemaCategoriesSection
                categories={categories}
                showAllCategories={showAllCategories}
                setShowAllCategories={setShowAllCategories}
                onOpenModal={() => setIsModalOpen(true)}
                creatingSchema={creatingSchema}
                onCreateSchema={createSchema}
                fetchCategories={fetchCategories}
            />

            <AddSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={createSchema}
            />
        </PageLayout>
    );
}

function HeaderSection() {
    return (
        <div className='space-y-6 text-center md:text-left'>
            <h1 className='text-5xl font-bold bg-linear-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                MM-cat
            </h1>
            <p className='text-default-600 text-xl mx-auto'>
                A <span className='font-semibold text-primary-600'>multi-model data modeling framework</span> powered by category theory.
                Model, transform, and explore multi-model data without database limitations.
            </p>
        </div>
    );
}

type GettingStartedSectionProps = {
    onOpenModal: () => void;
    creatingSchema: NewSchemaType | undefined;
    categories?: SchemaCategoryInfo[];
};

function GettingStartedSection({ onOpenModal, creatingSchema, categories }: GettingStartedSectionProps) {
    const navigate = useNavigate();

    return (
        <div className='space-y-8'>
            <div className='text-center'>
                <h2 className='text-3xl font-bold bg-linear-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                    Get Started in 3 Steps
                </h2>
                <p className='text-default-500 mt-2 max-w-2xl mx-auto'>
                    Quickly set up your multi-model data modeling environment with these simple steps
                </p>
            </div>
            <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
                <FeatureCard
                    icon={
                        <div className='size-14 rounded-full bg-primary-100 flex items-center justify-center'>
                            <FaDatabase className='size-7 text-primary-600' />
                        </div>
                    }
                    title='Connect Data Sources'
                    description='Link your existing databases or files to start modeling your data.'
                    buttonText='Connect Now'
                    buttonVariant='solid'
                    buttonColor='primary'
                    buttonAction={() => navigate(routes.datasources.list.path, { state: { openModal: true } })}
                />

                <FeatureCard
                    icon={
                        <div className='size-14 rounded-full bg-secondary-100 flex items-center justify-center'>
                            <FaPlus className='size-7 text-secondary-600' />
                        </div>
                    }
                    title='Create Schema Category'
                    description='Start a new project to model your data relationships and structure.'
                    buttonText='New Schema'
                    buttonVariant='solid'
                    buttonColor='secondary'
                    buttonAction={onOpenModal}
                    isLoading={creatingSchema === EMPTY_SCHEMA}
                    isDisabled={!!creatingSchema}
                />

                <FeatureCard
                    icon={
                        <div className='size-14 rounded-full bg-success-100 flex items-center justify-center'>
                            <BookOpenIcon className='size-7 text-success-600' />
                        </div>
                    }
                    title='Define Objects in Editor'
                    description='Open last created schema category and define objects.'
                    buttonText='Explore'
                    buttonVariant='solid'
                    buttonColor='success'
                    buttonAction={() => {
                        if (categories && categories.length > 0)
                            navigate(routes.category.editor.resolve({ categoryId: categories[0].id }));
                        else
                            toast.error('No schema categories available. Please create one first.');
                    }}
                    isDisabled={!categories || categories.length === 0}
                />
            </div>
        </div>
    );
}

type SchemaCategoriesSectionProps = {
    categories: SchemaCategoryInfo[] | undefined;
    showAllCategories: boolean;
    setShowAllCategories: (state: boolean) => void;
    onOpenModal: () => void;
    creatingSchema: NewSchemaType | undefined;
    onCreateSchema: (name: string, type: NewSchemaType) => void;
    fetchCategories: () => Promise<void>;
};

function SchemaCategoriesSection({ categories, showAllCategories, setShowAllCategories, onOpenModal, creatingSchema, onCreateSchema, fetchCategories }: SchemaCategoriesSectionProps) {
    const [ isReloading, setIsReloading ] = useState(false);

    const handleReload = useCallback(async () => {
        setIsReloading(true);
        try {
            await fetchCategories();
        }
        finally {
            setIsReloading(false);
        }
    }, []);

    return (
        <div className='space-y-8'>
            <div className='flex flex-col md:flex-row md:items-end justify-between gap-4'>
                <div className='mt-5'>
                    <h2 className='text-3xl font-bold bg-linear-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                        Your Schema Categories
                    </h2>
                    <p className='text-default-500'>
                        Create or explore existing schema categories to model your data
                    </p>
                </div>
                <div className='flex flex-wrap gap-3'>
                    <Button
                        onPress={onOpenModal}
                        isLoading={creatingSchema === EMPTY_SCHEMA}
                        isDisabled={!!creatingSchema}
                        color='primary'
                        startContent={<FaPlus className='size-4' />}
                    >
                        New Schema
                    </Button>
                    {EXAMPLE_SCHEMAS.map(example => (
                        <Button
                            key={example}
                            onPress={() => onCreateSchema(example, example)}
                            isLoading={creatingSchema === example}
                            isDisabled={!!creatingSchema}
                            color='secondary'
                            variant='flat'
                            startContent={<FaPlus className='size-4' />}
                        >
                            Example ({example})
                        </Button>
                    ))}
                </div>
            </div>

            {!categories ? (
                <div className='flex flex-col items-center justify-center py-12 gap-4'>
                    <p className='text-default-400'>Failed to load schemas</p>
                    <Button
                        onPress={handleReload}
                        isLoading={isReloading}
                        color='primary'
                        variant='flat'
                    >
                        Reload Schemas
                    </Button>
                </div>
            ) : categories.length === 0 ? (
                <div className='text-center border-2 border-dashed border-default-200 p-12 rounded-xl'>
                    <BookOpenIcon className='mx-auto size-12 text-default-300' />
                    <p className='mt-4 text-default-500'>No schema categories yet. Create your first one to get started!</p>
                </div>
            ) : (<>
                <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4'>
                    {(showAllCategories ? categories : categories.slice(0, 6)).map(category => (
                        <Card
                            key={category.id}
                            isPressable
                            isHoverable
                            className='p-6 hover:border-primary-300 transition-all'
                            shadow='sm'
                        >
                            <CustomLink to={routes.category.index.resolve({ categoryId: category.id })}>
                                <CardBody className='p-0'>
                                    <h3 className='text-lg font-semibold text-primary-600'>{category.label}</h3>
                                    <p className='text-default-500 mt-2'>System version ID: {category.systemVersionId}</p>
                                </CardBody>
                            </CustomLink>
                        </Card>
                    ))}
                </div>
                {categories.length > 6 && (
                    <div className='flex justify-center'>
                        <Button
                            variant='light'
                            onPress={() => setShowAllCategories(!showAllCategories)}
                            className='text-primary-600'
                        >
                            {showAllCategories ? 'Show Less' : `Show All (${categories.length})`}
                        </Button>
                    </div>
                )}
            </>)}
        </div>
    );
}

type FeatureCardProps = {
    icon: ReactNode;
    title: string;
    description: string;
    buttonText?: string;
    buttonVariant?: 'solid' | 'flat' | 'ghost';
    buttonColor?: 'default' | 'primary' | 'secondary' | 'success';
    buttonAction?: () => void;
    isLoading?: boolean;
    isDisabled?: boolean;
};

function FeatureCard({ icon, title, description, buttonText, buttonVariant = 'solid', buttonColor = 'primary', buttonAction, isLoading, isDisabled }: FeatureCardProps) {
    return (
        <Card className='p-6 h-full flex flex-col'>
            <CardBody className='flex flex-col gap-4 h-full p-0'>
                <div className='flex justify-center'>{icon}</div>
                <div className='flex flex-col items-center text-center grow min-h-[120px]'>
                    <h3 className='text-xl font-semibold text-default-800'>
                        <span>{title}</span>
                    </h3>
                    <p className='text-default-600 mt-2'>
                        <span>{description}</span>
                    </p>
                </div>
                <div className='h-[40px] flex items-center justify-center'>
                    {buttonText && (
                        <Button
                            color={buttonColor}
                            variant={buttonVariant}
                            onPress={buttonAction}
                            endContent={<FaArrowRight className='size-3' />}
                            className='w-full max-w-[200px]'
                            isLoading={isLoading}
                            isDisabled={isDisabled}
                        >
                            <span>{buttonText}</span>
                        </Button>
                    )}
                </div>
            </CardBody>
        </Card>
    );
}

type AddSchemaModalProps = {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (label: string, type: NewSchemaType) => void;
};

export function AddSchemaModal({ isOpen, onClose, onSubmit }: AddSchemaModalProps) {
    const [ label, setLabel ] = useState('');

    function submit() {
        if (!label.trim()) {
            toast.error('Please provide a valid label for the schema.');
            return;
        }
        onSubmit(label, EMPTY_SCHEMA);
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
                    <Button
                        color='primary'
                        onPress={submit}
                        isDisabled={!label.trim()}
                    >
                        Create Schema
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}
