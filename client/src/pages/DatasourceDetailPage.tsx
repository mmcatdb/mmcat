import { useEffect, useState } from 'react';
import { type Params, useParams } from 'react-router-dom';
import { api } from '@/api';
import { Datasource, type Settings } from '@/types/datasource';
import { ErrorPage, LoadingPage } from '@/pages/errorPages';
import { Button, Checkbox, Input, Spinner } from '@nextui-org/react';
import { Mapping } from '@/types/mapping';
import { MappingsTable } from '@/components/schema-categories/MappingsTable';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';

type DatasourceDetailProps = {
    datasourceId: string;
}

export function DatasourceDetailPage() {
    const { id } = useParams<{ id: string }>();

    if (!id) 
        return <ErrorPage />;

    return <DatasourceDetail datasourceId={id} />;
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

    if (!categoryId || !id) 
        return <ErrorPage />;

    if (loading)
        return <LoadingPage />;

    function handleAddMapping() {
        toast.error('Add mapping functionality not implemented yet');
    }

    return (
        <div>
            <DatasourceDetail datasourceId={id} />
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

function DatasourceDetail({ datasourceId }: DatasourceDetailProps) {
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ loading, setLoading ] = useState(true);
    const [ error, setError ] = useState<string>();
    const [ showConfiguration, setShowConfiguration ] = useState(false);

    // for edit mode
    const [ isEditing, setIsEditing ] = useState(false);
    const [ formValues, setFormValues ] = useState<Settings>();
    const [ isSaving, setIsSaving ] = useState(false);
    const [ originalValues, setOriginalValues ] = useState<Settings>(); // store for reverting (if editing canceled)

    useEffect(() => {
        const fetchDatasource = async () => {
            try {
                setLoading(true);
                const response = await api.datasources.getDatasource({ id: datasourceId });

                if (response.status && response.data) {
                    setDatasource(response.data);
                    setFormValues(response.data.settings);
                }
                else {
                    setError('Failed to load datasource details.');
                }
            }
            catch {
                setError('An error occurred while loading the datasource details.');
            }
            finally {
                setLoading(false);
            }
        };

        fetchDatasource();
    }, [ datasourceId ]);

    function handleInputChange(field: keyof Settings, value: string | boolean | undefined) {
        if (!formValues) 
            return;

        setFormValues({ ...formValues, [field]: value });
    }

    function handleLabelChange(newLabel: string) {
        setDatasource((prev) => ({ ...prev, label: newLabel } as Datasource));
    }

    function startEditing() {
        if (datasource && formValues) 
            setOriginalValues({ ...formValues }); // store original values for revert
        setIsEditing(true);
    }

    function cancelEditing() {
        if (originalValues) 
            setFormValues(originalValues); // revert to original values
        setIsEditing(false);
    }

    async function handleSaveChanges() {
        if (!formValues) 
            return;

        try {
            setIsSaving(true);
            const updatedDatasource = await api.datasources.updateDatasource(
                { id: datasourceId },
                { label: datasource?.label ?? '', settings: formValues },
            );

            if (updatedDatasource.status) {
                setDatasource(updatedDatasource.data);
                toast.success('Datasource updated successfully!');
            }
            else {
                toast.error('Something went wrong when updating datsource');
            }
        }
        catch (e) {
            toast.error('Failed to update datasource.');
        }
        finally {
            setIsSaving(false);
            setIsEditing(false);
        }
    }

    function renderEditFields() {
        if (!formValues || !datasource) 
            return null;

        const { type } = datasource;

        if ([ 'mongodb', 'postgresql', 'neo4j' ].includes(type)) {
            return (
                <>
                    <Input
                        label='Host'
                        value={formValues.host ?? ''}
                        onChange={(e) => handleInputChange('host', e.target.value)}
                    />
                    <Input
                        label='Port'
                        value={formValues.port != null ? String(formValues.port) : ''}
                        type='number'
                        onChange={(e) => handleInputChange('port', e.target.value)}
                    />
                    <Input
                        label='Database'
                        value={formValues.database ?? ''}
                        onChange={(e) => handleInputChange('database', e.target.value)}
                    />
                    <Input
                        label='Username'
                        value={formValues.username ?? ''}
                        onChange={(e) => handleInputChange('username', e.target.value)}
                    />
                    <Input
                        label='Password'
                        placeholder='Enter new password'
                        type='password'
                        onChange={(e) => handleInputChange('password', e.target.value)}
                    />
                    {[ 'mongodb' ].includes(type) && (
                        <Input
                            label='Authentication Database'
                            value={formValues.authenticationDatabase ?? ''}
                            onChange={(e) => handleInputChange('authenticationDatabase', e.target.value)}
                        />
                    )}
                </>
            );
        }

        if ([ 'json', 'jsonld' ].includes(type)) {
            return (
                <Input
                    label='File URL'
                    value={formValues.url ?? ''}
                    onChange={(e) => handleInputChange('url', e.target.value)}
                    required
                />
            );
        }

        if ([ 'csv' ].includes(type)) {
            return (
                <>
                    <Input
                        label='File URL'
                        value={formValues.url ?? ''}
                        onChange={(e) => handleInputChange('url', e.target.value)}
                        required
                    />
                    <Checkbox
                        isSelected={formValues?.hasHeader ?? false}
                        onChange={(e) =>
                            handleInputChange('hasHeader', e.target.checked)
                        }
                    >
                        Has header
                    </Checkbox>
                </>
            );
        }

        return null;
    }

    if (error ?? (!datasource && !loading)) 
        return <ErrorPage />;

    return (
        <div>
            <div className='mt-5'>
                {loading ? (
                    <div>
                        <Spinner />
                    </div>
                ) : (
                    <div>
                        {!isEditing ? (
                            // View Mode
                            <>
                                <h1 className='text-2xl font-bold text-zinc-800 my-5 dark:text-zinc-200'>
                                    {datasource?.label}
                                </h1>
                                <p className='text-zinc-800 mb-5 dark:text-zinc-200'>Type: {datasource?.type}</p>
                                <pre className='p-4 bg-zinc-50 rounded-md text-sm text-zinc-700 dark:bg-zinc-900 dark:text-zinc-50'>
                                    {JSON.stringify(datasource?.settings, null, 2)}
                                </pre>
                                <Button
                                    onClick={startEditing}
                                    className='mt-5'
                                    color='primary'
                                >
                                    Edit
                                </Button>
                            </>
                        ) : (
                            // Edit Mode
                            <div className='bg-zinc-50 p-6 rounded-lg border border-blue-200 dark:bg-zinc-900'>
                                <h2 className='text-xl font-semibold text-blue-700 mb-4 dark:text-blue-500'>
                                    Edit Datasource
                                </h2>
                                <Input
                                    label='Datasource Label'
                                    value={datasource?.label ?? ''}
                                    onChange={(e) => handleLabelChange(e.target.value)}
                                    className='mb-5'
                                />
                                <p className='text-zinc-800 mb-5 dark:text-zinc-50'>Type: {datasource?.type}</p>
                                <form className='grid grid-cols-1 gap-4'>
                                    {renderEditFields()}
                                    <Checkbox
                                        isSelected={formValues?.isWritable}
                                        onChange={(e) => handleInputChange('isWritable', e.target.checked)}
                                    >
                                        Is Writable?
                                    </Checkbox>
                                    <Checkbox
                                        isSelected={formValues?.isQueryable}
                                        onChange={(e) =>
                                            handleInputChange('isQueryable', e.target.checked)
                                        }
                                    >
                                        Is Queryable?
                                    </Checkbox>
                                    <div className='flex gap-2 mt-6'>
                                        <Button
                                            color='success'
                                            onClick={handleSaveChanges}
                                            isLoading={isSaving}
                                            className='px-6'
                                        >
                                            Save
                                        </Button>
                                        <Button
                                            color='danger'
                                            variant='light'
                                            onClick={cancelEditing}
                                            isDisabled={isSaving}
                                            className='px-6'
                                        >
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
                                onPress={() => setShowConfiguration((prev) => !prev)}
                                className='text-blue-700 border-blue-500 dark:text-blue-400'
                            >
                                {showConfiguration ? 'Hide Configuration' : 'Show Configuration'}
                            </Button>
                            {showConfiguration && (
                                <pre className='bg-zinc-50 text-zinc-700 mt-4 p-4 rounded-md text-sm dark:bg-zinc-900 dark:text-zinc-50'>
                                    {JSON.stringify(datasource?.configuration, null, 2)}
                                </pre>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
