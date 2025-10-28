import { api } from '@/api';
import { Job } from '@/types/job';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Chip, Tooltip } from '@heroui/react';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { LoadingPage, ReloadPage } from '../errorPages';
import { JobStateIcon } from './JobPage';
import { usePreferences } from '@/components/PreferencesProvider';
import { GoDotFill } from 'react-icons/go';
import { useBannerState } from '@/types/utils/useBannerState';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { routes } from '@/routes/routes';
import { InfoBanner } from '@/components/common';
import { EmptyState } from '@/components/TableCommon';
import { PageLayout } from '@/components/RootLayout';
import { type Id } from '@/types/id';

/** In ms. */
const REFRESH_INTERVAL_MS = 3000;

export function JobsPage() {
    const { showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();
    const [ groupedJobs, setGroupedJobs ] = useState<Record<Id, Job[]>>();
    const [ error, setError ] = useState(false);
    const { isVisible, dismissBanner, restoreBanner } = useBannerState('jobs-page');

    async function fetchJobs() {
        const response = await api.jobs.getAllJobsInCategory({ categoryId: category.id });
        if (!response.status) {
            setError(true);
            return;
        }

        setError(false);

        const jobsResponse = response.data.map(job => Job.fromResponse(job, category));
        const grouped = groupJobsByRunId(jobsResponse);

        // Compare states and update if needed
        setGroupedJobs(rawPrev => {
            const prev = rawPrev ?? {};
            const hasChanges = detectChanges(prev, grouped);
            return hasChanges ? grouped : prev;
        });
    }

    // Polling: jobs fetching periodically
    useEffect(() => {
        void fetchJobs();
        const intervalId = setInterval(() => {
            void fetchJobs();
        }, REFRESH_INTERVAL_MS);
        return () => clearInterval(intervalId);
    }, []);

    if (!groupedJobs)
        return <LoadingPage />;

    if (error) {
        return (
            <ReloadPage onReload={fetchJobs} />
        );
    }

    const classNameTH = 'px-4 py-3 text-left font-semibold bg-default-100 border-b border-default-300 text-default-800';

    return (
        <PageLayout>
            <div className='mb-4 flex items-center gap-2'>
                <h1 className='text-xl font-semibold'>Jobs in Runs</h1>
                <Tooltip content={isVisible ? 'Hide info' : 'Show info'}>
                    <button
                        onClick={isVisible ? dismissBanner : restoreBanner}
                        className='text-primary-500 hover:text-primary-700 transition'
                    >
                        <IoInformationCircleOutline className='size-6' />
                    </button>
                </Tooltip>
            </div>

            {isVisible && <JobInfoBanner className='mb-6' dismissBanner={dismissBanner} />}

            {/* No HeroUI table here, because of grouping functionality. */}
            {Object.entries(groupedJobs).length > 0 ? (
                <table className='w-full border-collapse rounded-xl overflow-hidden shadow-xs  bg-default-50'>
                    <thead>
                        <tr>
                            {showTableIDs && <th className={classNameTH}>Run ID</th>}
                            <th className={classNameTH}>Run Label</th>
                            <th className={classNameTH}>Jobs</th>
                        </tr>
                    </thead>
                    <tbody>
                        {Object.entries(groupedJobs).length > 0 && (
                            Object.entries(groupedJobs).map(([ runId, jobs ]) => (
                                <RunRow key={runId} runId={runId} jobs={jobs} />
                            ))
                        )}
                    </tbody>
                </table>
            ) : (
                <EmptyState
                    message='No runs available yet. First create an Action.'
                    buttonText='Go to Actions Page'
                    to={routes.category.actions.list.resolve({ categoryId: category.id })}
                />
            )}
        </PageLayout>
    );
}

/**
 * Compare old and new job groups for differences.
*/
function detectChanges(oldGroups: Record<Id, Job[]>, newGroups: Record<Id, Job[]>) {
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
    }, {} as Record<Id, Job[]>);
}

function RunRow({ runId, jobs }: { runId: Id, jobs: Job[] }) {
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
            {showTableIDs && (
                <td className='px-4 py-2'>
                    {runId}
                </td>
            )}
            <td className='px-4 py-2'>
                {newestJobs[0]?.runLabel || `Run ${runId}`}
            </td>
            <td className='px-4 py-2'>
                <div className='flex  gap-2'>
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
                            <Link to={routes.category.job.resolve({ categoryId: job.categoryId, jobId: job.id })}>
                                <JobStateIcon state={job.state} />
                            </Link>
                        </Tooltip>
                    ))}
                </div>
            </td>
        </tr>
    );
}

type JobInfoBannerProps = {
    className?: string;
    dismissBanner: () => void;
};

export function JobInfoBanner({ className, dismissBanner }: JobInfoBannerProps) {
    const { category } = useCategoryInfo();

    return (
        <InfoBanner className={className} dismissBanner={dismissBanner}>
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
                    <strong>Manage Jobs:</strong> Click a circle in the <em>Jobs</em> column of a Run or hover to see details.
                </li>
                <li className='flex items-center gap-2'>
                    <GoDotFill className='text-primary-500' />

                    <strong>Create a New Run & Jobs:</strong>
                    Go to the
                    <span>
                        <Link
                            to={routes.category.actions.list.resolve({ categoryId: category.id })}
                            className='text-primary-500 hover:underline'
                        >
                            Actions page
                        </Link>
                        .
                    </span>
                </li>
            </ul>
        </InfoBanner>
    );
}
