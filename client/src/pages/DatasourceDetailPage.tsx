import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '@/api';
import type { Datasource, Settings } from '@/types/datasource';
import { ErrorPage } from '@/pages/errorPages';
import { Button, Checkbox, Input, Spinner } from '@nextui-org/react';
import { Mapping } from '@/types/mapping';
import { MappingsTable } from '@/components/schema-categories/MappingsTable';
import { toast } from 'react-toastify';
import { EmptyState } from '@/components/TableCommon';

type DatasourceDetailProps = {
    datasourceId: string;
}

export const DatasourceDetailPage = () => {
    const { id } = useParams<{ id: string }>();

    if (!id) 
        return <ErrorPage />;

    return <DatasourceDetail datasourceId={id} />;
};

export const DatasourceInCategoryDetailPage = () => {
    const { categoryId, id } = useParams<{ categoryId: string, id: string }>();
    const [ mappings, setMappings ] = useState<Mapping[]>([]);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        if (!categoryId || !id) 
            return;

        const fetchMappings = async () => {
            try {
                setLoading(true);
                const response = await api.mappings.getAllMappingsInCategory({}, {
                    categoryId,
                    datasourceId: id,
                });
                if (response.status && response.data)
                    setMappings(response.data.map(Mapping.fromServer));
                else
                    setError('Failed to load data');
            }
            catch (e) {
                setError('Failed to load mappings.');
            }
            finally {
                setLoading(false);
            }
        };

        fetchMappings();
    }, [ categoryId, id ]);

    if (!categoryId || !id) 
        return <ErrorPage />;

    const handleAddMapping = () => {
        toast.error('Add mapping functionality not implemented yet');
    };

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
};

export const DatasourceDetail = ({ datasourceId }: DatasourceDetailProps) => {
    const [ datasource, setDatasource ] = useState<Datasource | null>(null);
    const [ loading, setLoading ] = useState(true);
    const [ error, setError ] = useState<string | null>(null);
    const [ showConfiguration, setShowConfiguration ] = useState(false);

    // for edit mode
    const [ isEditing, setIsEditing ] = useState(false);
    const [ formValues, setFormValues ] = useState<Settings | null>(null);

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

    const handleInputChange = (field: keyof Settings, value: string | boolean | undefined) => {
        if (!formValues) 
            return;

        setFormValues({ ...formValues, [field]: value });
    };

    const handleSaveChanges = async () => {
        if (!formValues) 
            return;

        try {
            const updatedDatasource = await api.datasources.updateDatasource(
                { id: datasourceId },
                { label: datasource?.label ?? '', settings: formValues },
            );
            
            setDatasource(updatedDatasource.data);
            toast.success('Datasource updated successfully!');
        }
        catch (e) {
            toast.error('Failed to update datasource.');
        }
        finally {
            setIsEditing(false);
        }
    };

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
                        <h1 className='heading-main my-5'>{datasource?.label}</h1>
                        <p className='mb-5'>Type: {datasource?.type}</p>

                        {!isEditing ? (
                            // View Mode
                            <>
                                <pre>{JSON.stringify(datasource?.settings, null, 2)}</pre>
                                <Button
                                    onClick={() => setIsEditing(true)}
                                    className='mt-5'
                                >
                                    Edit
                                </Button>
                            </>
                        ) : (
                            // Edit Mode
                            <form className='grid grid-cols-1 gap-4'>
                                <Input
                                    label='Host'
                                    value={formValues?.host ?? ''}
                                    onChange={(e) =>
                                        handleInputChange('host', e.target.value)
                                    }
                                />
                                <Input
                                    label='Port'
                                    value={formValues?.port?.toString() ?? ''}
                                    type='number'
                                    onChange={(e) =>
                                        handleInputChange('port', e.target.value)
                                    }
                                />
                                <Input
                                    label='Database'
                                    value={formValues?.database ?? ''}
                                    onChange={(e) =>
                                        handleInputChange('database', e.target.value)
                                    }
                                />
                                <Input
                                    label='Username'
                                    value={formValues?.username ?? ''}
                                    onChange={(e) =>
                                        handleInputChange('username', e.target.value)
                                    }
                                />
                                <Input
                                    label='Password'
                                    placeholder='Enter new password'
                                    type='password'
                                    onChange={(e) =>
                                        handleInputChange('password', e.target.value)
                                    }
                                />
                                <Input
                                    label='Authentication Database'
                                    value={formValues?.authenticationDatabase ?? ''}
                                    onChange={(e) =>
                                        handleInputChange(
                                            'authenticationDatabase',
                                            e.target.value,
                                        )
                                    }
                                />
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
                                <div className='flex gap-4 mt-4'>
                                    <Button
                                        color='primary'
                                        onClick={handleSaveChanges}
                                    >
                                        Save
                                    </Button>
                                    {/* // TODO: cancel confirmation modal if some changes */}
                                    <Button
                                        color='danger'
                                        variant='light'
                                        onClick={() => setIsEditing(false)}
                                    >
                                        Cancel
                                    </Button>
                                </div>
                            </form>
                        )}

                        <div className='pt-5'>
                            <Button 
                                size='sm'
                                variant='bordered'
                                onPress={() => setShowConfiguration((prev) => !prev)}
                            >
                                {showConfiguration ? 'Hide Configuration' : 'Show Configuration'}
                            </Button>
        
                            {showConfiguration && (
                                <pre className='text-zinc-400 mt-4'>
                                    {JSON.stringify(datasource?.configuration, null, 2)}
                                </pre>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};
