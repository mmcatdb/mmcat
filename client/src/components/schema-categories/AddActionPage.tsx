import { Button, Input, Select, SelectItem } from '@nextui-org/react';
import { useEffect, useState } from 'react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { ErrorPage } from '@/pages/errorPages';
import { type ActionInit, ActionType, type JobPayloadInit, ACTION_TYPES } from '@/types/action';
import { Datasource } from '@/types/datasource';
import { logicalModelsFromServer } from '@/types/datasource';

export function AddActionPage() {
    const [ label, setLabel ] = useState('');
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState(false);
    const { category } = useCategoryInfo();
    const navigate = useNavigate();
    const [ type, setType ] = useState<ActionType>(ActionType.ModelToCategory); // Default preselect
    const [ steps, setSteps ] = useState<JobPayloadInit[]>([
        { type: ActionType.ModelToCategory, datasourceId: '', mappingIds: [] }, // Default step
    ]);

    const [ datasources, setDatasources ] = useState<Datasource[]>([]);
    const [ logicalModels, setLogicalModels ] = useState<ReturnType<typeof logicalModelsFromServer>>([]);

    // TODO: NEFETCHOVAT, ALE PŘEDÁVAT ZESHORA
    useEffect(() => {
        async function fetchDatasourcesAndMappings() {
            const dsResponse = await api.datasources.getAllDatasources({}, { categoryId: category.id });
            const mappingsResponse = await api.mappings.getAllMappingsInCategory({}, { categoryId: category.id });

            if (!dsResponse.status ||  !mappingsResponse.status) {
                toast.error('Error fetching data from server.');
                setError(true);
                return;
            }
            const datasourcesFromServer = dsResponse.data;
            const mappingsFromServer = mappingsResponse.data;

            setLogicalModels(logicalModelsFromServer(datasourcesFromServer, mappingsFromServer));
            setDatasources(datasourcesFromServer.map(Datasource.fromServer));
        }

        fetchDatasourcesAndMappings();
    }, [ category.id ]);

    function addStep() {
        if (!type) {
            toast.error('Select an action type before adding steps.');
            return;
        }

        // Add a new step based on the current type
        if (type === ActionType.ModelToCategory || type === ActionType.CategoryToModel) 
            setSteps((prevSteps) => [ ...prevSteps, { type, datasourceId: '', mappingIds: [] } ]);
        else if (type === ActionType.RSDToCategory) 
            setSteps((prevSteps) => [ ...prevSteps, { type, datasourceIds: [] } ]);
    }

    function removeStep(index: number) {
        if (steps.length <= 1) {
            toast.error('At least one step is required.');
            return;
        }
        setSteps((prevSteps) => prevSteps.filter((_, i) => i !== index));
    }

    async function handleSubmit() {
        if (!label || !type || steps.length === 0) {
            toast.error('All fields and at least one step are required.');
            return;
        }

        setLoading(true);
        setError(false);

        const newAction: ActionInit = {
            categoryId: category.id,
            label,
            payloads: steps,
        };

        const response = await api.actions.createAction({}, newAction);
        if (!response.status) {
            toast.error('Something went wrong when creating an action.');
            setError(true);
            navigate(-1);
            return;
        }
        toast.success('Action created successfully.');
        navigate(-1);

        setLoading(false);
    }

    // TODO: resource not found
    if (error) 
        return <ErrorPage />;

    return (
        <div className='p-6'>
            <h1 className='text-xl font-semibold mb-4'>Add Action</h1>
            <div className='mb-4'>
                <Input
                    label='Label'
                    value={label}
                    onChange={(e) => setLabel(e.target.value)}
                    placeholder='Enter action label'
                />
            </div>
            <div className='mb-4'>
                <SelectActionType
                    actionType={type}
                    setActionType={(type) => setType(type)}
                />
            </div>
            <h2 className='text-lg font-semibold mb-2'>Steps</h2>
            <div className='mb-4'>
                {steps.map((step, index) => (
                    <StepForm
                        key={index}
                        step={step}
                        type={type}
                        datasources={datasources}
                        mappings={logicalModels}
                        updateStep={(updatedStep) => setSteps((prev) => prev.map((s, i) => (i === index ? updatedStep : s)))}
                        removeStep={() => removeStep(index)}
                        steps={steps}
                    />
                ))}
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
                    onPress={handleSubmit}
                    isLoading={loading}
                >
                    Submit
                </Button>
            </div>
        </div>
    );
}

type SelectActionTypeProps = {
    actionType: ActionType | '';
    setActionType: (type: ActionType) => void;
};

function SelectActionType({ actionType, setActionType }: SelectActionTypeProps) {
    return (
        <Select
            items={ACTION_TYPES}
            label='Action Type'
            placeholder='Select an Action Type'
            selectedKeys={actionType ? new Set([ actionType ]) : new Set()}
            onSelectionChange={(e) => {
                const selectedType = Array.from(e as Set<ActionType>)[0];
                setActionType(selectedType);
            }}
        >
            {(item) => <SelectItem key={item.value}>{item.label}</SelectItem>}
        </Select>
    );
}

type StepFormProps = {
    step: JobPayloadInit;
    type: ActionType | '';
    datasources: Datasource[];
    mappings: ReturnType<typeof logicalModelsFromServer>;
    updateStep: (step: JobPayloadInit) => void;
    removeStep: () => void;
    steps: JobPayloadInit[];
};

function StepForm({ step, type, datasources, mappings, updateStep, removeStep, steps }: StepFormProps) {
    if (type === ActionType.ModelToCategory || type === ActionType.CategoryToModel) {
        // narrowing ActionType
        const modelToCategoryStep = step as {
            type: ActionType.ModelToCategory | ActionType.CategoryToModel;
            datasourceId: string;
            mappingIds: string[];
        };

        modelToCategoryStep.type = type;
        const datasourceMappings = mappings.find((m) => m.datasource.id === modelToCategoryStep.datasourceId);

        return (
            <div className='mb-4 p-4 border rounded'>
                <Select
                    label='Datasource'
                    selectedKeys={
                        modelToCategoryStep.datasourceId ? new Set([ modelToCategoryStep.datasourceId ]) : new Set()
                    }
                    placeholder='Select a datasource'
                    onSelectionChange={(e) => {
                        const selectedDatasourceId = Array.from(e as Set<string>)[0];
                        updateStep({
                            ...modelToCategoryStep,
                            datasourceId: selectedDatasourceId,
                            mappingIds: [],
                        });
                    }}
                >
                    {datasources.map((ds) => (
                        <SelectItem key={ds.id}>{ds.label}</SelectItem>
                    ))}
                </Select>
                {datasourceMappings && (
                    <Select
                        label='Mappings'
                        selectedKeys={new Set(modelToCategoryStep.mappingIds)}
                        placeholder='Select mappings'
                        selectionMode='multiple'
                        onSelectionChange={(e) => {
                            const selectedMappingIds = Array.from(e as Set<string>);
                            updateStep({
                                ...modelToCategoryStep,
                                mappingIds: selectedMappingIds,
                            });
                        }}
                    >
                        {datasourceMappings.mappings.map((mapping) => (
                            <SelectItem key={mapping.id}>{mapping.kindName}</SelectItem>
                        ))}
                    </Select>
                )}
                <Button
                    color='danger'
                    onPress={removeStep}
                    className='mt-2'
                    isDisabled={steps.length === 1}
                >
                    Remove Step
                </Button>
            </div>
        );
    }

    if (type === ActionType.RSDToCategory) {
        const rsdToCategoryStep = step as {
            type: ActionType.RSDToCategory;
            datasourceIds: string[];
        };

        rsdToCategoryStep.type = type;

        return (
            <div className='mb-4 p-4 border rounded'>
                <Select
                    label='Datasources'
                    selectedKeys={new Set(rsdToCategoryStep.datasourceIds)}
                    placeholder='Select datasources'
                    onSelectionChange={(e) => {
                        const selectedDatasourceIds = Array.from(e as Set<string>);
                        updateStep({
                            ...rsdToCategoryStep,
                            datasourceIds: selectedDatasourceIds,
                        });
                    }}
                >
                    {datasources.map((ds) => (
                        <SelectItem key={ds.id}>{ds.label}</SelectItem>
                    ))}
                </Select>
                <Button color='danger' onPress={removeStep} className='mt-2'>
                    Remove Step
                </Button>
            </div>
        );
    }

    return null;
}