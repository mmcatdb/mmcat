import { useCallback, useEffect, useState } from 'react';
import { CustomLink } from '@/components/common';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Card } from '@nextui-org/react';
import { toast } from 'react-toastify';
import { BookOpenIcon } from '@heroicons/react/24/solid';
import { FaDatabase, FaPlus } from 'react-icons/fa';

const DOCUMENTATION_URL = 'https://mmcatdb.com/getting-started/quick-start/';
const EXAMPLE_SCHEMAS = [ 'basic' ] as const;

export function Home() {
    // const [ categories, setCategories ] = useState<SchemaCategoryInfo[]>([]);
    const [ isCreatingSchema, setIsCreatingSchema ] = useState(false);
    const [ isCreatingExampleSchema, setIsCreatingExampleSchema ] = useState(false);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    // const [ showAllCategories, setShowAllCategories ] = useState(false);

    async function fetchCategories() {
        const result = await api.schemas.getAllCategoryInfos({});
        if (!result.status)
            return;
        // setCategories(result.data.map(SchemaCategoryInfo.fromServer));
    }

    useEffect(() => {
        // TODO signal/abort
        void fetchCategories();
    }, []);

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
        // setCategories(categories =>
        //     categories ? [ ...categories, newCategory ] : [ newCategory ],
        // );

        toast.success(
            `${isExample ? 'Example schema' : 'Schema'} '${newCategory.label}' created successfully!`,
        );
    }, []);

    return (
        <div className='p-8 space-y-12'>
            {/* Main Section */}
            <div className='space-y-6'>
                <h1 className='text-4xl font-bold text-primary-500'>MM-cat</h1>
                <p className='text-default-700 text-lg'>
                    <span className='font-semibold'>A multi-model data modeling framework</span> powered by category theory.  
                    <span className='font-semibold'> Model, transform, and explore</span> multi-model data, without worrying about database-specific limitations.
                </p>
                <div className='flex gap-4'>
                    <Button
                        as='a'
                        href={DOCUMENTATION_URL}
                        // variant='filled'
                        color='primary'
                        target='_blank'
                        startContent={<BookOpenIcon className='w-6 h-6' />}
                    >
                        Read Documentation
                    </Button>
                    {/* <Button
                        as='a'
                        href={routes.category.index.resolve({ categoryId: 'basic' })}
                        variant='ghost'
                        color='secondary'
                        startContent={<PlayIcon className='w-6 h-6' />}
                    >
                        Explore Example Schema
                    </Button> */}
                </div>
            </div>

            {/* Getting Started */}
            <div className='space-y-6'>
                <h2 className='text-2xl font-semibold '>Getting Started</h2>
                <div className='grid grid-cols-1 md:grid-cols-3 gap-6 text-center'>
                    <Card className='p-6 shadow-medium hover:shadow-large transition'>
                        <FaDatabase className='w-12 h-12 mx-auto text-primary-500' />
                        <h3 className='mt-4 font-semibold text-lg'>1. Connect to a Data Source</h3>
                        <p className='text-default-600'>Link your existing databases or files to start modeling.</p>
                        <Button as={CustomLink} to={routes.datasources} variant='ghost' color='default'>
                            Connect Data
                        </Button>
                    </Card>

                    <Card className='p-6 shadow-medium hover:shadow-large transition'>
                        <FaPlus className='w-12 h-12 mx-auto text-secondary-500' />
                        <h3 className='mt-4 font-semibold text-lg'>2. Create a Schema Category</h3>
                        <p className='text-default-600'>Start a new project to model your data.</p>
                        <Button onPress={() => handleCreateSchema('new-schema')} variant='ghost' color='default'>
                            + Schema Category
                        </Button>
                    </Card>

                    <Card className='p-6 shadow-medium hover:shadow-large transition'>
                        <BookOpenIcon className='w-12 h-12 mx-auto text-green-500' />
                        <h3 className='mt-4 font-semibold text-lg'>3. Define Objects & Relations</h3>
                        <p className='text-default-600'>Use the Schema Category Editor to define objects and relations.</p>
                        <Button as={CustomLink} to={routes.categories} variant='ghost' color='default'>
                            Select Schema
                        </Button>
                    </Card>
                </div>
            </div>

            {/* Schema Categories Section */}
            <div className='space-y-6'>
                <h2 className='text-2xl font-semibold'>Add a Schema Category</h2>
                <p className='text-default-700'>
                    A <span className='font-semibold'>Schema Category</span> is a high-level model of your data, defining objects (data types) and relationships without worrying about database structure. It helps you focus on meaning, not storage.
                </p>
                <p className='text-default-600 font-semibold'>Start from scratch or explore a basic example:</p>
                <div className='flex flex-wrap gap-3'>
                    <Button
                        onPress={() => setIsModalOpen(true)}
                        isLoading={isCreatingSchema}
                        color='primary'
                        variant='ghost'
                        title='Add an empty schema category'
                    >
                        + Add Empty Schema
                    </Button>
                    {EXAMPLE_SCHEMAS.map(example => (
                        <Button 
                            key={example} 
                            onPress={() => handleCreateSchema(example, true)}
                            isLoading={isCreatingExampleSchema}
                            color='secondary'
                            variant='ghost'
                            title='Add an example (pre-made) schema category'
                        >
                            + Add Example Schema
                        </Button>
                    ))}
                </div>
            </div>

            {/* Current Schema Categories */}
            {/* <div className='space-y-6'>
                <h2 className='text-2xl font-semibold'>Explore existing Schema Categories</h2>
                {categories.length > 0 ? (
                    <>
                        <div className='grid grid-cols-1 sm:grid-cols-2 gap-4'>
                            {(showAllCategories ? categories : categories.slice(0, 4)).map(category => (
                                <Card key={category.id} isPressable className='p-4 shadow-medium hover:shadow-large transition'>
                                    <CustomLink to={routes.category.index.resolve({ categoryId: category.id })}>
                                        <CardBody>
                                            {category.label}
                                        </CardBody>
                                    </CustomLink>
                                </Card>
                            ))}
                        </div>
                        {categories.length > 4 && (
                            <Button
                                variant='ghost'
                                className='text-primary-500'
                                onPress={() => setShowAllCategories(!showAllCategories)}
                            >
                                {showAllCategories ? 'See Less' : 'See More'}
                            </Button>
                        )}
                    </>
                ) : (
                    <p className='text-default-500'>Loading...</p>
                )}
            </div> */}

            <AddSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={label => handleCreateSchema(label, false)}
                isSubmitting={isCreatingSchema}
            />
        </div>
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

    const handleSubmit = () => {
        if (!label.trim()) {
            toast.error('Please provide a valid label for the schema.');
            return;
        }
        onSubmit(label);
        handleClose();
    };

    const handleClose = () => {
        setLabel('');
        onClose();
    };

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
