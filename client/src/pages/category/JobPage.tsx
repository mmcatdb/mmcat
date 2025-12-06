import { api } from '@/api';
import { Job, JobState } from '@/types/job';
import { type Dispatch, type FunctionComponent, type SVGProps, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button } from '@heroui/react';
import { useCategoryInfo } from '@/components/context/CategoryInfoProvider';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';
import { type SchemaCategoryInfo } from '@/types/schema';
import { LoadingPage, ReloadPage } from '../errorPages';
import { cn } from '@/components/common/utils';
import { PlayCircleIcon } from '@heroicons/react/24/outline';
import { CheckCircleIcon, PauseCircleIcon, XCircleIcon, EllipsisHorizontalCircleIcon, StopCircleIcon } from '@heroicons/react/24/outline';
import { PageLayout } from '@/components/RootLayout';
import { JobPayloadDisplay } from '@/components/job/JobPayloadDisplay';

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
        return (
            // This will fetch the job only once, not repeatedly. Replace by a better solution once Tanstack query + router is used.
            <ReloadPage onReload={fetchJob} />
        );
    }

    return (
        <PageLayout className='flex flex-col gap-4'>
            <h1 className='text-2xl font-bold'>Job Details</h1>

            <div className='px-4 py-2 rounded-lg border border-default-300 bg-default-50'>
                <p className='mb-1'>
                    <span className='font-bold'>ID:</span> {job.id}
                </p>
                <p className='mb-1'>
                    <span className='font-bold'>Run ID:</span> {job.runId}
                </p>
                <p className='mb-1'>
                    <span className='font-bold'>Index:</span> {job.index}
                </p>
                <p className='mb-1'>
                    <span className='font-bold'>State:</span>
                    <span className={cn('m-2 px-3 py-1 rounded-full font-semibold', jobStateStyles[job.state].bg)}>
                        {job.state}
                    </span>
                </p>
                <p>
                    <span className='font-bold'>Created At:</span> {new Date(job.createdAt).toString()}
                </p>
            </div>

            <div className='flex justify-end'>
                <JobStateButton job={job} setJob={setJob} category={category} />
            </div>

            {job.error && (
                <div>
                    <h2 className='font-bold text-danger-400'>Error: {job.error?.name}</h2>
                    <div className='px-4 py-2 mt-2 rounded-lg text-sm border border-danger-400 bg-default-50'>
                        {JSON.stringify(job.error?.data)}
                    </div>
                </div>
            )}

            <div>
                <h2 className='font-bold'>Payload</h2>
                <div className='mt-2 px-4 py-2 rounded-lg border border-default-300 bg-default-50'>
                    <JobPayloadDisplay payload={job.payload} />
                </div>
            </div>

            {/* TODO display result */}
        </PageLayout>
    );
}

function JobStateButton({ job, setJob, category, className }: { job: Job, setJob: Dispatch<Job>, category: SchemaCategoryInfo, className?: string }) {
    const navigate = useNavigate();

    async function enableJob() {
        const result = await api.jobs.enableJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error enabling job');
            return;
        }

        setJob(Job.fromResponse(result.data, category));
    }

    async function disableJob() {
        const result = await api.jobs.disableJob({ id: job?.id });
        if (!result.status) {
            toast.error('Error disabling job');
            return;
        }

        setJob(Job.fromResponse(result.data, category));
    }

    async function restartJob() {
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
            <Button color='success' onPress={enableJob} className={className}>
                Enable
            </Button>
        );
    }

    if (job.state === JobState.Ready) {
        return (
            <Button color='warning' onPress={disableJob} className={className}>
                Disable
            </Button>
        );
    }

    if (TERMINAL_STATES.includes(job.state)) {
        return (
            <Button color='primary' onPress={restartJob} className={className}>
                Restart
            </Button>
        );
    }

    return null;
}

/**
 * Returns the appropriate icon for a job's status.
 */
export function JobStateIcon({ state }: { state: JobState }) {
    const styles = jobStateStyles[state];
    return (
        <styles.icon className={cn(
            'size-8',
            styles.color,
            state === JobState.Running && 'animate-spin',
        )} />
    );
}

export function JobStateLabel({ state }: { state: JobState }) {
    const styles = jobStateStyles[state];
    return (
        <span className={cn('font-semibold', styles.color)}>
            {state}
        </span>
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
