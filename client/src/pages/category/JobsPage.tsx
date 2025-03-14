import { api } from '@/api';
import { Job } from '@/types/job';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Chip, Tooltip } from '@nextui-org/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { LoadingPage, ReloadPage } from '../errorPages';
import { getJobStatusIcon } from '@/components/icons/Icons';
import { usePreferences } from '@/components/PreferencesProvider';
import { cn } from '@/components/utils';
import { HiXMark } from 'react-icons/hi2';
import { GoDotFill } from 'react-icons/go';

/** In ms. */
const REFRESH_INTERVAL = 3000;

export function JobsPage() {
    const { showTableIDs } = usePreferences().preferences;
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
        'px-4 py-3 text-left font-semibold bg-default-100 border-b border-default-300 text-default-800',
    );

    return (<>
        <h1 className='text-xl font-bold mb-8'>Jobs in Runs</h1>
        <JobInfoBanner className='mb-8' />
        {/* no NextUI (HeroUI) table here, because of grouping functionality */}
        <table
            className={cn(
                'w-full border-collapse rounded-xl overflow-hidden shadow-sm  bg-default-50',
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
                        <td colSpan={3} className='text-center p-4 text-default-500'>
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
    const { showTableIDs } = usePreferences().preferences;

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
        <tr className='hover:bg-default-100'>
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

type JobInfoBannerProps = {
    className?: string;
};

export function JobInfoBanner({ className }: JobInfoBannerProps) {
    // const { preferences, setPreferences } = usePreferences();
    // const [ isVisible, setIsVisible ] = useState(!preferences.dismissedJobsGuide);
    const navigate = useNavigate();
    const { categoryId } = useParams();

    // function handleClose() {
    //     setIsVisible(false);
    //     setPreferences({ ...preferences, dismissedJobsGuide: true });
    // }

    // if (!isVisible) 
    //     return null;

    return (
        <div className={cn('relative bg-default-50 text-default-900 p-4 rounded-lg border border-default-300', className)}>
            <button 
                // onClick={handleClose} 
                className='absolute top-2 right-2 text-default-500 hover:text-default-700 transition'
            >
                <HiXMark className='w-5 h-5' />
            </button>

            <h2 className='text-lg font-semibold mb-2'>Understanding Jobs & Runs</h2>
            <p className='text-sm'>
                A <strong>Job</strong> is a single execution of a transformation algorithm, while a <strong>Run</strong> is a group of related Jobs processed together.
            </p>

            <ul className='mt-3 text-sm space-y-2'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Job:</strong> Executes a transformation (e.g., importing/exporting data).
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Run:</strong> A batch of Jobs executed sequentially.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <strong>Status:</strong> Jobs can be <Chip size='sm'>Ready</Chip><Chip size='sm'>Running</Chip><Chip size='sm'>Finished</Chip><Chip size='sm'>Failed</Chip> or <Chip size='sm'>Disabled</Chip>.
                </li>
            </ul>

            <p className='text-sm mt-3'>
                Jobs run in order, and Runs help organize batch processing. Inspired by GitLab pipelines.
            </p>

            {/* Hint Section */}
            <h3 className='font-semibold mb-1 mt-4'>Next Steps</h3>
            <ul className='space-y-1 text-sm'>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span><strong>Manage Jobs:</strong> Click a circle in the <em>Jobs</em> column of a Run or hover to see details.</span>
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />
                    <span><strong>Create a New Run & Jobs:</strong> Go to the <button
                        onClick={() => navigate(`/category/${categoryId}/actions`)}
                        className='text-primary-500 hover:underline'
                    >
                            Actions page
                    </button>.</span>
                </li>
            </ul>
        </div>
    );
}
