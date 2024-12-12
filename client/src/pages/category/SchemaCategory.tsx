import { api } from '@/api';
import { createInitialGraphValue, SchemaCategory as SchemaCategoryType } from '@/types/schema';
import { SchemaUpdate } from '@/types/schema/SchemaUpdate';
import { type Params, useLoaderData } from 'react-router-dom';
import { Portal, portals } from '@/components/common';
import { logicalModelsFromServer } from '@/types/datasource';
import { type Dispatch, type SetStateAction, useState } from 'react';
import { GraphDisplay } from '@/components/GraphDisplay';
import { type GraphValue } from '@/components/graph';
import { Button } from '@nextui-org/react';
import { FaXmark } from 'react-icons/fa6';
import { Key } from '@/types/identifiers';

export function SchemaCategory() {
    const { category, updates } = useLoaderData() as SchemaCategoryLoaderData;
    const [ value, setValue ] = useState(() => createInitialGraphValue(category));

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

            <div className='relative'>
                <GraphDisplay value={value} setValue={setValue} className='min-h-[600px] w-full' />
                {Object.keys(value.selectedNodes).length > 0 && (
                    <div className='z-20 absolute top-2 right-2'>
                        <SelectedNodesCard category={category} value={value} setValue={setValue} />
                    </div>
                )}
            </div>
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

    const [ categoryResponse, updatesResponse, datasourcesResponse, mappingsResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.schemas.getCategoryUpdates({ id: categoryId }),
        api.datasources.getAllDatasources({}, { categoryId: categoryId }),
        api.mappings.getAllMappingsInCategory({}, { categoryId: categoryId }),
    ]);

    if (!categoryResponse.status || !updatesResponse.status || !datasourcesResponse.status || !mappingsResponse.status)
        throw new Error('Failed to load schema category');

    const updates = updatesResponse.data.map(SchemaUpdate.fromServer);
    const logicalModels = logicalModelsFromServer(datasourcesResponse.data, mappingsResponse.data);
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

type SelectedNodesCardProps = Readonly<{
    category: SchemaCategoryType;
    value: GraphValue;
    setValue: Dispatch<SetStateAction<GraphValue>>;
}>;

function SelectedNodesCard({ category, value, setValue }: SelectedNodesCardProps) {
    function unselectNode(nodeId: string) {
        setValue(prev => {
            const selectedNodes = { ...prev.selectedNodes };
            delete selectedNodes[nodeId];
            return { ...prev, selectedNodes };
        });
    }

    return (
        <div className='min-w-[200px] p-3 rounded-lg bg-black'>
            <div className='flex items-center justify-between pb-1'>
                <h3 className='font-semibold'>Selected objects</h3>
                <Button isIconOnly variant='light' size='sm' onClick={() => setValue(prev => ({ ...prev, selectedNodes: {} }))}>
                    <FaXmark />
                </Button>
            </div>
            <div className='flex flex-col'>
                {Object.values(value.selectedNodes).map(node => {
                    // TODO this is a hack, we should store the key on the node (or even the object)?
                    const object = category.getObject(Key.createNew(+node.id));

                    return (
                        <div key={node.id} className='flex items-center gap-2'>
                            <span className='text-primary font-semibold'>{object.key.toString()}</span>
                            {node.label}
                            <div className='grow' />
                            <Button isIconOnly variant='light' size='sm' onClick={() => unselectNode(node.id)}>
                                <FaXmark />
                            </Button>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
