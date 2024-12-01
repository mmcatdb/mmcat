import { Button, type SortDescriptor, Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@nextui-org/react';
import { Action } from '@/types/action';
import { useEffect, useState } from 'react';
import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { LoadingPage } from '../errorPages';
import { EmptyState, useSortableData } from '@/components/TableCommon';
import { AddIcon } from '@/components/icons/PlusIcon';
import { usePreferences } from '@/components/PreferencesProvider';
import { Outlet, useNavigate } from 'react-router-dom';
import { TrashIcon } from '@heroicons/react/24/outline'; 

export function ActionsPage() {
    return (
        <div>
            <Outlet />
        </div>
    );
}

export function ActionsPageOverview() {
    const [ actions, setActions ] = useState<Action[]>([]);
    const [ loading, setLoading ] = useState(false);
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
            setActions(actionsFromServer);

            setLoading(false);
            return true;
        };

        fetchActions();
    }, [ category.id ]);

    const deleteAction = async (actionId: string) => {
        const result = await api.actions.deleteAction({ id: actionId });

        if (!result.status) {
            toast.error('Error deleting action');
            return;
        }
        
        setActions((prev) => prev.filter((action) => action.id !== actionId));
        toast.success('Action deleted successfully');
    };

    const navigate = useNavigate();

    return (
        <div className='p-6'>
            <div className='flex items-center justify-between mb-4'>
                <h1 className='text-xl font-semibold'>Actions</h1>
                <Button
                    onPress={() => navigate(`/category/${category.id}/actions/add`)}
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
                        onButtonClick={() => navigate(`/category/${category.id}/actions/add`)}
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
    const { showTableIDs } = usePreferences().preferences;
    const { sortedData: sortedActions, sortDescriptor, setSortDescriptor } = useSortableData(actions, {
        column: 'label',
        direction: 'ascending',
    });

    const handleSortChange = (newSortDescriptor: SortDescriptor) => {
        setSortDescriptor(newSortDescriptor);
    };

    return (
        <Table 
            aria-label='Actions table'
            sortDescriptor={sortDescriptor}
            onSortChange={handleSortChange}
        >
            <TableHeader>
                {[
                    ...(showTableIDs
                        ? [
                            <TableColumn key='id' allowsSorting>
                                ID
                            </TableColumn>,
                        ]
                        : []),
                    <TableColumn key='label' allowsSorting>Label</TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent={'No mappings to display.'}>
                {sortedActions.map((action) => (
                    <TableRow key={action.id}>
                        {[
                            ...(showTableIDs
                                ? [ <TableCell key='id'>{action.id}</TableCell> ]
                                : []),
                            <TableCell key='label'>{action.label}</TableCell>,
                            // TODO: udělat detail Action s přiřazenými typy, Datasources a mapováním
                            // <TableCell key='type'>{action.payloads.map((p) => p.type).join(', ')}</TableCell>,
                            <TableCell key='actions' className='flex items-center space-x-2'>
                                <Button
                                    isIconOnly
                                    aria-label='Delete action'
                                    color='danger'
                                    variant='light' 
                                    onPress={() => onDeleteAction(action.id)}
                                >
                                    <TrashIcon className='w-5 h-5' />
                                </Button>
                                <Button color='primary' variant='bordered'>
                                    Create Run
                                </Button>
                            </TableCell>,
                        ]}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}