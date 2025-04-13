import { useState } from 'react';
import { type Params, useLoaderData, useLocation, useNavigate, useParams } from 'react-router-dom';
import { api } from '@/api';
import { Datasource, type Settings } from '@/types/datasource';
import { Button, Input, Tooltip } from '@nextui-org/react';
import { Mapping } from '@/types/mapping';
import { MappingsTable } from '@/components/mapping/MappingsTable';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';
import { DatasourceSpecificFields } from '@/components/datasources/DatasourceModal';
import { cn } from '@/components/utils';
import { HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';
import { useBannerState } from '@/types/utils/useBannerState';
import { IoInformationCircleOutline } from 'react-icons/io5';

export function DatasourceDetailPage() {
    return (<>
        <DatasourceDisplay />
    </>);
}

DatasourceDetailPage.loader = datasourceLoader;

export type DatasourceLoaderData = {
    datasource: Datasource;
};

async function datasourceLoader({ params: { id } }: { params: Params<'id'> }): Promise<DatasourceLoaderData> {
    if (!id)
        throw new Error('Datasource ID is required');

    const response = await api.datasources.getDatasource({ id });
    if (!response.status)
        throw new Error('Failed to load datasource info');

    return {
        datasource: Datasource.fromServer(response.data),
    };
}

export function DatasourceInCategoryPage() {
    const { datasource, mappings } = useLoaderData() as DatasourceInCategoryLoaderData;
    const { categoryId } = useParams<{ categoryId: string }>();
    const navigate = useNavigate();

    function handleCreateMapping() {
        if (!categoryId) {
            console.error('Category ID is missing');
            return;
        }
        navigate(`/schema-categories/${categoryId}/mappings/new`, {
            state: { datasourceId: datasource.id },   // pass datasource ID for new mapping creation
        });
    }

    return (
        <div>
            <DatasourceDisplay />

            <div className='mt-6'>
                <div className='flex justify-between items-center pb-6'>
                    <p className='text-xl'>Mappings Table</p>
                    <Button
                        color='primary'
                        onPress={handleCreateMapping}
                        size='sm'
                    >
                        + Add Mapping
                    </Button>
                </div>
                {mappings.length > 0 ? (
                    <MappingsTable mappings={mappings} />
                ) : (
                    <EmptyState
                        message='This datasource does not have a mapping yet.'
                        buttonText='+ Add Mapping'
                        onButtonClick={handleCreateMapping}
                    />
                )}
            </div>
        </div>
    );
}

DatasourceInCategoryPage.loader = datasourceInCategoryLoader;

export type DatasourceInCategoryLoaderData = {
    datasource: Datasource;
    mappings: Mapping[];
};

async function datasourceInCategoryLoader({ params: { categoryId, id } }: { params: Params<'categoryId' | 'id'> }): Promise<DatasourceInCategoryLoaderData> {
    if (!categoryId || !id)
        throw new Error('Datasource ID is required');

    const [ datasourceResponse, mappingsResponse ] = await Promise.all([
        api.datasources.getDatasource({ id }),
        api.mappings.getAllMappingsInCategory({}, { categoryId: categoryId, datasourceId: id }),
    ]);
    if (!datasourceResponse.status || !mappingsResponse.status)
        throw new Error('Failed to load datasource or mappings');

    return {
        datasource: Datasource.fromServer(datasourceResponse.data),
        mappings: mappingsResponse.data.map(Mapping.fromServer),
    };
}

function DatasourceDisplay() {
    const { datasource: initialDatasource } = useLoaderData() as DatasourceLoaderData | DatasourceInCategoryLoaderData;

    const [ datasource, setDatasource ] = useState<Datasource>(initialDatasource);
    const [ formValues, setFormValues ] = useState<Settings>(initialDatasource.settings);
    const [ isConfigurationShown, setIsConfigurationShown ] = useState(false);
    const [ isEditing, setIsEditing ] = useState(false);
    const [ isSaving, setIsSaving ] = useState(false);
    const { isVisible, dismissBanner, restoreBanner } = useBannerState('datasource-detail-page');

    const navigate = useNavigate();
    const location = useLocation();

    function handleInputChange(field: keyof Settings, value: unknown) {
        if (!formValues)
            return;

        setFormValues({ ...formValues, [field]: value });
    }

    function handleLabelChange(newLabel: string) {
        setDatasource(prev => ({ ...prev, label: newLabel } as Datasource));
    }

    function cancelEditing() {
        if (initialDatasource) {
            // revert to initial values
            setFormValues(initialDatasource.settings);
            setDatasource(initialDatasource);
        }
        setIsEditing(false);
    }

    async function handleSaveChanges() {
        if (!formValues)
            return;

        setIsSaving(true);
        const updatedDatasource = await api.datasources.updateDatasource(
            { id: initialDatasource.id },
            { label: datasource.label, settings: formValues },
        );
        setIsSaving(false);
        setIsEditing(false);

        if (updatedDatasource.status) {
            setDatasource(updatedDatasource.data);
            toast.success('Datasource updated successfully!');
            // navigate only if label has changed
            if (datasource.label !== initialDatasource.label)
                navigate(location.pathname);
        }
        else {
            toast.error('Something went wrong when updating datasource');
        }
    }

    const renderSettingsView = (settings: Settings) => {
        return (
            <div className='space-y-4'>
                {Object.entries(settings).map(([ key, value ]) => (
                    <div key={key} className='flex gap-4'>
                        <span className='w-1/3 text-sm font-medium text-default-500 capitalize'>
                            {key.replace(/([A-Z])/g, ' $1').trim()}
                        </span>
                        <span className='flex-1 text-default-800 break-all'>
                            {typeof value === 'object' 
                                ? JSON.stringify(value) 
                                : key.toLowerCase().includes('password')
                                    ? '••••••••'
                                    : String(value)}
                        </span>
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className='mt-5'>
            <div className='flex items-center gap-2 mb-4'>
                <h1 className='text-2xl font-bold text-default-800'>{initialDatasource.label}</h1>
                <Tooltip content={isVisible ? 'Hide info' : 'Show info'}>
                    <button
                        onClick={isVisible ? dismissBanner : restoreBanner}
                        className='text-primary-500 hover:text-primary-700 transition'
                    >
                        <IoInformationCircleOutline className='w-6 h-6' />
                    </button>
                </Tooltip>
            </div>

            {isVisible && <DatasourceDetailInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

            <div className='mb-6 p-4 bg-default-50 rounded-lg'>
                <div className='flex gap-8'>
                    <div>
                        <p className='text-sm font-medium text-default-500'>Type</p>
                        <p className='text-default-800'>{datasource.type}</p>
                    </div>
                    <div>
                        <p className='text-sm font-medium text-default-500'>ID</p>
                        <p className='text-default-800'>{datasource.id}</p>
                    </div>
                </div>
            </div>

            {!isEditing ? (
                // View Mode
                <>
                    <div className='mb-6'>
                        <div className='flex justify-between items-center mb-3'>
                            <h2 className='text-lg font-semibold'>Connection Settings</h2>
                            <Button
                                onClick={() => setIsEditing(true)}
                                color='primary'
                                size='sm'
                            >
                                Edit Settings
                            </Button>
                        </div>
                        <div className='p-4 rounded-lg bg-default-50'>
                            {renderSettingsView(datasource.settings)}
                        </div>
                    </div>
                </>
            ) : (
                // Edit Mode
                <div className='p-6 rounded-lg border border-blue-200 bg-default-50 mb-6'>
                    <h2 className='text-xl font-semibold mb-4'>
                        Edit Datasource
                    </h2>
                    <Input
                        label='Datasource Label'
                        value={datasource.label}
                        onChange={e => handleLabelChange(e.target.value)}
                        className='mb-5'
                    />
                    <form className='grid grid-cols-1 gap-4'>
                        <DatasourceSpecificFields
                            datasourceType={datasource.type}
                            settings={formValues || {}}
                            handleSettingsChange={handleInputChange}
                        />
                        <div className='flex gap-2 mt-6'>
                            <Button
                                color='primary'
                                onClick={handleSaveChanges}
                                isLoading={isSaving}
                                className='px-6'
                            >
                                Save
                            </Button>
                            <Button variant='flat' onClick={cancelEditing} isDisabled={isSaving} className='px-6'>
                                Cancel
                            </Button>
                        </div>
                    </form>
                </div>
            )}

            <div>
                <Button
                    size='sm'
                    variant='solid'
                    onPress={() => setIsConfigurationShown(prev => !prev)}
                    className='mb-2'
                >
                    {isConfigurationShown ? 'Hide Configuration' : 'Show Configuration'}
                </Button>
                {isConfigurationShown && (
                    <div className='p-4 rounded-md bg-default-50'>
                        <pre className='text-sm text-default-600 overflow-x-auto'>
                            {JSON.stringify(datasource?.configuration, null, 2)}
                        </pre>
                    </div>
                )}
            </div>
        </div>
    );
}

type DatasourceDetailInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

export function DatasourceDetailInfoBanner({ className, dismissBanner }: DatasourceDetailInfoBannerProps) {
    return (
        <div className={cn('relative', className)}>
            <div className={cn('relative bg-default-50 text-default-900 p-4 rounded-lg border border-default-300')}>
                <button 
                    onClick={dismissBanner} 
                    className='absolute top-2 right-2 text-default-500 hover:text-default-700 transition'
                >
                    <HiXMark className='w-5 h-5' />
                </button>

                <h2 className='text-lg font-semibold mb-2'>Managing a Data Source</h2>
                <ul className='mt-2 text-sm space-y-2'>
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
                </ul>
            </div>
        </div>
    );
}