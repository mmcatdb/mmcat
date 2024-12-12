import { Button, type SortDescriptor, Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@nextui-org/react';
import { Action, ActionType } from '@/types/action';
import { useEffect, useState } from 'react';
import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { LoadingPage, ReloadPage } from '../errorPages';
import { ConfirmationModal, EmptyState, useSortableData } from '@/components/TableCommon';
import { AddIcon } from '@/components/icons/PlusIcon';
import { usePreferences } from '@/components/PreferencesProvider';
import { Link, Outlet, type Params, useLoaderData, useNavigate } from 'react-router-dom';
import { TrashIcon } from '@heroicons/react/24/outline'; 
import { cn } from '@/components/utils';

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
    const [ error, setError ] = useState(false);
    const { category } = useCategoryInfo();

    async function fetchActions() {
        setLoading(true);
        setError(false);

        const response = await api.actions.getAllActionsInCategory({
            categoryId: category.id,
        });
        setLoading(false);

        if (!response.status) {
            setError(true);
            return;
        }
            
        const actionsFromServer = response.data.map(Action.fromServer);
        setActions(actionsFromServer);
    }

    useEffect(() => {
        void fetchActions();
    }, [ category.id ]);

    async function deleteAction(actionId: string) {
        const result = await api.actions.deleteAction({ id: actionId });

        if (!result.status) {
            toast.error('Error deleting action');
            return;
        }
        
        setActions((prev) => prev.filter((action) => action.id !== actionId));
        toast.success('Action deleted successfully');
    }

    const navigate = useNavigate();

    if (loading)
        return <LoadingPage />;

    if (error) 
        return <ReloadPage onReload={fetchActions} title='Actions' message='Failed to load actions.'/>;

    return (
        <div>
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
    const [ selectedAction, setSelectedAction ] = useState<Action | null>(null);

    function handleSortChange(newSortDescriptor: SortDescriptor) {
        setSortDescriptor(newSortDescriptor);
    }

    async function createRun(actionId: string) {
        setLoadingMap((prev) => ({ ...prev, [actionId]: true }));

        const response = await api.jobs.createRun({ actionId });

        if (!response.status) {
            toast.error('Error creating run');
        }
        else {
            toast.success('Run created successfully.');
            console.log('New Run:', response.data); // TODO: navigate to Runs page, or specific run
        }

        setLoadingMap((prev) => ({ ...prev, [actionId]: false }));
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
        setSelectedAction(null);
        setIsModalOpen(false);
    }

    function confirmDelete() {
        if (selectedAction) 
            onDeleteAction(selectedAction.id);
        
        closeModal();
    }

    return (
        <>
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
                    {sortedActions.map((action) => (
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
        </>
    );
}

export type ActionLoaderData = {
    action: Action;
};

export async function actionLoader({ params: { actionId } }: { params: Params<'actionId'> }): Promise<ActionLoaderData> {
    if (!actionId) 
        throw new Error('Action ID is required');
    
    const response = await api.actions.getAction({ id: actionId });

    if (!response.status) 
        throw new Error('Failed to load action');
    
    return {
        action: Action.fromServer(response.data),
    };
}

export function ActionDetailPage() {
    const { action } = useLoaderData() as ActionLoaderData;
    const navigate = useNavigate();
    const [ isCreatingRun, setIsCreatingRun ] = useState<boolean>(false);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ isDeleting, setIsDeleting ] = useState(false);

    const openModal = () => setIsModalOpen(true);
    const closeModal = () => setIsModalOpen(false);

    async function confirmDelete() {
        setIsDeleting(true);
        const result = await api.actions.deleteAction({ id: action.id });
        setIsDeleting(false);

        if (!result.status) {
            toast.error('Error deleting action');
            return;
        }

        toast.success('Action deleted successfully');
        navigate(-1);
    }

    async function handleCreateRun(actionId: string) {
        setIsCreatingRun(true);
        const response = await api.jobs.createRun({ actionId });
        setIsCreatingRun(false);

        if (!response.status) {
            toast.error('Error creating run');
        }
        else {
            toast.success('Run created successfully.');
            console.log('New Run:', response.data); // TODO: navigate to Runs page, or specific run
        }
    }

    return (
        <div>
            <h1 className='text-2xl font-bold mb-4'>{action.label}</h1>
            <p className='mb-4'>
                <strong>ID:</strong> {action.id}
            </p>

            <div className='mb-6'>
                <h2 className='text-xl font-semibold mb-2'>Steps</h2>
                {action.payloads && action.payloads.length > 0 ? (
                    <Table aria-label='Steps table'>
                        <TableHeader>
                            <TableColumn>Index</TableColumn>
                            <TableColumn>Type</TableColumn>
                            <TableColumn>Datasource</TableColumn>
                            <TableColumn>Mappings</TableColumn>
                        </TableHeader>
                        <TableBody>
                            {/* TODO: komponenta ds element */}
                            {action.payloads.map((payload, index) => {
                                let datasourceElement = <span>N/A</span>;
                                let mappings = 'N/A';

                                if (payload.type === ActionType.ModelToCategory || payload.type === ActionType.CategoryToModel) {
                                    // datasource = payload.datasource?.label || 'N/A';
                                    if (payload.datasource) {
                                        datasourceElement = (
                                            <Link
                                                to={`/category/${action.categoryId}/datasources/${payload.datasource.id}`}
                                                className='text-blue-500 hover:underline'
                                            >
                                                {payload.datasource.label}
                                            </Link>
                                        );
                                    }
                                    mappings = payload.mappings?.map((m) => m.kindName).join(', ') || 'N/A';
                                }
                                else if (payload.type === ActionType.RSDToCategory) {
                                    // datasource = payload.datasources?.map((ds) => ds.label).join(', ') || 'N/A';
                                    datasourceElement = (
                                        <div className='space-y-1'>
                                            {payload.datasources?.map((ds) => (
                                                <Link
                                                    key={ds.id}
                                                    to={`/category/${action.categoryId}/datasources/${ds.id}`}
                                                    className='text-blue-500 hover:underline'
                                                >
                                                    {ds.label}
                                                </Link>
                                            )) || 'N/A'}
                                        </div>
                                    );
                                }

                                return (
                                    <TableRow key={index}>
                                        <TableCell>{index}</TableCell>
                                        <TableCell>{payload.type}</TableCell>
                                        {/* <TableCell>{datasource}</TableCell> */}
                                        <TableCell>{datasourceElement}</TableCell>
                                        <TableCell>{mappings}</TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                ) : (
                    <p className='text-gray-500'>No steps available.</p>
                )}
            </div>

            <div className='flex space-x-4'>
                <Button
                    color='primary'
                    variant='bordered'
                    isDisabled={isCreatingRun}
                    onPress={() => handleCreateRun(action.id)}
                >
                    {isCreatingRun ? 'Creating...' : 'Create Run'}
                </Button>
                <Button
                    color='danger'
                    variant='bordered'
                    onPress={() => openModal()}
                    isDisabled={isDeleting}
                >
                    Delete
                </Button>
            </div>

            <ConfirmationModal
                isOpen={isModalOpen}
                onClose={closeModal}
                onConfirm={confirmDelete}
                title='Confirm Deletion?'
                message='This will permanently delete the selected datasource.'
                confirmButtonText='Yes, Delete'
                cancelButtonText='Cancel'
                confirmButtonColor='danger'
            />
        </div>
    );
}
