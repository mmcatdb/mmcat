import { useCallback, useEffect, useState } from 'react';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader } from '@nextui-org/react';
import { toast } from 'react-toastify';

const EXAMPLE_SCHEMAS = [
    'basic',
    'adminer',
] as const;

export function Home() {
    const [ categories, setCategories ] = useState<SchemaCategoryInfo[]>();
    const [ creatingSchema, setCreatingSchema ] = useState<string>();

    async function fetchCategories() {
        const result = await api.schemas.getAllCategoryInfos({});
        if (!result.status)
            return;

        setCategories(result.data.map(SchemaCategoryInfo.fromServer));
    }

    useEffect(() => {
        void fetchCategories();
    }, []);

    const handleCreateSchema = useCallback(
        async (name: string, isExample = false) => {
            setCreatingSchema(name);

            const response = isExample
                ? await api.schemas.createExampleCategory({ name })
                : await api.schemas.createNewCategory({}, { label: name });

            setCreatingSchema(undefined);

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
        <div className='text-justify text-lg'>
            <h1 className='heading-main text-4xl'>MM-cat</h1>
            <p>
                A multi-model data modeling framework based on category theory.
            </p>
            <p className='mt-3'>
                Adminer is designed for visualizing multi-model data and references between them, based on the schemata of schemafull databases and schema category.
                Using the structure of the schema category, it connects and allows traversal of stored records both within a single database and across heterogeneous systems.
                It supports filtering and multiple data visualization modes.
            </p>
            <h2 className='mt-3'>Current schema categories</h2>
            {categories ? (<>
                <div className='flex flex-col'>
                    {categories.map(category => (
                        <div key={category.id} className='text-primary-500'>
                            {category.label}
                        </div>
                    ))}
                </div>
                <h2 className='mt-3'>Add example schema category</h2>
                <div className='flex mt-1 gap-2'>
                    {EXAMPLE_SCHEMAS.map(example => (
                        <Button
                            key={example}
                            onPress={() => handleCreateSchema(example, true)}
                            isLoading={creatingSchema === example}
                        >
                            {example}
                        </Button>
                    ))}
                </div>
            </>) : (
                <p>Loading...</p>
            )}
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
