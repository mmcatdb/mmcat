import { api, type Resolved } from '@/api';
import { SchemaCategory as SchemaCategoryType } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { Suspense } from 'react';
import { type Params, useLoaderData, defer, Await } from 'react-router-dom';
import { LoadingComponent } from '../errorPages';
import { Portal, portals } from '@/components/common';
import { SchemaCategoryGraph } from '@/components/project/SchemaCategoryGraph';
import { logicalModelsFromServer } from '@/types/datasource';

export function SchemaCategory() {
    const loaderData = useLoaderData() as SchemaCategoryLoaderData;

    return (
        <Suspense fallback={<LoadingComponent />}>
            <Await resolve={loaderData.data}>
                {({ category, updates }: Resolved<SchemaCategoryLoaderData, 'data'>) => (
                    <div>
                        <SchemaCategoryContext category={category} />
                        <h1>Schema category {category.label} overview</h1>
                        <p>
                            Some text.
                        </p>
                        <p>
                            updates: {updates.length}
                        </p>

                        <SchemaCategoryGraph category={category} />
                    </div>
                )}
            </Await>
        </Suspense>
    );
}

type SchemaCategoryLoaderData = {
    data: Promise<{
        category: SchemaCategoryType;
        updates: SchemaUpdate[];
    }>;
};

export function schemaCategoryLoader({ params: { projectId } }: { params: Params<'projectId'> }) {
    if (!projectId)
        throw new Error('Project ID is required');

    const data = Promise.all([
        api.schemas.getCategory({ id: projectId }),
        api.schemas.getCategoryUpdates({ id: projectId }),
        api.datasources.getAllDatasources({}, { categoryId: projectId }),
        api.mappings.getAllMappingsInCategory({}, { categoryId: projectId }),
    ])
        .then(([ categoryResponse, updatesResponse, datasourcesResponse, mappingsResponse ]) => {
            if (!categoryResponse.status || !updatesResponse.status || !datasourcesResponse.status || !mappingsResponse.status)
                throw new Error('Failed to load schema category');

            const updates = updatesResponse.data.map(SchemaUpdate.fromServer);
            const logicalModels = logicalModelsFromServer(datasourcesResponse.data, mappingsResponse.data);
            const category = SchemaCategoryType.fromServer(categoryResponse.data, logicalModels);

            return { category, updates };
        });

    return defer({ data } satisfies SchemaCategoryLoaderData);
}

type SchemaCategoryContextProps = Readonly<{
    category: SchemaCategoryType;
}>;

function SchemaCategoryContext({ category }: SchemaCategoryContextProps) {
    return (
        <Portal to={portals.context}>
            <div className='p-2'>
                Context for: {category.label}
            </div>
        </Portal>
    );
}
