import { useLoaderData, useLocation, useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { Mapping, type MappingFromServer, type MappingInit } from '@/types/mapping';
import { type KeyFromServer, type SignatureIdFromServer } from '@/types/identifiers';
import { toast } from 'react-toastify';
import { Input } from '@nextui-org/react';
import { useState } from 'react';
import { Category } from '@/types/schema';

/**
 * Page for adding a new mapping.
 */
export function NewMappingPage() {
    const { category } = useLoaderData() as NewMappingLoaderData;
    const navigate = useNavigate();
    const [ kindName, setKindName ] = useState('');
    const location = useLocation();

    // Get datasource ID from route state
    const datasourceId = location.state?.datasourceId;

    if (!datasourceId) {
        navigate(-1); // Go back if no datasource ID
        // toast.error('Datasource ID is required');
        return null;
    }

    const initialMappingData: MappingFromServer = {
        id: '',
        kindName,
        categoryId: category.id,
        datasourceId: datasourceId,
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
                placeholder='Enter Kind Name or leave blank'
            />
            <MappingEditor category={category} mapping={initialMapping} onSave={handleSaveMapping} />
        </div>
    );
}

NewMappingPage.loader = newMappingLoader;

export type NewMappingLoaderData = {
    category: Category;
};

// @ts-expect-error FIXME
async function newMappingLoader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
    if (!categoryId)
        throw new Error('Category ID required');

    const categoryResponse = await api.schemas.getCategory({ id: categoryId });

    if (!categoryResponse.status)
        throw new Error('Failed to load category');

    return { category: Category.fromServer(categoryResponse.data) };
}
