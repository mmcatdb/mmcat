import { api } from '@/api';
import { InfoBanner, Tooltip } from '@/components/common';
import { AccessPathDisplay } from '@/components/mapping/AccessPathDisplay';
import { MappingEditor } from '@/components/mapping/MappingEditor';
import { PageLayout } from '@/components/RootLayout';
import { Datasource } from '@/types/Datasource';
import { Mapping } from '@/types/mapping';
import { Category } from '@/types/schema';
import { useBannerState } from '@/types/utils/useBannerState';
import { Button } from '@heroui/react';
import { useMemo, useState } from 'react';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { type Params, useLoaderData } from 'react-router-dom';

export function MappingPage() {
    const { category, mapping, datasource } = useLoaderData() as MappingLoaderData;

    const { isVisible, dismissBanner, restoreBanner } = useBannerState('mapping-detail-page');
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

                <Tooltip content={isVisible ? 'Hide info' : 'Show info'}>
                    <button
                        onClick={isVisible ? dismissBanner : restoreBanner}
                        className='text-primary-500 hover:text-primary-700 transition'
                    >
                        <IoInformationCircleOutline className='size-6' />
                    </button>
                </Tooltip>
            </div>

            {isVisible && <MappingDetailInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

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

MappingPage.loader = mappingLoader;

export type MappingLoaderData = {
    category: Category;
    mapping: Mapping;
    datasource: Datasource;
};

async function mappingLoader({ params: { categoryId, mappingId } }: { params: Params<'categoryId' | 'mappingId'> }) {
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
}

type DatasourceDetailInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

function MappingDetailInfoBanner({ className, dismissBanner }: DatasourceDetailInfoBannerProps) {
    return (
        <InfoBanner className={className} dismissBanner={dismissBanner}>
            <h2 className='text-lg font-semibold mb-2'>Defining a Mapping</h2>

            {/* TODO */}
            {/* <ul className='mt-2 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Edit:</strong> You can update connection details, but the type cannot be changed.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Password:</strong> If edit password field left empty, the existing password remains unchanged.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Delete:</strong> A Data Source can be removed if it’s not in use.
                </li>
            </ul> */}
        </InfoBanner>
    );
}
