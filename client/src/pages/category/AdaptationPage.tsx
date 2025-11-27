import { useState } from 'react';
import { useLoaderData, type Params } from 'react-router-dom';
import { api } from '@/api';
import { Datasource } from '@/types/Datasource';
import { Mapping } from '@/types/mapping';
import { Category } from '@/types/schema';
import { AdaptationResultPage } from '@/components/adaptation/AdaptationResultPage';
import { CreateAdaptationPage } from '@/components/adaptation/CreateAdaptationPage';

export function AdaptationPage() {
    const { category, datasources, mappings } = useLoaderData() as AdaptationPageData;
    const [ phase, setPhase ] = useState('input');

    if (phase === 'input') {
        return (
            <CreateAdaptationPage category={category} datasources={datasources} mappings={mappings} onNext={() => setPhase('result')} />
        );
    }

    return (
        <AdaptationResultPage category={category} datasources={datasources} mappings={mappings} />
    );
}

type AdaptationPageData = {
    category: Category;
    datasources: Datasource[];
    mappings: Mapping[];
};

AdaptationPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId' | 'queryId'> }): Promise<AdaptationPageData> => {
    if (!categoryId)
        throw new Error('Category ID is required');

    const [ categoryResponse, datasourcesResponse, mappingsResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.datasources.getAllDatasources({}, { categoryId }),
        api.mappings.getAllMappingsInCategory({}, { categoryId }),
    ]);
    if (!categoryResponse.status)
        throw new Error('Failed to load category info');
    if (!datasourcesResponse.status)
        throw new Error('Failed to load datasources');
    if (!mappingsResponse.status)
        throw new Error('Failed to load mappings');

    return {
        category: Category.fromResponse(categoryResponse.data),
        datasources: datasourcesResponse.data.map(Datasource.fromResponse),
        mappings: mappingsResponse.data.map(Mapping.fromResponse),
    };
};
