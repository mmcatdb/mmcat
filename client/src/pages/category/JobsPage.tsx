import { api } from '@/api';
import { Job } from '@/types/job';
import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Tooltip } from '@nextui-org/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { LoadingPage, ReloadPage } from '../errorPages';
import { getJobStatusIcon } from '@/components/icons/Icons';

export function JobsPage() {
    return (
        <div>
            <Outlet />
        </div>
    );
}

export function RunsPageOverview() {
    const { category } = useCategoryInfo();
    const [ groupedJobs, setGroupedJobs ] = useState<Record<string, Job[]>>({});
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState(false);

    async function fetchJobs() {
        setLoading(true);
        setError(false);
        const response = await api.jobs.getAllJobsInCategory({ categoryId: category.id });
        setLoading(false);

        if (!response.status) {
            setError(true);
            toast.error('Failed to fetch jobs.');
            return;
        }

        // Group jobs by `runId`
        const jobsFromServer = response.data.map((job) => Job.fromServer(job, category));
        const grouped = jobsFromServer.reduce((acc, job) => {
            const runId = job.runId;
            if (!acc[runId]) 
                acc[runId] = [];
            acc[runId].push(job);
            return acc;
        }, {} as Record<string, Job[]>);

        setGroupedJobs(grouped);
    }

    useEffect(() => {
        void fetchJobs();
    }, []);

    if (loading) 
        return <LoadingPage />;
    if (error) 
        return <ReloadPage onReload={fetchJobs} />;

    return (
        <div className='p-4'>
            <h1 className='text-2xl font-bold mb-4'>Runs</h1>
            {Object.entries(groupedJobs).length > 0 ? (
                Object.entries(groupedJobs).map(([ runId, jobs ]) => (
                    <RunDisplay key={runId} runId={runId} jobs={jobs} />
                ))
            ) : (
                <p>No runs available.</p>
            )}
        </div>
    );
}

function RunDisplay({ runId, jobs }: { runId: string, jobs: Job[] }) {
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
        <div className='mb-6'>
            <div className='flex items-center mb-2'>
                <h2 className='text-lg font-semibold flex-grow'>
                    {newestJobs[0]?.runLabel || `Run ${runId}`}
                </h2>
            </div>
            <div className='flex items-center gap-2'>
                {newestJobs.map((job) => (
                    <Tooltip
                        key={job.id}
                        content={
                            <div>
                                <p><strong>ID:</strong> {job.id}</p>
                                <p><strong>Step:</strong> #{job.index}</p>
                                {/* <p><strong>Label:</strong> {job.label}</p> */}
                                <p><strong>State:</strong> {job.state}</p>
                                <p><strong>Created At:</strong> {new Date(job.createdAt).toLocaleString()}</p>
                            </div>
                        }
                        placement='top'
                    >
                        <div>{getJobStatusIcon(job.state)}</div>
                    </Tooltip>
                ))}
            </div>
        </div>
    );
}
