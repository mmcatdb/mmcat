import { useState } from 'react';
import { type Params, useLoaderData, useLocation, useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { Datasource, type Settings } from '@/types/datasource';
import { Button, Input } from '@nextui-org/react';
import { Mapping } from '@/types/mapping';
import { MappingsTable } from '@/components/mapping/MappingsTable';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';
import { DatasourceSpecificFields } from '@/components/datasources/DatasourceModal';
import { cn } from '@/components/utils';
import { HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';

export function DatasourcePage() {
    return (<>
        <DatasourceDetailInfoBanner className='mb-6' />
        <DatasourceDisplay />
    </>);
}

DatasourcePage.loader = datasourceLoader;

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
    const { mappings } = useLoaderData() as DatasourceInCategoryLoaderData;

    function handleAddMapping() {
        toast.error('Add mapping functionality not implemented yet');
    }

    return (
        <div>
            <DatasourceDisplay />

            <div className='mt-6'>
                <p className='text-xl pb-6'>Mappings Table</p>
                {mappings.length > 0 ? (
                    <MappingsTable mappings={mappings} />
                ) : (
                    <EmptyState
                        message='This datasource does not have a mapping yet.'
                        buttonText='+ Add Mapping'
                        onButtonClick={handleAddMapping}
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
    const [ isConfigurationShown, setisConfigurationShown ] = useState(false);
    const [ isEditing, setIsEditing ] = useState(false);
    const [ isSaving, setIsSaving ] = useState(false);

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

    return (
        <div className='mt-5'>
            <h1 className='text-2xl font-bold my-5 text-default-800'>
                {initialDatasource.label}
            </h1>
            <p className='mb-5 text-default-800'>Type: {datasource.type}</p>

            {!isEditing ? (
                // View Mode
                <>
                    <pre className='p-4 rounded-lg text-sm bg-default-50 text-default-800'>
                        {JSON.stringify(datasource.settings, null, 2)}
                    </pre>
                    <Button
                        onClick={() => setIsEditing(true)} // start editing
                        className='mt-5'
                        color='primary'
                    >
                        Edit
                    </Button>
                </>
            ) : (
                // Edit Mode
                <div className='p-6 rounded-lg border border-blue-200 bg-default-50'>
                    <h2 className='text-xl font-semibold mb-4 text-primary-500'>
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
                            <Button color='primary' variant='ghost' onClick={cancelEditing} isDisabled={isSaving} className='px-6'>
                                Cancel
                            </Button>
                        </div>
                    </form>
                </div>
            )}

            <div className='pt-5'>
                <Button
                    size='sm'
                    variant='solid'
                    onPress={() => setisConfigurationShown(prev => !prev)}
                >
                    {isConfigurationShown ? 'Hide Configuration' : 'Show Configuration'}
                </Button>
                {isConfigurationShown && (
                    <pre className='mt-4 p-4 rounded-md text-sm bg-default-50 text-default-600'>
                        {JSON.stringify(datasource?.configuration, null, 2)}
                    </pre>
                )}
            </div>
        </div>
    );
}

type DatasourceDetailInfoBannerProps = {
    className?: string;
};

export function DatasourceDetailInfoBanner({ className }: DatasourceDetailInfoBannerProps) {
    // const { preferences, setPreferences } = usePreferences();
    // const [ isVisible, setIsVisible ] = useState(!preferences.dismissedDatasourceDetailGuide);

    // function handleClose() {
    //     setIsVisible(false);
    //     setPreferences({ ...preferences, dismissedDatasourceDetailGuide: true });
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

            <h2 className='text-lg font-semibold mb-2'>Managing a Data Source</h2>
            <ul className='mt-2 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Edit:</strong> You can update connection details, but the type cannot be changed.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Password:</strong> If left empty, the existing password remains unchanged.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Delete:</strong> A Data Source can be removed if it’s not in use.
                </li>
            </ul>
        </div>
    );
}