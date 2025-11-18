import { useCallback, useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { CreateDatasourceModal } from '@/components/datasources/CreateDatasourceModal';
import { api } from '@/api';
import { Datasource } from '@/types/Datasource';
import { EmptyState } from '@/components/TableCommon';
import { useLoaderData, type Params } from 'react-router-dom';
import { FaMagnifyingGlass, FaPlus } from 'react-icons/fa6';
import { RiMapPin2Line } from 'react-icons/ri';
import { Button, Tooltip } from '@heroui/react';
import { GoDotFill } from 'react-icons/go';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { useBannerState } from '@/types/utils/useBannerState';
import { InfoBanner } from '@/components/common';
import { PageLayout } from '@/components/RootLayout';
import { type Id } from '@/types/id';

export function DatasourcesInCategoryPage() {
    const data = useLoaderData() as DatasourcesInCategoryLoaderData;
    const [ datasourcesIn, setDatasourcesIn ] = useState<Datasource[]>(data.datasourcesIn);
    const [ datasourcesNotIn, setDatasourcesNotIn ] = useState<Datasource[]>(data.datasourcesNotIn);
    const { isVisible, dismissBanner, restoreBanner } = useBannerState('datasources-in-category-page');

    const [ isModalOpen, setIsModalOpen ] = useState(false);

    const [ datasourcesWithMappingsIds ] = useState<Id[]>(data.allDatasourcesWithMappingsIds);

    function onDatasourceCreated(newDatasource: Datasource) {
        setDatasourcesNotIn(prev => [ ...prev, newDatasource ]);
    }

    const onDelete = useCallback((id: Id) => {
        setDatasourcesIn(prev => prev.filter(ds => ds.id !== id));
        setDatasourcesNotIn(prev => prev.filter(ds => ds.id !== id));
    }, []);

    return (
        <PageLayout>
            <div className='flex items-center gap-2 mb-4'>
                <h1 className='text-xl font-semibold'>Datasources with mappings</h1>
                <Tooltip content={isVisible ? 'Hide info' : 'Show info'}>
                    <button
                        onClick={isVisible ? dismissBanner : restoreBanner}
                        className='text-primary-500 hover:text-primary-700 transition'
                    >
                        <IoInformationCircleOutline className='size-6' />
                    </button>
                </Tooltip>
            </div>

            {isVisible && <MappingInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

            <div className='mt-5'>
                {datasourcesIn.length > 0 ? (
                    <DatasourcesTable
                        datasources={datasourcesIn}
                        onDelete={onDelete}
                        datasourcesWithMappingsIds={datasourcesWithMappingsIds}
                    />
                ) : (
                    <div className='text-center border-2 border-dashed border-default-300 p-6 rounded-lg'>
                        <p className='text-lg font-semibold text-default-900'>
                            No Datasources with Mappings Found
                        </p>
                        <p className='text-default-500 mt-2'>
                            To add a mapping, follow these quick steps:
                        </p>

                        <div className='mt-4 text-default-600 text-sm space-y-2 flex flex-col items-center'>
                            <div className='flex items-center gap-2'>
                                <FaMagnifyingGlass />
                                <span className='font-bold'> Check:</span>
                                <em>Other Datasources Table</em>
                            </div>
                            <div className='flex items-center gap-2'>
                                <RiMapPin2Line />
                                <span className='font-bold'> Select:</span>
                                Your desired datasource
                            </div>
                            <div className='flex items-center gap-2'>
                                <FaPlus />
                                <span className='font-bold'> Click on:</span>
                                <span className='text-default-600 font-medium'>+ Add Mapping</span>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className='flex items-center justify-between mt-10'>
                <h1 className='text-xl font-bold'>Other Datasources</h1>
                <Button
                    onPress={() => setIsModalOpen(true)}
                    color='primary'
                    variant='flat'
                    startContent={<FaPlus className='size-3' />}
                >
                    Add Datasource
                </Button>
            </div>

            <div className='mt-5'>
                {datasourcesNotIn.length > 0 ? (
                    <DatasourcesTable
                        datasources={datasourcesNotIn}
                        onDelete={onDelete}
                        datasourcesWithMappingsIds={datasourcesWithMappingsIds}
                    />
                ) : (
                    <EmptyState
                        message='No other datasources available.'
                        buttonText='+ Add Datasource'
                        onClick={() => setIsModalOpen(true)}
                    />
                )}
            </div>

            <CreateDatasourceModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onDatasourceCreated={onDatasourceCreated}
            />
        </PageLayout>
    );
}

export type DatasourcesInCategoryLoaderData = {
    datasourcesIn: Datasource[];
    datasourcesNotIn: Datasource[];
    allDatasourcesWithMappingsIds: string[];  // IDs of all datasources with mappings in any category
};

DatasourcesInCategoryPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<DatasourcesInCategoryLoaderData> => {
    if (!categoryId)
        throw new Error('Action ID is required');

    const [ inCategoryResponse, allResponse, allMappingsResponse ] = await Promise.all([
        api.datasources.getAllDatasources({}, { categoryId }),
        api.datasources.getAllDatasources({}),
        api.mappings.getAllMappings({}), // Get ALL mappings to check for datasources used elsewhere
    ]);

    if (!inCategoryResponse.status || !allResponse.status || !allMappingsResponse.status)
        throw new Error('Failed to load datasources in category');

    const datasourcesIn = inCategoryResponse.data.map(Datasource.fromResponse);
    const datasourcesNotIn = allResponse.data
        .filter(ds => !datasourcesIn.some(inCategory => inCategory.id === ds.id))
        .map(Datasource.fromResponse);

    return {
        datasourcesIn,
        datasourcesNotIn,
        allDatasourcesWithMappingsIds: [ ...new Set(allMappingsResponse.data.map(m => m.datasourceId)) ],
    };
};

type MappingInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

export function MappingInfoBanner({ className, dismissBanner }: MappingInfoBannerProps) {
    return (
        <InfoBanner className={className} dismissBanner={dismissBanner}>
            <h2 className='text-lg font-semibold mb-4'>Understanding Mapping & Data Sources</h2>

            <p className='text-sm'>
                Before creating a <span className='font-bold'>Mapping</span>, you need to connect a <span className='font-bold'>Data Source</span>.
                A Data Source represents where your data is stored, such as a database, or file.
            </p>

            <p className='text-sm mt-2'>
                Once a Data Source is connected, you can create a <em>Mapping</em> on a <em>Schema Category</em>, linking the source to the <em>Conceptual Schema</em>.
                A <span className='font-bold'>Mapping</span> defines how data is structured and stored, using a <em>JSON-like access path</em> to describe relationships between objects.
            </p>

            <ul className='mt-3 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Mapping:</span> Defines how conceptual schema elements relate to database structures.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Access Path:</span> A tree structure that maps schema objects to database tables, or properties.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span className='font-bold'>Data Source:</span> The data source where mapped data is stored, defined by connection details.
                </li>
            </ul>

            <p className='text-sm mt-3'>
                Mappings ensure data is correctly structured and accessible for transformations. Choose an appropriate <em>data source</em> to connect to the Conceptual Schema.
            </p>
        </InfoBanner>
    );
}
