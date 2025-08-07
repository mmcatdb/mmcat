import { type Params, useLoaderData, useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { type Mapping } from '@/types/mapping';
import { useMemo } from 'react';
import { Category } from '@/types/schema';
import { routes } from '@/routes/routes';
import { Datasource } from '@/types/Datasource';
import { PageLayout } from '@/components/RootLayout';

/**
 * Page for adding a new mapping.
 */
export function NewMappingPage() {
    const { category, datasource } = useLoaderData() as NewMappingLoaderData;
    const navigate = useNavigate();

    const input = useMemo(() => ({
        mapping: undefined,
        datasource,
    }), [ datasource ]);

    function mappingCreated(mapping: Mapping) {
        navigate(routes.category.datasources.detail.resolve({ categoryId: category.id, datasourceId: mapping.datasourceId }));
    }

    return (
        <PageLayout isFullscreen>
            <MappingEditor
                category={category}
                input={input}
                onSave={mappingCreated}
                onCancel={() => navigate(-1)}
            />
        </PageLayout>
    );
}

NewMappingPage.loader = newMappingLoader;

export type NewMappingLoaderData = {
    category: Category;
    datasource: Datasource;
};

async function newMappingLoader({ params: { categoryId, datasourceId } }: { params: Params<'categoryId' | 'datasourceId'> }) {
    if (!categoryId)
        throw new Error('Category ID required');

    if (!datasourceId)
        throw new Error('Datasource ID is required');

    const [ categoryResponse, datasourceResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.datasources.getDatasource({ id: datasourceId }),
    ]);

    if (!categoryResponse.status || !datasourceResponse.status)
        throw new Error('Failed to load category');

    return {
        category: Category.fromResponse(categoryResponse.data),
        datasource: Datasource.fromResponse(datasourceResponse.data),
    };
}
