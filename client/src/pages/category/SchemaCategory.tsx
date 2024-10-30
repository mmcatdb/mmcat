import { api } from '@/api';
import { LogicalModel } from '@/types/logicalModel';
import { SchemaCategory as SchemaCategoryType } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { type Params, useLoaderData } from 'react-router-dom';
import { Portal, portals } from '@/components/common';
import { SchemaCategoryGraph } from '@/components/project/SchemaCategoryGraph';

export function SchemaCategory() {
    const { category, updates } = useLoaderData() as SchemaCategoryLoaderData;

    return (
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
    );
}

type SchemaCategoryLoaderData = {
        category: SchemaCategoryType;
        updates: SchemaUpdate[];
};

export async function schemaCategoryLoader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
    if (!categoryId)
        throw new Error('Category ID is required');

    const [ categoryResponse, updatesResponse, modelsResponse ] = await Promise.all([
        api.schemas.getCategoryWrapper({ id: categoryId }),
        api.schemas.getCategoryUpdates({ id: categoryId }),
        api.logicalModels.getAllLogicalModelsInCategory({ categoryId: categoryId }),
    ]);

    if (!categoryResponse.status || !updatesResponse.status || !modelsResponse.status)
        throw new Error('Failed to load schema category');

    const updates = updatesResponse.data.map(SchemaUpdate.fromServer);
    const logicalModels = modelsResponse.data.map(LogicalModel.fromServer);
    const category = SchemaCategoryType.fromServer(categoryResponse.data, logicalModels);

    return { category, updates };
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
