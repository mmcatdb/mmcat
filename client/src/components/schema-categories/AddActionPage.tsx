import { Button, Input, Select, SelectItem } from '@nextui-org/react';
import { type ActionInit, type ActionType, ACTION_TYPES } from '@/types/action';
import { useState } from 'react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { ErrorPage } from '@/pages/errorPages';

export function AddActionPage() {
    const [ label, setLabel ] = useState('');
    const [ type, setType ] = useState<ActionType | ''>('');
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState(false);
    const { category } = useCategoryInfo();
    const navigate = useNavigate();

    const handleSubmit = async () => {
        if (!label || !type) {
            toast.error('All fields are required.');
            return;
        }

        setLoading(true);
        setError(false);

        const newAction: ActionInit = {
            categoryId: category.id,
            label,
            payloads: [], // TODO: Extend this, INIT STATE HAS AT LEAST ONE STEP
        };

        const response = await api.actions.createAction({}, newAction);
        if (!response.status) 
            setError(true);
        else 
            toast.success('Action created successfully.');
        
        navigate(-1);
        setLoading(false);
    };

    return (
        <div className='p-6'>
            <h1 className='text-xl font-semibold mb-4'>Add Action</h1>
            {error && <ErrorPage />}
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
  
const SelectActionType = ({
    actionType,
    setActionType,
}: SelectActionTypeProps) => (
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