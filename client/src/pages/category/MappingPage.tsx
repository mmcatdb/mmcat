import { api } from '@/api';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { Mapping } from '@/types/mapping';
import { Category } from '@/types/schema';
import { type Params, useLoaderData } from 'react-router-dom';

export function MappingPage() {
    const { category, mapping } = useLoaderData() as MappingLoaderData;
    // const { category } = useCategoryInfo();

    return (
        <div>
            <h1>Mapping {mapping.kindName}</h1>

            <div className='mt-4 p-4 bg-slate-500'>
                <p>
                Some text.
                </p>
                <p>
                category 1: {category.label}
                </p>
            </div>

            <div className='mt-4'>
                <MappingEditor category={category} mapping={mapping} />
            </div>
        </div>
    );
}

MappingPage.loader = mappingLoader;

export type MappingLoaderData = {
    category: Category;
    mapping: Mapping;
};

async function mappingLoader({ params: { categoryId, mappingId } }: { params: Params<'categoryId' | 'mappingId'> }) {
    if (!categoryId || !mappingId)
        throw new Error('Mapping ID is required');

    const [ categoryResponse, mappingResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.mappings.getMapping({ id: mappingId }),
    ]);
    if (!categoryResponse.status || !mappingResponse.status)
        throw new Error('Failed to load mapping');

    return {
        category: Category.fromServer(categoryResponse.data),
        mapping: Mapping.fromServer(mappingResponse.data),
    };
}
