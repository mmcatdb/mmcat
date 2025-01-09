import { useEffect, useState } from 'react';
import { type Params, useLoaderData, useLocation, useNavigate, useParams } from 'react-router-dom';
import { api } from '@/api';
import { Datasource, type Settings } from '@/types/datasource';
import { ErrorPage, LoadingPage } from '@/pages/errorPages';
import { Button, Input, Spinner } from '@nextui-org/react';
import { Mapping } from '@/types/mapping';
import { MappingsTable } from '@/components/schema-categories/MappingsTable';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';
import { DatasourceSpecificFields } from '@/components/datasources/DatasourceModal';
import { usePreferences } from '@/components/PreferencesProvider';
import { cn } from '@/components/utils';

export function DatasourceDetailPage() {
    return <DatasourceDetail />;
}

export type DatasourceDetailLoaderData = {
    datasource: Datasource;
};

export async function datasourceDetailLoader({ params: { id } }: { params: Params<'id'> }): Promise<DatasourceDetailLoaderData> {
    if (!id) 
        throw new Error('Datasource ID is required');

    const response = await api.datasources.getDatasource({ id });
    if (!response.status) 
        throw new Error('Failed to load datasource info');
    

    return {
        datasource: Datasource.fromServer(response.data),
    };
}

export function DatasourceInCategoryDetailPage() {
    const { categoryId, id } = useParams<{ categoryId: string, id: string }>();
    const [ mappings, setMappings ] = useState<Mapping[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string>();

    useEffect(() => {
        if (!categoryId || !id) 
            return;

        const fetchMappings = async () => {
            setLoading(true);
            const response = await api.mappings.getAllMappingsInCategory({}, {
                categoryId,
                datasourceId: id,
            });
            setLoading(false);

            if (response.status)
                setMappings(response.data.map(Mapping.fromServer));
            else 
                setError('Failed to load data');
        };

        fetchMappings();
    }, [ categoryId, id ]);

    if (!categoryId || !id || error) 
        return <ErrorPage />;

    if (loading)
        return <LoadingPage />;

    function handleAddMapping() {
        toast.error('Add mapping functionality not implemented yet');
    }

    return (
        <div>
            <DatasourceDetail />
            <div className='mt-6'>
                <p className='text-xl pb-6'>Mappings Table</p>
                {loading ? (
                    <Spinner />
                ) : mappings.length > 0 ? (
                    <MappingsTable
                        mappings={mappings}
                        loading={loading}
                        error={error}
                    />
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

function DatasourceDetail() {
    const { datasource: initialDatasource } = useLoaderData() as DatasourceDetailLoaderData;
    const { theme } = usePreferences().preferences;

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
        setDatasource((prev) => ({ ...prev, label: newLabel } as Datasource));
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
            toast.error('Something went wrong when updating datsource');
        }
    }

    return (
        <div className='mt-5'>
            <h1 className={cn('text-2xl font-bold my-5', theme === 'dark' ? 'text-zinc-200' : 'text-zinc-800')}>
                {initialDatasource.label}
            </h1>
            <p className={cn('mb-5', theme === 'dark' ? 'text-zinc-200' : 'text-zinc-800')}>Type: {datasource.type}</p>

            {!isEditing ? (
                // View Mode
                <>
                    <pre className={cn('p-4 rounded-md text-sm',
                        theme === 'dark' ? 'bg-zinc-900 text-zinc-50' : 'bg-zinc-50 text-zinc-700',
                    )}>
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
                <div className={cn('p-6 rounded-lg border border-blue-200',
                    theme === 'dark' ? 'bg-zinc-900' : 'bg-zinc-50',
                )}>
                    <h2 className={cn('text-xl font-semibold mb-4',
                        theme === 'dark' ? 'text-blue-500' : 'text-blue-600',
                    )}>
                        Edit Datasource
                    </h2>
                    <Input
                        label='Datasource Label'
                        value={datasource.label}
                        onChange={(e) => handleLabelChange(e.target.value)}
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
                                color='success'
                                onClick={handleSaveChanges}
                                isLoading={isSaving}
                                className='px-6'
                            >
                                Save
                            </Button>
                            <Button color='danger' variant='light' onClick={cancelEditing} isDisabled={isSaving} className='px-6'>
                                Cancel
                            </Button>
                        </div>
                    </form>
                </div>
            )}

            <div className='pt-5'>
                <Button 
                    size='sm'
                    variant='bordered'
                    onPress={() => setisConfigurationShown((prev) => !prev)}
                    className={cn('border-blue-500',
                        theme === 'dark' ? 'text-blue-400' : 'text-blue-700',
                    )}
                >
                    {isConfigurationShown ? 'Hide Configuration' : 'Show Configuration'}
                </Button>
                {isConfigurationShown && (
                    <pre className={cn('mt-4 p-4 rounded-md text-sm',
                        theme === 'dark' ? 'bg-zinc-900 text-zinc-50' : 'bg-zinc-50 text-zinc-700',
                    )}>
                        {JSON.stringify(datasource?.configuration, null, 2)}
                    </pre>
                )}
            </div>
        </div>
    );
}
