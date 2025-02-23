import { api } from '@/api';
import { Job } from '@/types/job';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Tooltip } from '@nextui-org/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { LoadingPage, ReloadPage } from '../errorPages';
import { getJobStatusIcon } from '@/components/icons/Icons';
import { usePreferences } from '@/components/PreferencesProvider';
import { cn } from '@/components/utils';

/** In ms. */
const REFRESH_INTERVAL = 3000;

export function JobsPage() {
    const { theme, showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();
    const [ groupedJobs, setGroupedJobs ] = useState<Record<string, Job[]>>();
    const [ error, setError ] = useState(false);

    // FIXME: Use loader instead.
    async function fetchJobs() {
        const response = await api.jobs.getAllJobsInCategory({ categoryId: category.id });
        if (!response.status) {
            setError(true);
            return;
        }

        setError(false);

        const jobsFromServer = response.data.map(job => Job.fromServer(job, category));
        const grouped = groupJobsByRunId(jobsFromServer);

        // Compare states and update if needed
        setGroupedJobs(prev => {
            const hasChanges = detectChanges(prev ?? {}, grouped);
            return hasChanges ? grouped : prev;
        });
    }

    // Polling: jobs fetching periodically
    useEffect(() => {
        void fetchJobs();
        const intervalId = setInterval(fetchJobs, REFRESH_INTERVAL);
        return () => clearInterval(intervalId);
    }, []);

    if (!groupedJobs)
        return <LoadingPage />;

    if (error)
        return <ReloadPage onReload={fetchJobs} />;

    const classNameTH = cn(
        'px-4 py-3 text-left font-semibold bg-zinc-100 dark:bg-zinc-800 border-b-4',
        theme === 'dark' ? 'border-zinc-900 text-zinc-200' : 'border-zinc-300 text-zinc-700',
    );

    return (<>
        <h1 className='text-xl font-bold mb-8'>Jobs in Runs</h1>
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
    </>);
}

/** Compare old and new job groups for differences.  */
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

/** Group jobs by runId.  */
function groupJobsByRunId(jobs: Job[]) {
    return jobs.reduce((acc, job) => {
        const runId = job.runId;
        if (!acc[runId])
            acc[runId] = [];
        acc[runId].push(job);
        return acc;
    }, {} as Record<string, Job[]>);
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
