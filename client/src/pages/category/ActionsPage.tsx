import { Button, type SortDescriptor, Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@nextui-org/react';
import { Action } from '@/types/action';
import { useState } from 'react';
import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { ConfirmationModal, EmptyState, useSortableData } from '@/components/TableCommon';
import { AddIcon } from '@/components/icons/PlusIcon';
import { usePreferences } from '@/components/PreferencesProvider';
import { type Params, useLoaderData, useNavigate } from 'react-router-dom';
import { TrashIcon } from '@heroicons/react/24/outline';
import { cn } from '@/components/utils';

export function ActionsPage() {
    const data = useLoaderData() as ActionsLoaderData;
    const [ actions, setActions ] = useState<Action[]>(data.actions);

    const { category } = useCategoryInfo();

    async function deleteAction(actionId: string) {
        const result = await api.actions.deleteAction({ id: actionId });
        if (!result.status) {
            toast.error('Error deleting action');
            return;
        }

        setActions(prev => prev.filter(action => action.id !== actionId));
        toast.success('Action deleted successfully');
    }

    const navigate = useNavigate();

    return (
        <div>
            <div className='flex items-center justify-between mb-4'>
                <h1 className='text-xl font-semibold'>Actions</h1>
                <Button
                    onPress={() => navigate(`/category/${category.id}/actions/add`)}
                    color='primary'
                    startContent={<AddIcon />}
                >
                    Add Action
                </Button>
            </div>

            <div>
                {actions.length > 0 ? (
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

ActionsPage.loader = actionsLoader;

export type ActionsLoaderData = {
    actions: Action[];
};

async function actionsLoader({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<ActionsLoaderData> {
    if (!categoryId)
        throw new Error('Action ID is required');

    const response = await api.actions.getAllActionsInCategory({ categoryId });
    if (!response.status)
        throw new Error('Failed to load actions');

    return {
        actions: response.data.map(Action.fromServer),
    };
}

type ActionsTableProps = {
    actions: Action[];
    onDeleteAction: (id: string) => void;
};

function ActionsTable({ actions, onDeleteAction }: ActionsTableProps) {
    const { theme, showTableIDs } = usePreferences().preferences;
    const { sortedData: sortedActions, sortDescriptor, setSortDescriptor } = useSortableData(actions, {
        column: 'label',
        direction: 'ascending',
    });
    const [ loadingMap, setLoadingMap ] = useState<Record<string, boolean>>({});
    const { category } = useCategoryInfo();
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ selectedAction, setSelectedAction ] = useState<Action>();

    function handleSortChange(newSortDescriptor: SortDescriptor) {
        setSortDescriptor(newSortDescriptor);
    }

    async function createRun(actionId: string) {
        setLoadingMap(prev => ({ ...prev, [actionId]: true }));

        const response = await api.jobs.createRun({ actionId });

        if (!response.status) {
            toast.error('Error creating run');
        }
        else {
            toast.success('Run created successfully.');
            console.log('New Run:', response.data);
        }

        setLoadingMap(prev => ({ ...prev, [actionId]: false }));
    }

    const navigate = useNavigate();

    function handleRowAction(key: React.Key) {
        if (category.id) {
            navigate(`/category/${category.id}/actions/${key}`, {
                state: { sortDescriptor },
            });
        }
    }

    function openModal(action: Action) {
        setSelectedAction(action);
        setIsModalOpen(true);
    }

    function closeModal() {
        setSelectedAction(undefined);
        setIsModalOpen(false);
    }

    function confirmDelete() {
        if (selectedAction)
            onDeleteAction(selectedAction.id);

        closeModal();
    }

    return (<>
        <Table
            aria-label='Actions table'
            sortDescriptor={sortDescriptor}
            onSortChange={handleSortChange}
            onRowAction={handleRowAction}
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
                    <TableColumn key='label' allowsSorting>
                        Label
                    </TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent={'No mappings to display.'}>
                {sortedActions.map(action => (
                    <TableRow
                        key={action.id}
                        className={cn('cursor-pointer',
                            theme === 'dark' ? 'hover:bg-zinc-800 focus:bg-zinc-700' : 'hover:bg-zinc-100 focus:bg-zinc-200',
                        )}
                    >
                        {[
                            ...(showTableIDs
                                ? [ <TableCell key='id'>{action.id}</TableCell> ]
                                : []),
                            <TableCell key='label'>{action.label}</TableCell>,
                            <TableCell key='actions' className='flex items-center space-x-2'>
                                <Button
                                    isIconOnly
                                    aria-label='Delete action'
                                    color='danger'
                                    variant='light'
                                    onPress={() => openModal(action)}
                                >
                                    <TrashIcon className='w-5 h-5' />
                                </Button>
                                <Button
                                    color='primary'
                                    variant='bordered'
                                    isDisabled={loadingMap[action.id]}
                                    onPress={() => createRun(action.id)}
                                >
                                    {loadingMap[action.id] ? 'Creating...' : 'Create Run'}
                                </Button>
                            </TableCell>,
                        ]}
                    </TableRow>
                ))}
            </TableBody>
        </Table>

        {selectedAction && (
            <ConfirmationModal
                isOpen={isModalOpen}
                onClose={closeModal}
                title='Confirm Deletion'
                message={`Are you sure you want to delete the action "${selectedAction.label}"?`}
                confirmButtonText='Delete'
                confirmButtonColor='danger'
                cancelButtonText='Cancel'
                onConfirm={confirmDelete}
            />
        )}
    </>);
}
