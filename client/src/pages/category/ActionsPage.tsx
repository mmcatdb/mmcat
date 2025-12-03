import { Button, Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@heroui/react';
import { type ActionInfo } from '@/types/job';
import { useState } from 'react';
import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { toast } from 'react-toastify';
import { ConfirmationModal, EmptyState, useSortableData } from '@/components/TableCommon';
import { usePreferences } from '@/components/PreferencesProvider';
import { Link, type Params, useLoaderData, useNavigate } from 'react-router-dom';
import { TrashIcon } from '@heroicons/react/24/outline';
import { GoDotFill } from 'react-icons/go';
import { useBannerState } from '@/types/utils/useBannerState';
import { routes } from '@/routes/routes';
import { FaPlus } from 'react-icons/fa';
import { InfoBanner, InfoTooltip } from '@/components/common';
import { PageLayout } from '@/components/RootLayout';
import { type Id } from '@/types/id';

export function ActionsPage() {
    const data = useLoaderData() as ActionsLoaderData;
    const [ actions, setActions ] = useState<ActionInfo[]>(data.actions);
    const { category } = useCategoryInfo();
    const banner = useBannerState('actions-page');

    async function deleteAction(actionId: Id) {
        const result = await api.actions.deleteAction({ id: actionId });
        if (!result.status) {
            toast.error('Error deleting action');
            return;
        }

        setActions(prev => prev.filter(action => action.id !== actionId));
        toast.success('Action deleted successfully');
    }

    return (
        <PageLayout>
            {/* Header with Info Icon */}
            <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                    <h1 className='text-xl font-semibold'>Actions</h1>

                    <InfoTooltip {...banner} />
                </div>

                <Button
                    as={Link}
                    to={routes.category.actions.new.resolve({ categoryId: category.id })}
                    color='primary'
                    startContent={<FaPlus className='size-4' />}
                >
                    Add Action
                </Button>
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <ActionInfoInner />
            </InfoBanner>

            {/* Actions Table or Empty State */}
            <div>
                {actions.length > 0 ? (
                    <ActionsTable
                        actions={actions}
                        onDeleteAction={deleteAction}
                    />
                ) : (
                    <EmptyState
                        message='No actions available.'
                        buttonText='Add Action'
                        buttonStartContent={<FaPlus className='size-4' />}
                        to={routes.category.actions.new.resolve({ categoryId: category.id })}
                    />
                )}
            </div>
        </PageLayout>
    );
}

type ActionsLoaderData = {
    actions: ActionInfo[];
};

ActionsPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<ActionsLoaderData> => {
    if (!categoryId)
        throw new Error('Action ID is required');

    const response = await api.actions.getAllActionsInCategory({ categoryId });
    if (!response.status)
        throw new Error('Failed to load actions');

    return {
        actions: response.data,
    };
};

type ActionsTableProps = {
    actions: ActionInfo[];
    onDeleteAction: (id: Id) => void;
};

function ActionsTable({ actions, onDeleteAction }: ActionsTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { sortedData: sortedActions, sortDescriptor, setSortDescriptor } = useSortableData(actions, {
        column: 'label',
        direction: 'ascending',
    });
    const [ loadingMap, setLoadingMap ] = useState<Record<Id, boolean>>({});
    const { category } = useCategoryInfo();
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ selectedAction, setSelectedAction ] = useState<ActionInfo>();

    async function createRun(actionId: Id) {
        setLoadingMap(prev => ({ ...prev, [actionId]: true }));

        const response = await api.jobs.createRun({ actionId });

        if (!response.status)
            toast.error('Error creating run');
        else
            toast.success('Run created successfully.');
            // console.log('New Run:', response.data);

        setLoadingMap(prev => ({ ...prev, [actionId]: false }));
    }

    const navigate = useNavigate();

    function handleRowAction(key: React.Key) {
        if (category.id) {
            navigate(routes.category.actions.detail.resolve({ categoryId: category.id, actionId: String(key) }), {
                state: { sortDescriptor },
            });
        }
    }

    function openModal(action: ActionInfo) {
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
            onSortChange={setSortDescriptor}
            onRowAction={handleRowAction}
        >
            <TableHeader>
                {[
                    ...(showTableIDs ? [
                        <TableColumn key='id' allowsSorting>
                            ID
                        </TableColumn>,
                    ] : []),
                    <TableColumn key='label' allowsSorting>
                        Label
                    </TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent='No mappings to display.'>
                {sortedActions.map(action => (
                    <TableRow
                        key={action.id}
                        className='cursor-pointer hover:bg-default-100 focus:bg-default-200'
                    >
                        {[
                            ...(showTableIDs ? [
                                <TableCell key='id'>{action.id}</TableCell>,
                            ] : []),
                            <TableCell key='label'>{action.label}</TableCell>,
                            <TableCell key='actions' className='flex items-center space-x-2'>
                                <Button
                                    isIconOnly
                                    aria-label='Delete action'
                                    color='danger'
                                    variant='light'
                                    onPress={() => openModal(action)}
                                >
                                    <TrashIcon className='size-5' />
                                </Button>
                                <Button
                                    color='primary'
                                    variant='flat'
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

export function ActionInfoInner() {
    return (<>
        <h2 className='text-lg font-semibold mb-2'>Understanding Actions & Jobs</h2>

        {/* Info Content */}
        <p className='text-sm'>
            An <span className='font-bold'>Action</span> is something that <span className='font-bold'>spawns Jobs</span>.
            Think of it as a <span className='font-bold'>trigger</span> for executing transformations or data processing tasks.
            For example, if you want to <span className='font-bold'>export data to PostgreSQL</span>, you create an <span className='font-bold'>Action</span> to start the process.
        </p>

        <ul className='mt-3 text-sm space-y-2'>
            <li className='flex items-center gap-2'>
                <GoDotFill className='text-primary-500' />
                <span className='font-bold'>Action:</span> Spawns jobs (e.g., exporting data to PostgreSQL).
            </li>
            <li className='flex items-center gap-2'>
                <GoDotFill className='text-primary-500' />
                <span className='font-bold'>Job:</span> A single execution of a transformation algorithm.
            </li>
            <li className='flex items-center gap-2'>
                <GoDotFill className='text-primary-500' />
                <span className='font-bold'>Run:</span> A collection of multiple Job executions (similar to a CI/CD pipeline).
            </li>
        </ul>

        <p className='text-sm mt-3'>
            Inspired by GitLab, Jobs are queued and executed sequentially. Runs help group multiple executions together.
        </p>
    </>);
}
