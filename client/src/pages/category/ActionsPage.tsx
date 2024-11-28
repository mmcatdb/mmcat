import { Button, Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@nextui-org/react';
import { Action } from '@/types/action';
import { useEffect, useState } from 'react';
import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { LoadingPage } from '../errorPages';
import { EmptyState } from '@/components/TableCommon';
import { AddIcon } from '@/components/icons/PlusIcon';

export function ActionsPage() {
    const [ actions, setActions ] = useState<Action[]>([]);
    const [ loading, setLoading ] = useState(false);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const { category } = useCategoryInfo();

    useEffect(() => {
        const fetchActions = async () => {
            setLoading(true);

            const response = await api.actions.getAllActionsInCategory({
                categoryId: category.id,
            });

            if (!response.status) 
                return false;
            
            const actionsFromServer = response.data.map(Action.fromServer);
            console.log('Got HERE, Fetched Actions:', actionsFromServer);
            setActions(actionsFromServer);

            setLoading(false);
            return true;
        };

        fetchActions();
    }, [ category.id ]);

    // Delete an action
    const deleteAction = async (actionId: string) => {
        try {
            await api.actions.deleteAction({ id: actionId });
            setActions((prev) => prev.filter((action) => action.id !== actionId));
            toast.success('Action deleted successfully');
        }
        catch (error) {
            toast.error('Error deleting action');
            console.error(error);
        }
    };

    return (
        <div className='p-6'>
            <div className='flex items-center justify-between mb-4'>
                <h1 className='text-xl font-semibold'>Actions</h1>
                <Button
                    onPress={() => setIsModalOpen(true)}
                    color='primary'
                    startContent={<AddIcon />}
                    isDisabled={loading}
                >
                    Add Action
                </Button>
            </div>

            <div>
                {loading ? (
                    <LoadingPage />
                ) : actions.length > 0 ? (
                    <ActionsTable actions={actions} onDeleteAction={deleteAction} />
                ) : (
                    <EmptyState
                        message='No actions available.'
                        buttonText='+ Add Action'
                        onButtonClick={() => setIsModalOpen(true)}
                    />
                )}
            </div>
        </div>
    );
}

type ActionsTableProps = {
    actions: Action[];
    onDeleteAction: (id: string) => void;
};

function ActionsTable({ actions, onDeleteAction }: ActionsTableProps) {
    return (
        <Table aria-label='Actions table'>
            <TableHeader>
                <TableColumn key='id'>ID</TableColumn>
                <TableColumn key='label'>Label</TableColumn>
                <TableColumn>Type</TableColumn>
                <TableColumn key='actions'>Actions</TableColumn>
            </TableHeader>
            <TableBody>
                {actions.map((action) => (
                    <TableRow key={action.id}>
                        <TableCell key='id'>{action.id}</TableCell>
                        <TableCell key='label'>{action.label}</TableCell>
                        <TableCell>{action.payloads.map((p) => p.type).join(', ')}</TableCell>
                        <TableCell key='actions'>
                            <Button color='danger' onPress={() => onDeleteAction(action.id)}>
                                Delete
                            </Button>
                            <Button color='primary' className='ml-2'>
                                Create Run
                            </Button>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}

