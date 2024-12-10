import { api } from '@/api';
import { Job } from '@/types/job';
import { useEffect, useState } from 'react';
import { Outlet, useNavigate, useParams } from 'react-router-dom';
import { Tooltip } from '@nextui-org/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { LoadingPage, ReloadPage } from '../errorPages';
import { getJobStatusIcon } from '@/components/icons/Icons';
import { usePreferences } from '@/components/PreferencesProvider';

export function JobsPage() {
    return (
        <div>
            <Outlet />
        </div>
    );
}

export function RunsPageOverview() {
    const { showTableIDs } = usePreferences().preferences;
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

        const jobsFromServer = response.data.map((job) => Job.fromServer(job, category));
        const grouped = groupJobsByRunId(jobsFromServer);

        // Compare states and update if needed
        setGroupedJobs((prev) => {
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

    return (
        <div className='p-4'>
            <h1 className='text-2xl font-bold mb-4'>Runs</h1>
            <table className='table-auto w-full border-collapse border border-zinc-300'>
                <thead>
                    <tr>
                        {showTableIDs && <th className='border border-zinc-300 px-4 py-2 text-left'>Run ID</th>}
                        <th className='border border-zinc-300 px-4 py-2 text-left'>Run Label</th>
                        <th className='border border-zinc-300 px-4 py-2 text-left'>Jobs</th>
                    </tr>
                </thead>
                <tbody>
                    {Object.entries(groupedJobs).length > 0 ? (
                        Object.entries(groupedJobs).map(([ runId, jobs ]) => (
                            <RunRow key={runId} runId={runId} jobs={jobs} />
                        ))
                    ) : (
                        <tr>
                            <td colSpan={3} className='text-center p-4'>
                                No runs available.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
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
        <tr className='hover:bg-zinc-100'>
            {showTableIDs && <td className='border border-zinc-300 px-4 py-2'>{runId}</td>}
            <td className='border border-zinc-300 px-4 py-2'>{newestJobs[0]?.runLabel || `Run ${runId}`}</td>
            <td className='border border-zinc-300 px-4 py-2'>
                <div className='flex items-center gap-2'>
                    {newestJobs.map((job) => (
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

export function JobDetailPage() {
    const { jobId } = useParams<{ jobId: string }>();
    const [ job, setJob ] = useState<Job | null>(null);
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState(false);
    const { category } = useCategoryInfo();

    const fetchJobDetails = async () => {
        setLoading(true);
        setError(false);
        const response = await api.jobs.getJob({ id: jobId });
        setLoading(false);

        if (!response.status) {
            setError(true); return; 
        }

        setJob(Job.fromServer(response.data, category));
    };

    useEffect(() => {
        void fetchJobDetails();
    }, [ jobId ]);

    if (loading) 
        return <LoadingPage />;
    if (error) 
        return <ReloadPage onReload={fetchJobDetails} />;

    return (
        <div className='p-4'>
            <h1 className='text-2xl font-bold mb-4'>Job Details</h1>
            {job ? (
                <div className='border border-zinc-300 rounded-lg p-4'>
                    <p>
                        <strong>ID:</strong> {job.id}
                    </p>
                    <p>
                        <strong>Run ID:</strong> {job.runId}
                    </p>
                    <p>
                        <strong>Index:</strong> {job.index}
                    </p>
                    <p>
                        <strong>State:</strong> {job.state}
                    </p>
                    <p>
                        <strong>Created At:</strong> {new Date(job.createdAt).toString()}
                    </p>
                </div>
            ) : (
                <p>No job details available.</p>
            )}
        </div>
    );
}
