import { useCallback, useEffect, useState } from 'react';
import { CustomLink } from '@/components/common';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Card, CardBody } from '@nextui-org/react';
import { toast } from 'react-toastify';
import { BookOpenIcon } from '@heroicons/react/24/solid';
import { FaDatabase, FaPlus, FaArrowRight } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';

const DOCUMENTATION_URL = 'https://mmcatdb.com/getting-started/quick-start/';
const EXAMPLE_SCHEMAS = [ 'basic' ] as const;

export function Home() {
    const [ categories, setCategories ] = useState<SchemaCategoryInfo[]>();
    const [ isCreatingSchema, setIsCreatingSchema ] = useState(false);
    const [ isCreatingExampleSchema, setIsCreatingExampleSchema ] = useState(false);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ showAllCategories, setShowAllCategories ] = useState(false);

    useEffect(() => {
        void fetchCategories();
    }, []);

    async function fetchCategories() {
        const result = await api.schemas.getAllCategoryInfos({});
        if (result.status)
            setCategories(result.data.map(SchemaCategoryInfo.fromServer));
    }

    const handleCreateSchema = useCallback(async (name: string, isExample = false) => {
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
        setCategories(prev => [ newCategory, ...(prev ?? []) ]);

        toast.success(`${isExample ? 'Example schema' : 'Schema'} '${name}' created successfully!`);
    }, []);

    return (
        <div className='p-6 max-w-7xl mx-auto space-y-16'>
            <HeaderSection />
            <GettingStartedSection />
            <SchemaCategoriesSection
                categories={categories}
                showAllCategories={showAllCategories}
                setShowAllCategories={setShowAllCategories}
                onOpenModal={() => setIsModalOpen(true)}
                isCreatingSchema={isCreatingSchema}
                isCreatingExampleSchema={isCreatingExampleSchema}
                onCreateSchema={handleCreateSchema}
            />
            <AddSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={label => handleCreateSchema(label, false)}
                isSubmitting={isCreatingSchema}
            />
        </div>
    );
}

function HeaderSection() {
    return (
        <div className='space-y-6 text-center md:text-left'>
            <h1 className='text-5xl font-bold text-primary-600 bg-gradient-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                MM-cat
            </h1>
            <p className='text-default-600 text-xl mx-auto'>
                A <span className='font-semibold text-primary-600'>multi-model data modeling framework</span> powered by category theory.
                Model, transform, and explore multi-model data without database limitations.
            </p>
            <Button 
                as='a' 
                href={DOCUMENTATION_URL} 
                variant='flat'
                color='primary'
                target='_blank' 
                endContent={<BookOpenIcon className='w-5 h-5' />}
                className='font-medium'
            >
                    Documentation
            </Button>
        </div>
    );
}

function GettingStartedSection() {
    const navigate = useNavigate();

    return (
        <div className='space-y-8'>
            <div className='text-center'>
                <h2 className='text-3xl font-bold bg-gradient-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                    Get Started in 3 Steps
                </h2>
                <p className='text-default-500 mt-2 max-w-2xl mx-auto'>
                    Quickly set up your multi-model data modeling environment with these simple steps
                </p>
            </div>
            <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
                <FeatureCard
                    icon={<div className='w-14 h-14 rounded-full bg-primary-100 flex items-center justify-center'>
                        <FaDatabase className='w-7 h-7 text-primary-600' />
                    </div>}
                    title='Connect Data Sources'
                    description='Link your existing databases or files to start modeling your data.'
                    buttonText='Connect Now'
                    buttonVariant='solid'
                    buttonColor='primary'
                    buttonAction={() => navigate(routes.datasources)}
                />
                <FeatureCard
                    icon={<div className='w-14 h-14 rounded-full bg-secondary-100 flex items-center justify-center'>
                        <FaPlus className='w-7 h-7 text-secondary-600' />
                    </div>}
                    title='Create Schema'
                    description='Start a new project to model your data relationships and structure.'
                    buttonText='New Schema'
                    buttonVariant='solid'
                    buttonColor='secondary'
                    buttonAction={() => navigate(routes.categories)} // TODO: href on this page, to next section
                />
                <FeatureCard
                    icon={<div className='w-14 h-14 rounded-full bg-success-100 flex items-center justify-center'>
                        <BookOpenIcon className='w-7 h-7 text-success-600' />
                    </div>}
                    title='Define Objects'
                    description='Open schema category and use our visual editor to define objects and their relationships.'
                    buttonText='Explore'
                    buttonVariant='solid'
                    buttonColor='success'
                    buttonAction={() => navigate(routes.categories)}
                />
            </div>
        </div>
    );
}

function SchemaCategoriesSection({
    categories,
    showAllCategories,
    setShowAllCategories,
    onOpenModal,
    isCreatingSchema,
    isCreatingExampleSchema,
    onCreateSchema,
}: {
    categories: SchemaCategoryInfo[] | undefined;
    showAllCategories: boolean;
    setShowAllCategories: (state: boolean) => void;
    onOpenModal: () => void;
    isCreatingSchema: boolean;
    isCreatingExampleSchema: boolean;
    onCreateSchema: (name: string, isExample?: boolean) => void;
}) {
    return (
        <div className='space-y-8'>
            <div className='flex flex-col md:flex-row md:items-end justify-between gap-4'>
                <div>
                    <h2 className='text-3xl font-bold bg-gradient-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                        Your Schema Categories
                    </h2>
                    <p className='text-default-500'>
                        Create or explore existing schema categories to model your data
                    </p>
                </div>
                <div className='flex flex-wrap gap-3'>
                    <Button 
                        onPress={onOpenModal} 
                        isLoading={isCreatingSchema} 
                        color='primary'
                        startContent={<FaPlus className='w-4 h-4' />}
                    >
                        New Schema
                    </Button>
                    {EXAMPLE_SCHEMAS.map(example => (
                        <Button
                            key={example}
                            onPress={() => onCreateSchema(example, true)}
                            isLoading={isCreatingExampleSchema}
                            color='secondary'
                            variant='flat'
                            startContent={<FaPlus className='w-4 h-4' />}
                        >
                            Example Schema
                        </Button>
                    ))}
                </div>
            </div>

            {!categories ? (
                <div className='flex justify-center py-12'>
                    <p className='text-default-400'>Loading schemas...</p>
                </div>
            ) : categories.length === 0 ? (
                <div className='text-center border-2 border-dashed border-default-200 p-12 rounded-xl'>
                    <BookOpenIcon className='w-12 h-12 mx-auto text-default-300' />
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
    icon: React.ReactNode;
    title: string;
    description: string;
    buttonText?: string;
    buttonVariant?: 'solid' | 'flat' | 'ghost';
    buttonColor?: 'default' | 'primary' | 'secondary' | 'success';
    buttonAction?: () => void;
};

function FeatureCard({ icon, title, description, buttonText, buttonVariant = 'solid', buttonColor = 'primary', buttonAction }: FeatureCardProps) {
    return (
        <Card className='p-6 h-full flex flex-col'>
            <CardBody className='flex flex-col gap-4 flex-grow p-0'>
                {icon}
                <h3 className='text-xl font-semibold text-default-800'>{title}</h3>
                <p className='text-default-600 flex-grow'>{description}</p>
                {buttonText && (
                    <Button 
                        color={buttonColor}
                        variant={buttonVariant}
                        onPress={buttonAction}
                        className='mt-4 self-start'
                        endContent={<FaArrowRight className='w-3 h-3' />}
                    >
                        {buttonText}
                    </Button>
                )}
            </CardBody>
        </Card>
    );
}

type AddSchemaModalProps = {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (label: string) => void;
    isSubmitting: boolean;
};

export function AddSchemaModal({ isOpen, onClose, onSubmit, isSubmitting }: AddSchemaModalProps) {
    const [ label, setLabel ] = useState('');

    function handleSubmit() {
        if (!label.trim()) {
            toast.error('Please provide a valid label for the schema.');
            return;
        }
        onSubmit(label);
        handleClose();
    }

    function handleClose() {
        setLabel('');
        onClose();
    }

    return (
        <Modal isOpen={isOpen} onClose={handleClose} isDismissable={false}>
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
                        classNames={{
                            input: 'text-lg',
                        }}
                    />
                </ModalBody>
                <ModalFooter>
                    <Button variant='light' onPress={handleClose}>
                        Cancel
                    </Button>
                    <Button 
                        color='primary' 
                        onPress={handleSubmit} 
                        isLoading={isSubmitting}
                        isDisabled={!label.trim()}
                    >
                        Create Schema
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}
