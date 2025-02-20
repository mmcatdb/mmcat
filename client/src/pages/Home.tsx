import { useCallback, useEffect, useState } from 'react';
import { CustomLink } from '@/components/common';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader } from '@nextui-org/react';
import { toast } from 'react-toastify';

const DOCUMENTATION_URL = import.meta.env.VITE_DOCUMENTATION_URL;

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function Home() {
    const [ categories, setCategories ] = useState<SchemaCategoryInfo[]>();
    const [ isCreatingSchema, setIsCreatingSchema ] = useState(false);
    const [ isCreatingExampleSchema, setIsCreatingExampleSchema ] = useState(false);
    const [ isModalOpen, setIsModalOpen ] = useState(false);

    async function fetchCategories() {
        const result = await api.schemas.getAllCategoryInfos({});
        if (!result.status)
            return;

        setCategories(result.data.map(SchemaCategoryInfo.fromServer));
    }

    useEffect(() => {
        // TODO signal/abort
        void fetchCategories();
    }, []);

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
            setCategories(categories =>
                categories ? [ ...categories, newCategory ] : [ newCategory ],
            );

            toast.success(
                `${isExample ? 'Example schema' : 'Schema'} '${newCategory.label}' created successfully!`,
            );
        },
        [],
    );

    return (
        <div>
            <h1 className='heading-main'>MM-cat</h1>
            <p>
                A multi-model data modeling framework based on category theory.
            </p>
            <br />
            <p>
                Detailed instructions on how to use this tool can be found <a href={DOCUMENTATION_URL} className='underline text-blue-600 hover:text-blue-800 visited:text-purple-600'>here</a>.
            </p>
            <h2 className='mt-3'>Current schema categories</h2>
            {categories ? (<>
                <div className='flex flex-col'>
                    {categories.map(category => (
                        <div key={category.id}>
                            <CustomLink to={routes.category.index.resolve({ categoryId: category.id })}>
                                {category.label}
                            </CustomLink>
                        </div>
                    ))}
                </div>
                <h2 className='mt-3'>Add example schema category</h2>
                <div className='flex'>
                    {EXAMPLE_SCHEMAS.map(example => (
                        <Button 
                            key={example} 
                            onPress={() => handleCreateSchema(example, true)}
                            isLoading={isCreatingExampleSchema}
                        >
                            {example}
                        </Button>
                    ))}
                </div>
                <h2 className='mt-3'>Add empty schema category</h2>
                <Button
                    key={'newSchema'} 
                    onPress={() => setIsModalOpen(true)}
                    isLoading={isCreatingSchema}
                >
                        + Add schema
                </Button>
            </>) : (
                <p>Loading...</p>
            )}

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

export function AddSchemaModal({
    isOpen,
    onClose,
    onSubmit,
    isSubmitting,
}: AddSchemaModalProps) {
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
                <ModalHeader>Add New Schema</ModalHeader>
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
