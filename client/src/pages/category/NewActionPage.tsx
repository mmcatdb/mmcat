import { Button, Input, Select, SelectItem } from '@heroui/react';
import { type FC, useEffect, useState } from 'react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { ErrorPage } from '@/pages/errorPages';
import { type ActionInit, ActionType, type JobPayloadInit, ACTION_TYPES, type RSDToCategoryPayloadInit, type ModelToCategoryPayloadInit, type CategoryToModelPayloadInit } from '@/types/action';
import { Datasource } from '@/types/Datasource';
import { TrashIcon } from '@heroicons/react/24/outline';
import { type LogicalModel, logicalModelsFromResponse } from '@/types/mapping';
import { PageLayout } from '@/components/RootLayout';

export function NewActionPage() {
    const [ label, setLabel ] = useState('');
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState(false);
    const { category } = useCategoryInfo();
    const navigate = useNavigate();
    const [ type, setType ] = useState(ActionType.ModelToCategory); // Default preselect
    const [ steps, setSteps ] = useState<JobPayloadInit[]>([ getDefaultStep(type) ]);

    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ logicalModels, setLogicalModels ] = useState<LogicalModel[]>([]);

    useEffect(() => {
        async function fetchDatasourcesAndMappings() {
            const datasourcesResponse = await api.datasources.getAllDatasources({}, { categoryId: category.id });
            const mappingsResponse = await api.mappings.getAllMappingsInCategory({}, { categoryId: category.id });

            if (!datasourcesResponse.status ||  !mappingsResponse.status) {
                toast.error('Error fetching data from server.');
                setError(true);
                return;
            }

            setLogicalModels(logicalModelsFromResponse(datasourcesResponse.data, mappingsResponse.data));
            setDatasources(datasourcesResponse.data.map(Datasource.fromResponse));
        }

        void fetchDatasourcesAndMappings();
    }, [ category.id ]);

    function selectType(type: ActionType) {
        setType(type);
        // Reset steps when type changes.
        setSteps([ getDefaultStep(type) ]);
    }

    function addStep() {
        if (!type) {
            toast.error('Select an action type before adding steps.');
            return;
        }

        // Add a new step based on the current type
        if (type === ActionType.ModelToCategory || type === ActionType.CategoryToModel)
            setSteps(prevSteps => [ ...prevSteps, { type, datasourceId: '', mappingIds: [] } ]);
        else if (type === ActionType.RSDToCategory)
            setSteps(prevSteps => [ ...prevSteps, { type, datasourceIds: [] } ]);
    }

    function removeStep(index: number) {
        if (steps.length <= 1) {
            toast.error('At least one step is required.');
            return;
        }
        setSteps(prevSteps => prevSteps.filter((_, i) => i !== index));
    }

    async function handleSubmit() {
        // Validate required fields
        if (!label || !type || steps.length === 0) {
            toast.error('All fields and at least one step are required.');
            return;
        }

        // Validate that each step has at least one datasource selected
        const hasInvalidSteps = steps.some(step => {
            if (step.type === ActionType.ModelToCategory || step.type === ActionType.CategoryToModel)
                return !step.datasourceId;
            else if (step.type === ActionType.RSDToCategory)
                return step.datasourceIds.length === 0;

            return true;
        });

        if (hasInvalidSteps) {
            toast.error('Each step must have at least one datasource selected.');
            return;
        }

        setLoading(true);
        setError(false);

        try {
            const newAction: ActionInit = {
                categoryId: category.id,
                label,
                payloads: steps,
            };
            const response = await api.actions.createAction({}, newAction);
            if (!response.status)
                throw new Error('Failed to create action');

            toast.success('Action created successfully.');
            navigate(-1);
        }
        catch (err) {
            toast.error('Something went wrong when creating an action.');
            setError(true);
        }
        finally {
            setLoading(false);
        }
    }

    if (error)
        return <ErrorPage />;

    return (
        <PageLayout>
            <h1 className='text-xl font-semibold mb-4'>Add Action</h1>

            <div className='mb-4'>
                <Input
                    label='Label'
                    value={label}
                    onChange={e => setLabel(e.target.value)}
                    placeholder='Enter action label'
                />
            </div>

            <div className='mb-4'>
                <SelectActionType actionType={type} setActionType={selectType} />
            </div>

            <h2 className='text-lg font-semibold mb-2'>Steps</h2>

            <div className='mb-4'>
                {steps.map((step, index) => {
                    const Component = getStepForm(step.type);

                    return (
                        <div key={index} className='p-2 border rounded-lg flex items-center gap-2 border-default-300'>
                            <Component
                                step={step}
                                datasources={datasources}
                                logicalModels={logicalModels}
                                updateStep={updatedStep => setSteps(prev => prev.map((s, i) => (i === index ? updatedStep : s)))}
                            />

                            <Button
                                isIconOnly
                                aria-label='Delete step'
                                color='danger'
                                variant='light'
                                onPress={() => removeStep(index)}
                                isDisabled={steps.length === 1}
                                className='p-1'
                            >
                                <TrashIcon className='size-5' />
                            </Button>
                        </div>
                    );
                })}
            </div>

            <Button onPress={addStep} className='mb-4'>
                Add Step
            </Button>

            <div className='flex justify-end'>
                <Button
                    onPress={() => navigate(-1)}
                    isDisabled={loading}
                    className='mr-2'
                >
                    Cancel
                </Button>
                <Button
                    color='primary'
                    onPress={() => {
                        void handleSubmit();
                    }}
                    isLoading={loading}
                >
                    Submit
                </Button>
            </div>
        </PageLayout>
    );
}

type SelectActionTypeProps = {
    actionType: ActionType;
    setActionType: (type: ActionType) => void;
};

function SelectActionType({ actionType, setActionType }: SelectActionTypeProps) {
    return (
        <Select
            items={ACTION_TYPES}
            label='Action Type'
            placeholder='Select an Action Type'
            selectedKeys={actionType ? new Set([ actionType ]) : new Set()}
            onSelectionChange={keys => {
                const type = (keys as Set<ActionType>).values().next().value;
                if (type)
                    setActionType(type);
            }}
        >
            {item => <SelectItem key={item.type}>{item.label}</SelectItem>}
        </Select>
    );
}

function getDefaultStep(type: ActionType): JobPayloadInit {
    switch (type) {
    case ActionType.ModelToCategory:
    case ActionType.CategoryToModel:
        return { type, datasourceId: '', mappingIds: [] };
    case ActionType.RSDToCategory:
        return { type, datasourceIds: [] };
    default:
        throw new Error(`Unsupported action type: ${type}`);
    }
}

type StepFormProps<TStep extends JobPayloadInit = JobPayloadInit> = {
    step: TStep;
    datasources: Datasource[];
    logicalModels: LogicalModel[];
    updateStep: (step: TStep) => void;
};

function getStepForm(type: ActionType) {
    switch (type) {
    case ActionType.ModelToCategory:
    case ActionType.CategoryToModel:
        return TransformationStepForm as FC<StepFormProps>;
    case ActionType.RSDToCategory:
        return InferenceStepForm as FC<StepFormProps>;
    default:
        throw new Error(`Unsupported action type: ${type}`);
    }
}

function TransformationStepForm({ step, datasources, logicalModels, updateStep }: StepFormProps<ModelToCategoryPayloadInit | CategoryToModelPayloadInit>) {
    const logicalModel = logicalModels.find(m => m.datasource.id === step.datasourceId);

    return (<>
        <Select
            label='Datasource'
            selectedKeys={step.datasourceId ? new Set([ step.datasourceId ]) : new Set()}
            placeholder='Select a datasource'
            onSelectionChange={keys => {
                const datasourceId = (keys as Set<string>).values().next().value;
                if (!datasourceId)
                    return;

                updateStep({ ...step, datasourceId, mappingIds: [] });
            }}
        >
            {datasources.map(ds => <SelectItem key={ds.id}>{ds.label}</SelectItem>)}
        </Select>

        {logicalModel && (
            <Select
                label='Mappings'
                selectedKeys={new Set(step.mappingIds)}
                placeholder='Select mappings'
                selectionMode='multiple'
                onSelectionChange={keys => updateStep({ ...step, mappingIds: [ ...(keys as Set<string>) ] })}
            >
                {logicalModel.mappings.map(mapping => (
                    <SelectItem key={mapping.id}>{mapping.kindName}</SelectItem>
                ))}
            </Select>
        )}
    </>);
}

function InferenceStepForm({ step, datasources, updateStep }: StepFormProps<RSDToCategoryPayloadInit>) {
    return (
        <Select
            label='Datasources'
            selectedKeys={new Set(step.datasourceIds)}
            placeholder='Select datasources'
            onSelectionChange={keys => {
                updateStep({
                    ...step,
                    datasourceIds: [ ...(keys as Set<string>) ],
                });
            }}
        >
            {datasources.map(ds => <SelectItem key={ds.id}>{ds.label}</SelectItem>)}
        </Select>
    );
}
