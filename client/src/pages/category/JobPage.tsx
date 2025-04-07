import { api } from '@/api';
import { Job, type JobFromServer, JobState } from '@/types/job';
import { useEffect, useState } from 'react';
import { type Params, useLoaderData, useNavigate, useRevalidator } from 'react-router-dom';
import { Button } from '@nextui-org/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { getJobStateTextStyle } from '@/components/icons/Icons';
import { cn } from '@/components/utils';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';

/** In ms. */
const REFRESH_INTERVAL = 3000;

export function JobPage() {
    const data = useLoaderData() as JobLoaderData;
    const { category } = useCategoryInfo();

    const [ job, setJob ] = useState(() => Job.fromServer(data.job, category));

    const revalidator = useRevalidator();

    useEffect(() => {
        if ([ JobState.Finished, JobState.Failed ].includes(job.state))
            return;

        setTimeout(() => revalidator.revalidate(), REFRESH_INTERVAL);
    }, [ job ]);

    // FIXME This does not work properly. We have to set the timeout again after the revalidation. However, we don't update the job from the server after revalidation.

    const navigate = useNavigate();

    async function handleEnableJob() {
        const result = await api.jobs.enableJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error enabling job');
            return;
        }

        setJob(Job.fromServer(result.data, category));
    }

    async function handleDisableJob() {
        const result = await api.jobs.disableJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error disabling job');
            return;
        }

        setJob(Job.fromServer(result.data, category));
    }

    async function handleRestartJob() {
        const result = await api.jobs.createRestartedJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error restarting job');
            return;
        }

        const newJob = Job.fromServer(result.data, category);
        navigate(routes.category.job.resolve({ categoryId: category.id, jobId: newJob.id }));
    }

    function renderJobStateButton(customClassName: string) {
        switch (job.state) {
        case JobState.Disabled:
            return (
                <Button
                    onClick={handleEnableJob}
                    color='success'
                    className={customClassName}
                >
                    Enable
                </Button>
            );
        case JobState.Ready:
            return (
                <Button
                    onClick={handleDisableJob}
                    color='warning'
                    className={customClassName}
                >
                    Disable
                </Button>
            );
        case JobState.Finished:
        case JobState.Failed:
            return (
                <Button
                    onClick={handleRestartJob}
                    color='primary'
                    className={customClassName}
                >
                    Restart
                </Button>
            );
        default:
            return null;
        }
    }

    return (
        <div className='p-4'>
            <h1 className='text-2xl font-bold mb-4'>Job Details</h1>

            <div
                className='border rounded-lg p-4 border-default-300 bg-default-50'
            >
                <p className='mb-1'>
                    <strong>ID:</strong> {job.id}
                </p>
                <p className='mb-1'>
                    <strong>Run ID:</strong> {job.runId}
                </p>
                <p className='mb-1'>
                    <strong>Index:</strong> {job.index}
                </p>
                <p className='mb-1'>
                    <strong>State:</strong>
                    <span
                        className={cn(
                            'm-2 px-3 py-1 rounded-full font-semibold',
                            getJobStateTextStyle(job.state),
                        )}
                    >
                        {job.state}
                    </span>
                </p>
                <p>
                    <strong>Created At:</strong> {new Date(job.createdAt).toString()}
                </p>
            </div>

            {renderJobStateButton('mt-5')}

            {job.error && (
                <div className='mt-5 text-danger-400'>
                    <span className='font-bold'>Error: {job.error?.name}</span>
                    <div className='p-4 mt-2 rounded-lg text-sm border border-danger-400 bg-default-50 text-default-900'>
                        {JSON.stringify(job.error?.data)}
                    </div>
                </div>
            )}
        </div>
    );
}

JobPage.loader = jobLoader;

export type JobLoaderData = {
    job: JobFromServer;
};

async function jobLoader({ params: { jobId } }: { params: Params<'jobId'> }): Promise<JobLoaderData> {
    if (!jobId)
        throw new Error('Job ID is required');

    const response = await api.jobs.getJob({ id: jobId });
    if (!response.status)
        throw new Error('Failed to load job info');

    return {
        job: response.data,
    };
}
