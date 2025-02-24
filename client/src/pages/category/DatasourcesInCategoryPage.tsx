import { useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { EmptyState } from '@/components/TableCommon';
import { useLoaderData, type Params } from 'react-router-dom';
import { Button } from '@nextui-org/react';
import { AddIcon } from '@/components/icons/PlusIcon';

export function DatasourcesInCategoryPage() {
    const data = useLoaderData() as DatasourcesInCategoryLoaderData;
    const [ datasourcesIn, setDatasourcesIn ] = useState<Datasource[]>(data.datasourcesIn);
    const [ datasourcesNotIn, setDatasourcesNotIn ] = useState<Datasource[]>(data.datasourcesNotIn);

    const { category } = useCategoryInfo();
    const [ isModalOpen, setIsModalOpen ] = useState(false);

    function onDatasourceCreated(newDatasource: Datasource) {
        setDatasourcesNotIn(prev => [ ...prev, newDatasource ]);
    }

    async function deleteDatasource(id: string) {
        const response = await api.datasources.deleteDatasource({ id });

        if (!response.status) {
            toast.error('Failed to delete datasource. Please try again.');
            return;
        }

        setDatasourcesIn(prev => prev.filter(ds => ds.id !== id));
        setDatasourcesNotIn(prev => prev.filter(ds => ds.id !== id));
    }

    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1 className='text-xl font-bold'>Datasources in {category.label} (with mapping)</h1>

                <Button
                    onPress={() => setIsModalOpen(true)}
                    color='primary'
                    startContent={<AddIcon />}
                >
                    Add Datasource
                </Button>
            </div>

            <div className='mt-5'>
                {datasourcesIn.length > 0 ? (
                    <DatasourcesTable
                        datasources={datasourcesIn}
                        deleteDatasource={deleteDatasource}
                    />
                ) : (
                    <div className = 'text-center border border-zinc-500 p-6'>
                        There is no datasources with mappings available. You can add mapping via Other Datasources.
                    </div>
                )}
            </div>

            <div className='flex items-center justify-between mt-10'>
                <h1 className='text-xl font-bold'>Other Datasources</h1>
            </div>

            <div className='mt-5'>
                {datasourcesNotIn.length > 0 ? (
                    <DatasourcesTable
                        datasources={datasourcesNotIn}
                        deleteDatasource={deleteDatasource}
                    />
                ) : (
                    <EmptyState
                        message='No other datasources available.'
                        buttonText='+ Add Datasource'
                        onButtonClick={() => setIsModalOpen(true)}
                    />
                )}
            </div>

            <DatasourceModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onDatasourceCreated={onDatasourceCreated}
            />
        </div>
    );
}

DatasourcesInCategoryPage.loader = datasourcesInCategoryLoader;

export type DatasourcesInCategoryLoaderData = {
    datasourcesIn: Datasource[];
    datasourcesNotIn: Datasource[];
};

async function datasourcesInCategoryLoader({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<DatasourcesInCategoryLoaderData> {
    if (!categoryId)
        throw new Error('Action ID is required');

    const [ inCategoryResponse, allResponse ] = await Promise.all([
        api.datasources.getAllDatasources({}, { categoryId: categoryId }),
        api.datasources.getAllDatasources({}),
    ]);
    if (!inCategoryResponse.status || !allResponse.status)
        throw new Error('Failed to load datasources in category');

    const datasourcesIn = inCategoryResponse.data.map(Datasource.fromServer);
    const datasourcesNotIn = allResponse.data
        .filter(ds => !datasourcesIn.some(inCategory => inCategory.id === ds.id))
        .map(Datasource.fromServer);

    return {
        datasourcesIn,
        datasourcesNotIn,
    };
}
