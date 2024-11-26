import { Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Select, SelectItem, Table, TableBody, TableCell, TableHeader, TableRow } from '@nextui-org/react';
import { AddIcon } from '@/components/icons/PlusIcon';
import { EmptyState } from '@/components/TableCommon';
import { ErrorPage, LoadingPage } from '@/pages/errorPages';
import { Action, ACTION_TYPES, type JobPayloadInit, type ActionInit, type ActionType } from '@/types/action';
import { toast } from 'react-toastify';
import { useActions } from '@/components/schema-categories/ActionsReducer';
import { useState } from 'react';
import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';


export function ActionsPage() {
    const {
        actions,
        loading,
        error,
        isModalOpen,
        setModalOpen,
        addAction,
        deleteAction,
    } = useActions();

    if (error)
        return <ErrorPage />;

    return (
        <div>
            <div className='flex items-center justify-between'>
                <h1>Actions</h1>
                <Button
                    onPress={() => setModalOpen(true)}
                    color='primary'
                    startContent={<AddIcon />}
                    isDisabled={loading}
                >
                    Add Action
                </Button>
            </div>

            <div className='mt-5'>
                {loading ? (
                    <LoadingPage />
                ) : actions.length > 0 ? (
                    <ActionsTable
                        actions={actions}
                        onDeleteAction={deleteAction}
                    />
                ) : (
                    <EmptyState
                        message='No actions available.'
                        buttonText='+ Add Action'
                        onButtonClick={() => toast.error('Not implemented')}
                    />
                )}
            </div>

            {isModalOpen && (
                <AddActionModal
                    isOpen={isModalOpen}
                    onClose={() => setModalOpen(false)}
                    onAddAction={addAction}
                />
            )}
        </div>
    );
}

type ActionsTableProps = {
    actions: Action[];
    onDeleteAction: (id: string) => void;
};

export function ActionsTable({ actions, onDeleteAction }: ActionsTableProps) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Actions</TableCell>
                </TableRow>
            </TableHeader>
            <TableBody>
                {actions.map((action) => (
                    <TableRow key={action.id}>
                        <TableCell>{action.id}</TableCell>
                        <TableCell>{action.payload.type}</TableCell>
                        <TableCell>
                            <Button color='danger' onPress={() => onDeleteAction(action.id)}>
                                Delete
                            </Button>
                            <Button color='primary' className='ml-2'>
                                Create Job
                            </Button>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}

type AddActionModalProps = {
    isOpen: boolean;
    onClose: () => void;
    onAddAction: (newAction: Action) => void;
};

export function AddActionModal({ isOpen, onClose, onAddAction }: AddActionModalProps) {
    const [ label, setLabel ] = useState('');
    const [ type, setType ] = useState<ActionType | ''>('');
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState<string | null>(null);
    const { category } = useCategoryInfo();

    const [ payloads, setPayloads ] = useState<JobPayloadInit[]>([]);

    const addPayload = (newPayload: JobPayloadInit) => {
        setPayloads((prev) => [ ...prev, newPayload ]);
    };

    const handlePayloadChange = (updatedPayload: JobPayloadInit[]) => {
        setPayloads(updatedPayload);
    };

    const handleSubmit = async () => {
        if (!label || !type) {
            setError('All fields are required.');
            return;
        }

        setLoading(true);
        setError(null);

        const newAction: ActionInit = {
            categoryId: category.id,
            label,
            payloads,
        };

        try {
            const response = await api.actions.createAction({}, newAction);
            if (response.status && response.data) {
                const createdAction = Action.fromServer(response.data);
                onAddAction(createdAction);
                onClose();
            }
            else {
                setError('Failed to create action.');
            }
        }
        catch (err) {
            setError('Error occurred while creating the action.');
        }
        finally {
            setLoading(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose}>
            <ModalContent>
                <ModalHeader>
                    <p>Add Action</p>
                </ModalHeader>
                <ModalBody>
                    {error && <p className='text-red-500'>{error}</p>}
                    <Input
                        label='Label'
                        value={label}
                        onChange={(e) => setLabel(e.target.value)}
                        placeholder='Enter action label'
                    />
                    <SelectActionType
                        actionType={type}
                        setActionType={(type) => {
                            console.log('Selected action type:', type);
                            setType(type);
                        }}
                        isDisabled={false}
                    />
                </ModalBody>
                <ModalFooter>
                    <Button onPress={onClose} isDisabled={loading}>
                        Cancel
                    </Button>
                    <Button
                        color='primary'
                        onPress={handleSubmit}
                        isDisabled={loading || !label || !type}
                        isLoading={loading}
                    >
                        Add Action
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}

type SelectActionTypeProps = {
    actionType: ActionType | '';
    setActionType: (type: ActionType) => void;
    isDisabled: boolean;
};
  
const SelectActionType = ({
    actionType,
    setActionType,
    isDisabled,
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
        isDisabled={isDisabled}
    >
        {(item) => <SelectItem key={item.value}>{item.label}</SelectItem>}
    </Select>
);
