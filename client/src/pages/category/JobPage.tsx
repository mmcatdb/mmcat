import { api } from '@/api';
import { Job, JobState } from '@/types/job';
import { type Dispatch, type FunctionComponent, type SVGProps, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '@heroui/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';
import { type SchemaCategoryInfo } from '@/types/schema';
import { LoadingPage, ReloadPage } from '../errorPages';
import { twJoin, twMerge } from 'tailwind-merge';
import { PlayCircleIcon } from '@heroicons/react/24/outline';
import { CheckCircleIcon, PauseCircleIcon, XCircleIcon, EllipsisHorizontalCircleIcon, StopCircleIcon } from '@heroicons/react/24/outline';

/** In milliseconds. */
const REFRESH_INTERVAL_MS = 1000;
const TERMINAL_STATES = [ JobState.Finished, JobState.Failed ];

export function JobPage() {
    const { category } = useCategoryInfo();
    const { jobId } = useParams();
    const [ job, setJob ] = useState<Job>();
    const [ error, setError ] = useState(false);

    const intervalRef = useRef<NodeJS.Timeout>();

    async function fetchJob() {
        const response = await api.jobs.getJob({ id: jobId! });
        if (!response.status) {
            setError(true);
            if (intervalRef.current)
                clearInterval(intervalRef.current);

            return;
        }

        setError(false);
        const newJob = Job.fromResponse(response.data, category);
        setJob(newJob);

        if (TERMINAL_STATES.includes(newJob.state) && intervalRef.current)
            clearInterval(intervalRef.current);
    }

    useEffect(() => {
        void fetchJob();

        intervalRef.current = setInterval(() => {
            void fetchJob();
        }, REFRESH_INTERVAL_MS);

        return () => {
            if (intervalRef.current)
                clearInterval(intervalRef.current);
        };
    // Force the page to refetch the job navigation to the same page.
    }, [ jobId ]);

    if (!job)
        return <LoadingPage />;

    if (error) {
        return <ReloadPage onReload={() => {
            // This will fetch the job only once, not repeatedly. Replace by a better solution once Tanstack query + router is used.
            void fetchJob();
        }} />;
    }

    return (
        <div className='p-4'>
            <h1 className='text-2xl font-bold mb-4'>Job Details</h1>

            <div className='border rounded-lg p-4 border-default-300 bg-default-50'>
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
                    <span className={twMerge('m-2 px-3 py-1 rounded-full font-semibold', jobStateStyles[job.state].bg)}>
                        {job.state}
                    </span>
                </p>
                <p>
                    <strong>Created At:</strong> {new Date(job.createdAt).toString()}
                </p>
            </div>

            <JobStateButton job={job} setJob={setJob} category={category} className='mt-5' />

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

function JobStateButton({ job, setJob, category, className }: { job: Job, setJob: Dispatch<Job>, category: SchemaCategoryInfo, className?: string }) {
    const navigate = useNavigate();

    async function handleEnableJob() {
        const result = await api.jobs.enableJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error enabling job');
            return;
        }

        setJob(Job.fromResponse(result.data, category));
    }

    async function handleDisableJob() {
        const result = await api.jobs.disableJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error disabling job');
            return;
        }

        setJob(Job.fromResponse(result.data, category));
    }

    async function handleRestartJob() {
        const result = await api.jobs.createRestartedJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error restarting job');
            return;
        }

        const newJob = Job.fromResponse(result.data, category);
        navigate(routes.category.job.resolve({ categoryId: category.id, jobId: newJob.id }));
    }

    if (job.state === JobState.Disabled) {
        return (
            <Button color='success' onPress={handleEnableJob} className={className}>
                Enable
            </Button>
        );
    }

    if (job.state === JobState.Ready) {
        return (
            <Button color='warning' onPress={handleDisableJob} className={className}>
                Disable
            </Button>
        );
    }

    if (TERMINAL_STATES.includes(job.state)) {
        return (
            <Button color='primary' onPress={handleRestartJob} className={className}>
                Restart
            </Button>
        );
    }

    return null;
}

/**
 * Returns the appropriate icon for a job's status.
 */
export function JobStateIcon({ state }: {state: JobState }) {
    const styles = jobStateStyles[state];
    return (
        <styles.icon className={twJoin(
            'w-8 h-8',
            styles.color,
            state === JobState.Running && 'animate-spin',
        )} />
    );
}

/**
 * Styling configuration for job states, mapping each state to color and background.
 */
const jobStateStyles: Record<JobState, { color: string, bg: string, icon: FunctionComponent<SVGProps<SVGSVGElement>> }> = {
    [JobState.Disabled]: { color: 'text-default-400', bg: 'bg-default-400', icon: StopCircleIcon },
    [JobState.Ready]: { color: 'text-primary-400', bg: 'bg-primary-400', icon: PlayCircleIcon },
    [JobState.Running]: { color: 'text-primary-500', bg: 'bg-primary-500', icon: EllipsisHorizontalCircleIcon },
    [JobState.Waiting]: { color: 'text-warning-500', bg: 'bg-yellow-500', icon: PauseCircleIcon },
    [JobState.Finished]: { color: 'text-success-500', bg: 'bg-success-400', icon: CheckCircleIcon },
    [JobState.Failed]: { color: 'text-danger-400', bg: 'bg-danger-400', icon: XCircleIcon },
};
