import { api, type Resolved } from '@/api';
import { LogicalModel } from '@/types/logicalModel';
import { SchemaCategory as SchemaCategoryType } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { sleep } from '@/types/utils/common';
import { Suspense } from 'react';
import { type Params, useLoaderData, defer, Await } from 'react-router-dom';
import { LoadingComponent } from '../errorPages';
import { Portal, portals } from '@/components/common';

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
        api.schemas.getCategoryWrapper({ id: projectId }),
        api.schemas.getCategoryUpdates({ id: projectId }),
        api.logicalModels.getAllLogicalModelsInCategory({ categoryId: projectId }),
        sleep(2000),
    ])
        .then(([ categoryResponse, updatesResponse, modelsResponse ]) => {
            if (!categoryResponse.status || !updatesResponse.status || !modelsResponse.status)
                throw new Error('Failed to load schema category');

            const updates = updatesResponse.data.map(SchemaUpdate.fromServer);
            const logicalModels = modelsResponse.data.map(LogicalModel.fromServer);
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
