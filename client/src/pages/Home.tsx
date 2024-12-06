import { useCallback, useEffect, useState } from 'react';
import { CustomLink } from '@/components/common';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button } from '@nextui-org/react';
import { toast } from 'react-toastify';

const DOCUMENTATION_URL = import.meta.env.VITE_DOCUMENTATION_URL;

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

export function Home() {
    const [ categories, setCategories ] = useState<SchemaCategoryInfo[]>();

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

    const [ isCreatingSchema, setIsCreatingSchema ] = useState(false);

    const createExampleSchema = useCallback(async (name: string) => {
        setIsCreatingSchema(true);
        const response = await api.schemas.createExampleCategory({ name });
        setIsCreatingSchema(false);
        if (!response.status)
            return;

        const newCategory = SchemaCategoryInfo.fromServer(response.data);
        setCategories(categories => categories ? [ ...categories, newCategory ] : [ newCategory ]);
        toast.success(`Example schema '${newCategory.label}' created successfully!`);
    }, []);

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
                            onPress={() => createExampleSchema(example)}
                            isLoading={isCreatingSchema}
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
