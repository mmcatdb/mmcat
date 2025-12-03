import { api } from '@/api';
import { InfoBanner, InfoTooltip } from '@/components/common';
import { AccessPathDisplay } from '@/components/mapping/AccessPathDisplay';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { PageLayout } from '@/components/RootLayout';
import { Datasource } from '@/types/Datasource';
import { Mapping } from '@/types/mapping';
import { Category } from '@/types/schema';
import { useBannerState } from '@/types/utils/useBannerState';
import { Button } from '@heroui/react';
import { useMemo, useState } from 'react';
import { type Params, useLoaderData } from 'react-router-dom';

export function MappingPage() {
    const { category, mapping, datasource } = useLoaderData() as MappingLoaderData;

    const banner = useBannerState('mapping-detail-page');
    const [ isEditing, setIsEditing ] = useState(false);

    const input = useMemo(() => ({
        mapping,
        datasource,
    }), [ mapping, datasource ]);

    if (isEditing) {
        return (
            <PageLayout isFullscreen>
                <MappingEditor
                    category={category}
                    input={input}
                    // TODO update the mapping data
                    onSave={() => setIsEditing(false)}
                    onCancel={() => setIsEditing(false)}
                />
            </PageLayout>
        );
    }

    return (
        <PageLayout>
            <div className='mb-4 flex items-center gap-2'>
                <h1 className='text-xl font-bold text-default-800'>Mapping {mapping.kindName}</h1>

                <InfoTooltip {...banner} />
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <MappingDetailInfoInner />
            </InfoBanner>

            <div className='mt-4 p-4 bg-slate-500'>
                <p>
                    category: {category.label}
                </p>
            </div>

            <Button color='primary' onPress={() => setIsEditing(true)}>
                Edit Mapping
            </Button>

            <div className='grid grid-cols-2'>
                <AccessPathDisplay property={mapping.accessPath} />

                <div>
                    {/* TODO info about the selected property */}
                </div>
            </div>
        </PageLayout>
    );
}

export type MappingLoaderData = {
    category: Category;
    mapping: Mapping;
    datasource: Datasource;
};

MappingPage.loader = async ({ params: { categoryId, mappingId } }: { params: Params<'categoryId' | 'mappingId'> }) => {
    if (!categoryId || !mappingId)
        throw new Error('Mapping ID is required');

    const [ categoryResponse, mappingResponse, datasourceResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        api.mappings.getMapping({ id: mappingId }),
        api.datasources.getDatasourceForMapping({}, { mappingId }),
    ]);
    if (!categoryResponse.status || !mappingResponse.status || !datasourceResponse.status)
        throw new Error('Failed to load mapping');

    return {
        category: Category.fromResponse(categoryResponse.data),
        mapping: Mapping.fromResponse(mappingResponse.data),
        datasource: Datasource.fromResponse(datasourceResponse.data),
    };
};

function MappingDetailInfoInner() {
    return (<>
        <h2 className='text-lg font-semibold mb-2'>Defining a Mapping</h2>

        {/* TODO */}
        {/* <ul className='mt-2 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Edit:</span> You can update connection details, but the type cannot be changed.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Password:</span> If edit password field left empty, the existing password remains unchanged.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Delete:</span> A Data Source can be removed if it’s not in use.
                </li>
            </ul> */}
    </>);
}
