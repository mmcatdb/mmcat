import { type Params, useLoaderData, useLocation, useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { Mapping, type MappingResponse, type MappingInit } from '@/types/mapping';
import { type KeyResponse, type SignatureIdResponse } from '@/types/identifiers';
import { toast } from 'react-toastify';
import { useState } from 'react';
import { Category } from '@/types/schema';
import { routes } from '@/routes/routes';

/**
 * Page for adding a new mapping.
 */
export function NewMappingPage() {
    const { category } = useLoaderData() as NewMappingLoaderData;
    const navigate = useNavigate();
    const location = useLocation();
    const [ kindName, setKindName ] = useState('');

    // Get datasource ID from route state
    // const datasourceId = location.state?.datasourceId;
    const { datasourceId, datasourceLabel } = location.state || {};

    if (!datasourceId) {
        navigate(-1); // Go back if no datasource ID
        // toast.error('Datasource ID is required');
        return null;
    }

    const initialMappingData: MappingResponse = {
        id: '',
        kindName,
        categoryId: category.id,
        datasourceId,
        rootObjexKey: 0 as KeyResponse,
        primaryKey: [ 'EMPTY' ] as SignatureIdResponse,
        accessPath: {
            name: { value: 'root' },
            signature: 'EMPTY',
            subpaths: [],
        },
        version: '',
    };

    const initialMapping = Mapping.fromResponse(initialMappingData);

    async function handleSaveMapping(mapping: Mapping, finalKindName: string) {
        const mappingInit: MappingInit = {
            categoryId: mapping.categoryId,
            datasourceId: mapping.datasourceId,
            rootObjexKey: mapping.rootObjexKey.toServer(),
            primaryKey: mapping.primaryKey.toServer(),
            kindName: finalKindName,
            accessPath: mapping.accessPath.toServer(),
        };

        const response = await api.mappings.createMapping({}, mappingInit);
        if (response.status) {
            toast.success('Mapping created successfully!');
            navigate(routes.category.datasource.resolve({ categoryId: category.id, datasourceId: mapping.datasourceId }));
        }
        else {
            toast.error('Failed to create mapping');
        }
    }

    return (
        <MappingEditor
            category={category}
            mapping={initialMapping}
            kindName={kindName}
            setKindName={setKindName}
            onSave={handleSaveMapping}
            datasourceLabel={datasourceLabel}
        />
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

    return { category: Category.fromResponse(categoryResponse.data) };
}
