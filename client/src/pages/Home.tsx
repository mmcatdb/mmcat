import { useCallback, useEffect, useState } from 'react';
import { CustomLink } from '@/components/common';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Card, CardBody } from '@nextui-org/react';
import { toast } from 'react-toastify';
import { BookOpenIcon } from '@heroicons/react/24/solid';
import { FaDatabase, FaPlus } from 'react-icons/fa';
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
        <div className='p-8 space-y-12'>
            <HeaderSection />
            <GettingStartedSection onCreateSchema={handleCreateSchema} />
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
        <div className='space-y-6'>
            <h1 className='text-4xl font-bold text-primary-500'>MM-cat</h1>
            <p className='text-default-700 text-lg'>
                <span className='font-semibold'>A multi-model data modeling framework</span> powered by category theory.
                <span className='font-semibold'> Model, transform, and explore</span> multi-model data, without worrying about database-specific limitations.
            </p>
            <Button as='a' href={DOCUMENTATION_URL} color='primary' target='_blank' startContent={<BookOpenIcon className='w-6 h-6' />}>
                Read Documentation
            </Button>
        </div>
    );
}

function GettingStartedSection({ onCreateSchema }: { onCreateSchema: (name: string, isExample?: boolean) => void }) {
    return (
        <div className='space-y-6'>
            <h2 className='text-2xl font-semibold'>Getting Started</h2>
            <div className='grid grid-cols-1 md:grid-cols-3 gap-6 text-center'>
                <FeatureCard
                    icon={<FaDatabase className='w-12 h-12 mx-auto text-primary-500' />}
                    title='1. Connect to a Data Source'
                    description='Link your existing databases or files to start modeling.'
                    linkText='Connect Data'
                    linkTo={routes.datasources}
                />
                <FeatureCard
                    icon={<FaPlus className='w-12 h-12 mx-auto text-secondary-500' />}
                    title='2. Create a Schema Category'
                    description='Start a new project to model your data.'
                    buttonText='+ Schema Category'
                    onButtonClick={() => onCreateSchema('new-schema')}
                />
                <FeatureCard
                    icon={<BookOpenIcon className='w-12 h-12 mx-auto text-green-500' />}
                    title='3. Define Objects & Relations'
                    description='Use the Schema Category Editor to define objects and relations.'
                    linkText='Select Schema'
                    linkTo={routes.categories}
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
        <div className='space-y-6'>
            <h2 className='text-2xl font-semibold'>Add a Schema Category</h2>
            <p className='text-default-700'>
                A <span className='font-semibold'>Schema Category</span> is a high-level model of your data, defining objects (data types) and relationships without worrying about database structure.
            </p>
            <div className='flex flex-wrap gap-3'>
                <Button onPress={onOpenModal} isLoading={isCreatingSchema} color='primary' variant='ghost'>
                    + Add Empty Schema
                </Button>
                {EXAMPLE_SCHEMAS.map(example => (
                    <Button
                        key={example}
                        onPress={() => onCreateSchema(example, true)}
                        isLoading={isCreatingExampleSchema}
                        color='secondary'
                        variant='ghost'
                    >
                        + Add Example Schema
                    </Button>
                ))}
            </div>

            <h2 className='text-2xl font-semibold'>Explore existing Schema Categories</h2>
            {!categories ? (
                <p className='text-default-500'>Loading...</p>
            ) : categories.length === 0 ? (
                <div className='text-center border p-6 rounded-lg border-default-200'>
                    <p className='mt-2 text-default-500'>No categories available. Create one to get started!</p>
                </div>
            ) : (<>
                <div className='grid grid-cols-1 sm:grid-cols-2 gap-4'>
                    {(showAllCategories ? categories : categories.slice(0, 4)).map(category => (
                        <Card key={category.id} isPressable className='p-4 shadow-medium hover:shadow-large transition'>
                            <CustomLink to={routes.category.index.resolve({ categoryId: category.id })}>
                                <CardBody>{category.label}</CardBody>
                            </CustomLink>
                        </Card>
                    ))}
                </div>
                {categories.length > 4 && (
                    <Button variant='ghost' className='text-primary-500' onPress={() => setShowAllCategories(!showAllCategories)}>
                        {showAllCategories ? 'See Less' : 'See More'}
                    </Button>
                )}
            </>)}
        </div>
    );
}

type FeatureCardProps = {
    icon: React.ReactNode;
    title: string;
    description: string;
    linkText?: string;
    linkTo?: string;
    buttonText?: string;
    onButtonClick?: () => void;
}

function FeatureCard({ icon, title, description, linkText, linkTo, buttonText, onButtonClick }: FeatureCardProps) {
    const navigate = useNavigate();

    return (
        <Card className='p-6 shadow-medium hover:shadow-large transition'>
            {icon}
            <h3 className='mt-4 font-semibold text-lg'>{title}</h3>
            <p className='text-default-600'>{description}</p>
            {linkText && linkTo && <Button variant='ghost' onPress={() => navigate(linkTo)}>{linkText}</Button>}
            {buttonText && <Button variant='ghost' onPress={onButtonClick}>{buttonText}</Button>}
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
                <ModalHeader className='text-lg font-semibold'>Add New Schema</ModalHeader>
                <ModalBody>
                    <Input
                        label='Schema Label'
                        placeholder='Enter schema label'
                        value={label}
                        onChange={e => setLabel(e.target.value)}
                        fullWidth
                    />
                </ModalBody>
                <ModalFooter>
                    <Button color='danger' variant='light' onPress={handleClose}>
                        Cancel
                    </Button>
                    <Button color='primary' onPress={handleSubmit} isLoading={isSubmitting}>
                        Create
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}
