import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { Mapping } from '@/types/mapping';
import { type Params, useLoaderData } from 'react-router-dom';

export function MappingPage() {
    const { mapping } = useLoaderData() as MappingLoaderData;
    const { category } = useCategoryInfo();

    return (
        <div className='p-4 bg-slate-500'>
            <h1>Mapping {mapping.kindName}</h1>
            <p>
                Some text.
            </p>
            <p>
                category 1: {category.label}
            </p>
        </div>
    );
}

MappingPage.loader = mappingLoader;

export type MappingLoaderData = {
    mapping: Mapping;
};

async function mappingLoader({ params: { mappingId } }: { params: Params<'mappingId'> }) {
    if (!mappingId)
        throw new Error('Mapping ID is required');

    return {
        mapping: await api.mappings.getMapping({ id: mappingId }).then(response => {
            if (!response.status)
                throw new Error('Failed to load mapping');

            return Mapping.fromServer(response.data);
        }),
    };
}
