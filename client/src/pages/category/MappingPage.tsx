import { api } from '@/api';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { Datasource } from '@/types/Datasource';
import { Mapping } from '@/types/mapping';
import { Category } from '@/types/schema';
import { useMemo } from 'react';
import { type Params, useLoaderData } from 'react-router-dom';

export function MappingPage() {
    const { category, mapping, datasource } = useLoaderData() as MappingLoaderData;
    // const { category } = useCategoryInfo();

    const input = useMemo(() => ({
        mapping,
        datasource,
    }), [ mapping, datasource ]);

    return (
        <div>
            <h1>Mapping {mapping.kindName}</h1>

            <div className='mt-4 p-4 bg-slate-500'>
                <p>
                    category: {category.label}
                </p>
            </div>

            <div className='mt-4'>
                <MappingEditor
                    category={category}
                    input={input}
                    // TODO this whole thing
                />
            </div>
        </div>
    );
}

MappingPage.loader = mappingLoader;

export type MappingLoaderData = {
    category: Category;
    mapping: Mapping;
    datasource: Datasource;
};

async function mappingLoader({ params: { categoryId, mappingId } }: { params: Params<'categoryId' | 'mappingId'> }) {
    if (!categoryId || !mappingId)
        throw new Error('Mapping ID is required');

    const [ categoryResponse, mappingResponse, datasourceResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.mappings.getMapping({ id: mappingId }),
        api.datasources.getDatasourceForMapping({}, { mappingId }),
    ]);
    if (!categoryResponse.status || !mappingResponse.status || !datasourceResponse.status)
        throw new Error('Failed to load mapping');

    return {
        category: Category.fromResponse(categoryResponse.data),
        mapping: Mapping.fromResponse(mappingResponse.data),
        datasource: Datasource.fromResponse(datasourceResponse.data),
    };
}
