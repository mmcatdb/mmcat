import { Button } from '@heroui/react';
import { Action, type JobPayload } from '@/types/job';
import { useState } from 'react';
import { api } from '@/api';
import { toast } from 'react-toastify';
import { ConfirmationModal } from '@/components/common/tableComponents';
import { type Params, useLoaderData, useNavigate } from 'react-router-dom';
import { PageLayout } from '@/components/RootLayout';
import { type Id } from '@/types/id';
import { JobPayloadDisplay } from '@/components/job/JobPayloadDisplay';

export function ActionDetailPage() {
    const { action } = useLoaderData() as ActionLoaderData;
    const navigate = useNavigate();
    const [ isCreatingRun, setIsCreatingRun ] = useState<boolean>(false);
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ isDeleting, setIsDeleting ] = useState(false);

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

    async function handleCreateRun(actionId: Id) {
        setIsCreatingRun(true);
        const response = await api.jobs.createRun({ actionId });
        setIsCreatingRun(false);

        if (!response.status)
            toast.error('Error creating run');
        else
            toast.success('Run created successfully.');
            // console.log('New Run:', response.data);
    }

    return (
        <PageLayout>
            <h1 className='text-2xl font-bold mb-4'>{action.label}</h1>

            <p className='mb-4'>
                <span className='font-bold'>ID:</span> {action.id}
            </p>

            <div className='mb-6 space-y-2'>
                <h2 className='text-xl font-semibold'>Steps</h2>
                <StepsList payloads={action.payloads} />
            </div>

            <div className='flex space-x-4'>
                <Button
                    color='danger'
                    variant='flat'
                    onPress={() => setIsModalOpen(true)}
                    isDisabled={isDeleting}
                >
                    Delete
                </Button>
                <Button
                    color='primary'
                    variant='solid'
                    isDisabled={isCreatingRun}
                    onPress={() => handleCreateRun(action.id)}
                >
                    {isCreatingRun ? 'Creating...' : 'Create Run'}
                </Button>
            </div>

            <ConfirmationModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onConfirm={confirmDelete}
                title='Confirm Deletion?'
                message='This will permanently delete the action.'
                confirmButtonText='Yes, Delete'
                cancelButtonText='Cancel'
                confirmButtonColor='danger'
            />
        </PageLayout>
    );
}

export type ActionLoaderData = {
    action: Action;
};

ActionDetailPage.loader = async ({ params: { actionId } }: { params: Params<'actionId'> }): Promise<ActionLoaderData> => {
    if (!actionId)
        throw new Error('Action ID is required');

    const response = await api.actions.getAction({ id: actionId });

    if (!response.status)
        throw new Error('Failed to load action');

    return {
        action: Action.fromResponse(response.data),
    };
};

type StepsListProps = {
    payloads: JobPayload[];
};

/**
 * Renders a table of steps for the action.
 */
function StepsList({ payloads }: StepsListProps) {
    if (!payloads || payloads.length === 0)
        return <p className='text-gray-500'>No steps available.</p>;

    return (<>
        {payloads.map((payload, index) => (
            <div key={index} className='flex gap-4'>
                <h3 className='shrink-0 pt-1 text-xl font-bold'>{index}</h3>
                <div className='grow px-4 py-2 rounded-lg border border-default-300 bg-default-50'>
                    <JobPayloadDisplay payload={payload} />
                </div>
            </div>
        ))}
    </>);
}
