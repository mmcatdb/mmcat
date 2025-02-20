import { api } from '@/api';
import { Job, JobState } from '@/types/job';
import { useEffect, useState } from 'react';
import { Outlet, useNavigate, useParams } from 'react-router-dom';
import { Button, Tooltip } from '@nextui-org/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { LoadingPage, ReloadPage } from '../errorPages';
import { getJobStateTextStyle, getJobStatusIcon } from '@/components/icons/Icons';
import { usePreferences } from '@/components/PreferencesProvider';
import { cn } from '@/components/utils';

export function JobsPage() {
    return (
        <div>
            <Outlet />
        </div>
    );
}

export function RunsPageOverview() {
    const { theme, showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();
    const [ groupedJobs, setGroupedJobs ] = useState<Record<string, Job[]>>({});
    const [ isInitialLoad, setIsInitialLoad ] = useState(true);
    const [ error, setError ] = useState(false);

    async function fetchJobs() {
        // setError(false);
        const response = await api.jobs.getAllJobsInCategory({ categoryId: category.id });
        setIsInitialLoad(false);

        if (!response.status) {
            setError(true);
            return;
        }

        setError(false);

        const jobsFromServer = response.data.map(job => Job.fromServer(job, category));
        const grouped = groupJobsByRunId(jobsFromServer);

        // Compare states and update if needed
        setGroupedJobs(prev => {
            const hasChanges = detectChanges(prev, grouped);
            return hasChanges ? grouped : prev;
        });
    }

    // Compare old and new job groups for differences
    function detectChanges(oldGroups: Record<string, Job[]>, newGroups: Record<string, Job[]>) {
        for (const runId in newGroups) {
            const oldJobs = oldGroups[runId] || [];
            const newJobs = newGroups[runId];
            if (oldJobs.length !== newJobs.length)
                return true;

            for (let i = 0; i < newJobs.length; i++) {
                if (newJobs[i].state !== oldJobs[i]?.state)
                    return true;
            }
        }
        return false;
    }

    // Group jobs by runId
    function groupJobsByRunId(jobs: Job[]) {
        return jobs.reduce((acc, job) => {
            const runId = job.runId;
            if (!acc[runId]) 
                acc[runId] = [];
            acc[runId].push(job);
            return acc;
        }, {} as Record<string, Job[]>);
    }

    // Polling: jobs fetching periodically
    useEffect(() => {
        void fetchJobs();
        const intervalId = setInterval(fetchJobs, 2000); // Poll every 2 seconds
        return () => clearInterval(intervalId);
    }, []);

    if (isInitialLoad) 
        return <LoadingPage />;

    if (error) 
        return <ReloadPage onReload={fetchJobs} />;

    const classNameTH = cn(
        'px-4 py-3 text-left font-semibold bg-zinc-100 dark:bg-zinc-800 border-b-4',
        theme === 'dark' ? 'border-zinc-900 text-zinc-200' : 'border-zinc-300 text-zinc-700',
    );
    
    return (
        <>
            <h1 className='text-2xl font-bold mb-4'>Jobs in Runs</h1>
            <table
                className={cn(
                    'w-full border-collapse rounded-xl overflow-hidden shadow-sm',
                    theme === 'dark' ? 'border-zinc-700 bg-zinc-900' : 'border-zinc-300 bg-white',
                )}
            >
                <thead>
                    <tr>
                        {showTableIDs && <th className={classNameTH}>Run ID</th>}
                        <th className={classNameTH}>Run Label</th>
                        <th className={classNameTH}>Jobs</th>
                    </tr>
                </thead>
                <tbody>
                    {Object.entries(groupedJobs).length > 0 ? (
                        Object.entries(groupedJobs).map(([ runId, jobs ]) => (
                            <RunRow key={runId} runId={runId} jobs={jobs} />
                        ))
                    ) : (
                        <tr>
                            <td colSpan={3} className='text-center p-4 text-zinc-500 dark:text-zinc-400'>
                                No runs available.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </>
    );
    
}

function RunRow({ runId, jobs }: { runId: string, jobs: Job[] }) {
    const navigate = useNavigate();
    const { theme, showTableIDs } = usePreferences().preferences;

    // Keep only the most recent job per index
    const newestJobs = Object.values(
        jobs.reduce((acc, job) => {
            const existing = acc[job.index];
            if (!existing || new Date(job.createdAt) > new Date(existing.createdAt))
                acc[job.index] = job;
            return acc;
        }, {} as Record<number, Job>),
    );

    return (
        <tr className={cn(theme === 'dark' ? 'hover:bg-zinc-800' : 'hover:bg-zinc-100')}>
            {showTableIDs && <td className={'px-4 py-2'}>{runId}</td>}
            <td className={'px-4 py-2'}>{newestJobs[0]?.runLabel || `Run ${runId}`}</td>
            <td className={'px-4 py-2'}>
                <div className='flex items-center gap-2'>
                    {newestJobs.map(job => (
                        <Tooltip
                            key={job.id}
                            content={
                                <div>
                                    <p>
                                        <strong>ID:</strong> {job.id}
                                    </p>
                                    <p>
                                        <strong>Step:</strong> #{job.index}
                                    </p>
                                    <p>
                                        <strong>State:</strong> {job.state}
                                    </p>
                                    <p>
                                        <strong>Created At:</strong> {new Date(job.createdAt).toString()}
                                    </p>
                                </div>
                            }
                            placement='top'
                        >
                            <div
                                onClick={() => navigate(`${job.id}`)}
                                className='cursor-pointer'
                            >
                                {getJobStatusIcon(job.state)}
                            </div>
                        </Tooltip>
                    ))}
                </div>
            </td>
        </tr>
    );
}

type JobDetailStatus = {
    loading: boolean;
    error: boolean;
    polling: boolean;
};

export function JobDetailPage() {
    const { theme } = usePreferences().preferences;
    const { jobId } = useParams<{ jobId: string }>();
    const [ job, setJob ] = useState<Job | null>(null);
    const [ jobStatus, setJobStatus ] = useState<JobDetailStatus>({
        loading: true,
        error: false,
        polling: true,
    });
    const { category } = useCategoryInfo();
    const navigate = useNavigate();

    async function fetchJobDetails() {
        setJobStatus(prev => ({ ...prev, error: false }));
        const response = await api.jobs.getJob({ id: jobId });
        setJobStatus(prev => ({ ...prev, loading: false }));

        if (!response.status) {
            setJobStatus(prev => ({ ...prev, error: true }));
            return;
        }

        const fetchedJob = Job.fromServer(response.data, category);
        setJob(fetchedJob);

        if ([ JobState.Finished, JobState.Failed ].includes(fetchedJob.state)) 
            setJobStatus(prev => ({ ...prev, polling: false }));
        
    }

    const pollInterval = 3000;

    useEffect(() => {
        let intervalId: NodeJS.Timeout | null = null;

        const startPolling = () => {
            intervalId = setInterval(() => void fetchJobDetails(), pollInterval);
        };

        void fetchJobDetails();

        if (jobStatus.polling) 
            startPolling();

        return () => {
            if (intervalId) 
                clearInterval(intervalId);
        };
    }, [ jobId, jobStatus.polling ]);

    async function handleEnableJob() {
        setJobStatus(prev => ({ ...prev, polling: true }));
        const result = await api.jobs.enableJob({ id: job?.id });
        if (result.status) 
            setJob(Job.fromServer(result.data, category));
    }

    async function handleDisableJob() {
        setJobStatus(prev => ({ ...prev, polling: true }));
        const result = await api.jobs.disableJob({ id: job?.id });
        if (result.status) 
            setJob(Job.fromServer(result.data, category));
    }

    async function handleRestartJob() {
        const result = await api.jobs.createRestartedJob({ id: job?.id });
        setJobStatus(prev => ({ ...prev, polling: true }));
        if (result.status) 
            navigate(`/category/${category.id}/jobs/${result.data.id}`);
        
    }

    function renderJobStateButton(customClassName: string) {
        if (!job) 
            return null;

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

    if (jobStatus.loading) 
        return <LoadingPage />;
    if (jobStatus.error) 
        return <ReloadPage onReload={fetchJobDetails} />;

    return (
        <div className='p-4'>
            <h1 className='text-2xl font-bold mb-4'>Job Details</h1>
            {job ? (
                <div>
                    <div
                        className={cn(
                            'border rounded-lg p-4',
                            theme === 'dark' ? 'border-zinc-500' : 'border-zinc-300',
                        )}
                    >
                        <p>
                            <strong>ID:</strong> {job.id}
                        </p>
                        <p>
                            <strong>Run ID:</strong> {job.runId}
                        </p>
                        <p>
                            <strong>Index:</strong> {job.index}
                        </p>
                        <p className='my-2'>
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

                    {job.error === null ? (
                        <span></span>
                    ) : (
                        <div className='mt-5 text-red-500'>
                            <span className='font-bold'>Error: {job.error?.name}</span>
                            <div className={cn('p-4 mt-2 rounded-md text-sm border border-red-500',
                                theme === 'dark' ? 'bg-zinc-900 text-zinc-50' : 'bg-zinc-50 text-zinc-700',
                            )}>
                                {JSON.stringify(job.error?.data)}
                            </div>
                        </div>
                    )}
                    
                </div>
            ) : (
                <p>No job details available.</p>
            )}
        </div>
    );
}