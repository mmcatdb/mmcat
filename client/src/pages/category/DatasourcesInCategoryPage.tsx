import { useState } from 'react';
import { DatasourcesTable } from '@/components/datasources/DatasourcesTable';
import { DatasourceModal } from '@/components/datasources/DatasourceModal';
import { api } from '@/api';
import { Datasource } from '@/types/datasource';
import { toast } from 'react-toastify';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { EmptyState } from '@/components/TableCommon';
import { useLoaderData, type Params } from 'react-router-dom';
import { FaMagnifyingGlass, FaPlus } from 'react-icons/fa6';
import { RiMapPin2Line } from 'react-icons/ri';
import { Chip } from '@nextui-org/react';
import { HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';
import { cn } from '@/components/utils';

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
            <h1 className='text-xl font-bold mb-4'>Datasources in {category.label} (with mapping)</h1>

            <MappingInfoBanner className='mb-6' />

            <div className='mt-5'>
                {datasourcesIn.length > 0 ? (
                    <DatasourcesTable
                        datasources={datasourcesIn}
                        deleteDatasource={deleteDatasource}
                    />
                ) : (
                    <div className='text-center border border-default-300 p-6 rounded-lg bg-default-100'>
                        <p className='text-lg font-semibold text-default-900'>
                            No Datasources with Mappings Found 😿
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
                                <span className='font-bold'> Navigate to:</span> 
                                Your desired datasource
                            </div>
                            <div className='flex items-center gap-2'>
                                <FaPlus />
                                <span className='font-bold'> Click:</span> 
                                <span className='text-primary-600 font-medium'><Chip size='sm'>+ Add Mapping</Chip></span>
                            </div>
                        </div>
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

type MappingInfoBannerProps = {
    className?: string;
};

export function MappingInfoBanner({ className }: MappingInfoBannerProps) {
    // const { preferences, setPreferences } = usePreferences();
    // const [ isVisible, setIsVisible ] = useState(!preferences.dismissedMappingGuide);

    // function handleClose() {
    //     setIsVisible(false);
    //     setPreferences({ ...preferences, dismissedMappingGuide: true });
    // }

    // if (!isVisible) 
    //     return null;

    return (
        <div className={cn('relative bg-default-50 text-default-900 p-4 rounded-lg border border-default-300', className)}>
            <button 
                // onClick={handleClose} 
                className='absolute top-2 right-2 text-default-500 hover:text-default-700 transition'
            >
                <HiXMark className='w-5 h-5' />
            </button>

            <h2 className='text-lg font-semibold mb-4'>Understanding Mapping & Data Sources</h2>

            <p className='text-sm'>
                Before creating a <strong>Mapping</strong>, you need to connect a <strong>Data Source</strong>.  
                A Data Source represents where your data is stored, such as a database, or file.  
            </p>

            <p className='text-sm mt-2'>
                Once a Data Source is connected, you can create a <em>Mapping</em> on a <em>Schema Category</em>, linking the source to the <em>Conceptual Schema</em>.  
                A <strong>Mapping</strong> defines how data is structured and stored, using a <em>JSON-like access path</em> to describe relationships between objects.
            </p>

            <ul className='mt-3 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Mapping:</strong> Defines how conceptual schema elements relate to database structures.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Access Path:</strong> A tree structure that maps schema objects to database tables, or properties.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Data Source:</strong> The data source where mapped data is stored, defined by connection details.
                </li>
            </ul>

            <p className='text-sm mt-3'>
                Mappings ensure data is correctly structured and accessible for transformations. Choose an appropriate <em>data source</em> to connect to the Conceptual Schema.
            </p>
        </div>
    );
}
