import { Button, Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@nextui-org/react';
import { Action, ActionType, type JobPayload } from '@/types/action';
import { useState } from 'react';
import { api } from '@/api';
import { toast } from 'react-toastify';
import { ConfirmationModal } from '@/components/TableCommon';
import { Link, type Params, useLoaderData, useNavigate } from 'react-router-dom';
import { routes } from '@/routes/routes';

export function ActionPage() {
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
            console.log('New Run:', response.data);
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
                <StepsTable payloads={action.payloads} categoryId={action.categoryId} />
            </div>

            <div className='flex space-x-4'>
                <Button
                    color='primary'
                    variant='solid'
                    isDisabled={isCreatingRun}
                    onPress={() => handleCreateRun(action.id)}
                >
                    {isCreatingRun ? 'Creating...' : 'Create Run'}
                </Button>
                <Button
                    color='danger'
                    variant='bordered'
                    onPress={openModal}
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
                message='This will permanently delete the action.'
                confirmButtonText='Yes, Delete'
                cancelButtonText='Cancel'
                confirmButtonColor='danger'
            />
        </div>
    );
}

ActionPage.loader = actionLoader;

export type ActionLoaderData = {
    action: Action;
};

async function actionLoader({ params: { actionId } }: { params: Params<'actionId'> }): Promise<ActionLoaderData> {
    if (!actionId)
        throw new Error('Action ID is required');

    const response = await api.actions.getAction({ id: actionId });

    if (!response.status)
        throw new Error('Failed to load action');

    return {
        action: Action.fromServer(response.data),
    };
}

type StepsTableProps = {
    payloads: JobPayload[];
    categoryId: string;
}

function StepsTable({ payloads, categoryId }: StepsTableProps) {
    if (!payloads || payloads.length === 0)
        return <p className='text-gray-500'>No steps available.</p>;

    return (
        <Table aria-label='Steps table'>
            <TableHeader>
                <TableColumn>Index</TableColumn>
                <TableColumn>Type</TableColumn>
                <TableColumn>Datasource</TableColumn>
                <TableColumn>Mappings</TableColumn>
            </TableHeader>
            <TableBody>
                {payloads.map((payload, index) => {
                    const datasourceElement = renderDatasourceElement({ payload, categoryId });
                    const mappings = renderMappings(payload);

                    return (
                        <TableRow key={index}>
                            <TableCell>{index}</TableCell>
                            <TableCell>{payload.type}</TableCell>
                            <TableCell>{datasourceElement}</TableCell>
                            <TableCell>{mappings}</TableCell>
                        </TableRow>
                    );
                })}
            </TableBody>
        </Table>
    );
}

type renderDatasourceElementProps = {
    payload: JobPayload;
    categoryId: string;
}

function renderDatasourceElement({ payload, categoryId }: renderDatasourceElementProps) {
    if (payload.type === ActionType.ModelToCategory || payload.type === ActionType.CategoryToModel) {
        if (payload.datasource) {
            return (
                <Link
                    to={routes.categories + `/${categoryId}/datasources/${payload.datasource.id}`}
                    className='text-primary-500 hover:underline'
                >
                    {payload.datasource.label}
                </Link>
            );
        }
        return <span>N/A</span>;
    }

    if (payload.type === ActionType.RSDToCategory) {
        return (
            <div className='space-y-1'>
                {payload.datasources?.map(ds => (
                    <Link
                        key={ds.id}
                        to={routes.categories + `/${categoryId}/datasources/${ds.id}`}
                        className='text-primary-500 hover:underline'
                    >
                        {ds.label}
                    </Link>
                )) || <span>N/A</span>}
            </div>
        );
    }

    return <span>N/A</span>;
}

function renderMappings(payload : JobPayload) {
    if (
        payload.type === ActionType.ModelToCategory ||
        payload.type === ActionType.CategoryToModel
    )
        return payload.mappings?.map(m => m.kindName).join(', ') || 'N/A';

    return 'N/A';
}
