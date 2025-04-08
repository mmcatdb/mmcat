import { type Params, useLoaderData, useNavigate, useParams } from 'react-router-dom';
import { api } from '@/api';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { Mapping, type MappingFromServer, type MappingInit } from '@/types/mapping';
import { type KeyFromServer, type SignatureIdFromServer } from '@/types/identifiers';
import { toast } from 'react-toastify';
import { Input } from '@nextui-org/react';
import { useState } from 'react';
import { Category } from '@/types/schema';

export function NewMappingPage() {
    const { category } = useLoaderData() as NewMappingLoaderData;
    const { id: datasourceId } = useParams<{ id: string }>(); // From route :id
    const navigate = useNavigate();
    const [ kindName, setKindName ] = useState('New Mapping');

    const initialMappingData: MappingFromServer = {
        id: '',
        kindName,
        categoryId: category.id,
        datasourceId: datasourceId ?? 'a3553b6a-e6aa-4a1c-af11-e294c89a4044', // FIXME
        rootObjectKey: 0 as KeyFromServer,
        primaryKey: [ 'EMPTY' ] as SignatureIdFromServer,
        accessPath: {
            name: { value: 'root' },
            signature: 'EMPTY',
            subpaths: [],
        },
        version: '',
    };

    const initialMapping = Mapping.fromServer(initialMappingData);

    const handleSaveMapping = async (mapping: Mapping) => {
        const mappingInit: MappingInit = {
            categoryId: mapping.categoryId,
            datasourceId: mapping.datasourceId,
            rootObjectKey: mapping.rootObjexKey.toServer(),
            primaryKey: mapping.primaryKey.toServer(),
            kindName,
            accessPath: mapping.accessPath.toServer(),
        };

        const response = await api.mappings.createMapping({}, mappingInit);
        if (response.status) {
            toast.success('Mapping created successfully!');
            navigate(`/schema-categories/${category.id}/datasources/${mapping.datasourceId}`);
        }
        else {
            toast.error('Failed to create mapping');
        }
    };

    return (
        <div>
            <h1 className='text-2xl font-bold mb-4'>Create New Mapping</h1>
            <Input
                label='Kind Name'
                value={kindName}
                onChange={e => setKindName(e.target.value)}
                className='mb-4 max-w-xs'
            />
            <MappingEditor category={category} mapping={initialMapping} onSave={handleSaveMapping} />
        </div>
    );
}

NewMappingPage.loader = newMappingLoader;

export type NewMappingLoaderData = {
    category: Category;
};

async function newMappingLoader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
    if (!categoryId)
        throw new Error('Category ID required');

    const categoryResponse = await api.schemas.getCategory({ id: categoryId });

    if (!categoryResponse.status)
        throw new Error('Failed to load category');

    return { category: Category.fromServer(categoryResponse.data) };
}

// export type NewMappingLoaderData = {
//     category: Category;
//     datasource: Datasource;
// };

// async function newMappingLoader({ params: { categoryId, id } }: { params: Params<'categoryId' | 'id'> }): Promise<NewMappingLoaderData> {
//     if (!categoryId || !id)
//         throw new Error('Category ID and Datasource ID are required');

//     const [ categoryResponse, datasourceResponse ] = await Promise.all([
//         api.schemas.getCategory({ id: categoryId }),
//         api.datasources.getDatasource({ id }),
//     ]);

//     if (!categoryResponse.status || !datasourceResponse.status)
//         throw new Error('Failed to load category or datasource');

//     return {
//         category: Category.fromServer(categoryResponse.data),
//         datasource: Datasource.fromServer(datasourceResponse.data),
//     };
// }
