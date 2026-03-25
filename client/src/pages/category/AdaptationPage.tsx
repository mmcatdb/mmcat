import { useCallback, useEffect, useRef, useState } from 'react';
import { useLoaderData, type Params } from 'react-router-dom';
import { api } from '@/api';
import { Datasource } from '@/types/Datasource';
import { Mapping } from '@/types/mapping';
import { Category } from '@/types/schema';
import { AdaptationResultPage } from '@/components/adaptation/AdaptationResultPage';
import { CreateAdaptationPage } from '@/components/adaptation/CreateAdaptationPage';
import { Adaptation, type AdaptationResult, type AdaptationJob, adaptationJobFromResponse } from '@/components/adaptation/adaptation';
import { AdaptationSettingsPage } from '@/components/adaptation/AdaptationSettingsPage';
import { JobState } from '@/types/job';
import { AdaptationJobPage } from '@/components/adaptation/AdaptationJobPage';
import { Query } from '@/types/query';
import { type Id } from '@/types/id';

const POLLING_INTERVAL_MS = 1000;

export function AdaptationPage() {
    const loaderData = useLoaderData() as AdaptationPageData;
    const [ adaptation, setAdaptation ] = useState(loaderData.adaptation);

    const { category, datasources } = loaderData;
    const [ queries, setQueries ] = useState<Query[]>(loaderData.queries);

    const jobIntervalRef = useRef<NodeJS.Timeout>();

    const [ job, setJob ] = useState<AdaptationJob>();
    const [ inspectedResult, setInspectedResult ] = useState<AdaptationResult>();

    const updateQuery = useCallback((updated: Query) => {
        setQueries(prev => prev.map(q => q.id === updated.id ? updated : q));
    }, []);

    function tryStartPolling(adaptationId: Id) {
        if (jobIntervalRef.current)
            return;

        jobIntervalRef.current = setInterval(async () => {
            const response = await api.adaptations.pollAdaptation({ adaptationId });
            if (!response.status) {
                // TODO handle error
                console.error(response.error);
                clearInterval(jobIntervalRef.current);
                jobIntervalRef.current = undefined;
                return;
            }

            if (!response.data) {
                clearInterval(jobIntervalRef.current);
                jobIntervalRef.current = undefined;
                return;
            }

            const job = adaptationJobFromResponse(response.data, category, datasources, queries);
            if (job.state === JobState.Finished) {
                clearInterval(jobIntervalRef.current);
                jobIntervalRef.current = undefined;
            }

            setJob(job);
        }, POLLING_INTERVAL_MS);
    }

    const adaptationId = adaptation?.id;

    useEffect(() => {
        if (!adaptationId)
            return;

        tryStartPolling(adaptationId);
    // eslint-disable-next-line react-hooks/exhaustive-deps -- This ok we want it only once anyway.
    }, [ adaptationId ]);

    if (!adaptation) {
        return (
            <CreateAdaptationPage category={category} datasources={datasources} onNext={setAdaptation} />
        );
    }

    function startJob(job: AdaptationJob) {
        setJob(job);
        tryStartPolling(adaptationId!);
    }

    if (!job) {
        return (
            <AdaptationSettingsPage
                category={category}
                datasources={datasources}
                queries={queries}
                updateQuery={updateQuery}
                adaptation={adaptation}
                onNext={startJob}
            />
        );
    }

    function inspectResult(job: AdaptationJob) {
        // clearInterval(jobIntervalRef.current);
        setInspectedResult({
            processedStates: job.processedStates,
            solutions: job.solutions,
        });
    }

    if (!inspectedResult) {
        return (
            <AdaptationJobPage adaptation={adaptation} job={job} onNext={inspectResult} />
        );
    }

    function resumeJob() {
        setInspectedResult(undefined);
    }

    function restartJob() {
        setJob(undefined);
        setInspectedResult(undefined);

        clearInterval(jobIntervalRef.current);
        jobIntervalRef.current = undefined;
    }

    return (
        <AdaptationResultPage category={category} adaptation={adaptation} job={job} result={inspectedResult} queries={queries} onResume={resumeJob} onRestart={restartJob} />
    );
}

type AdaptationPageData = {
    category: Category;
    datasources: Datasource[];
    mappings: Mapping[];
    adaptation: Adaptation | undefined;
    queries: Query[];
};

AdaptationPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId' | 'queryId'> }): Promise<AdaptationPageData> => {
    if (!categoryId)
        throw new Error('Category ID is required');

    const [ categoryResponse, datasourcesResponse, mappingsResponse, adaptationsResponse ] = await Promise.all([
        api.schemas.getCategory({ id: categoryId }),
        // We want all datasources here, not just those in the category.
        api.datasources.getAllDatasources({}, {}),
        api.mappings.getAllMappingsInCategory({}, { categoryId }),
        api.adaptations.getAdaptationForCategory({ categoryId }),
    ]);
    if (!categoryResponse.status)
        throw new Error('Failed to load category info');
    if (!datasourcesResponse.status)
        throw new Error('Failed to load datasources');
    if (!mappingsResponse.status)
        throw new Error('Failed to load mappings');
    if (!adaptationsResponse.status)
        throw new Error('Failed to load adaptation');

    const datasources = datasourcesResponse.data.map(Datasource.fromResponse);

    // TODO Temp for now. If we choose to keep it, move it to the Promise.all.
    const queriesResponse = await api.queries.getQueriesInCategory({ categoryId });
    if (!queriesResponse.status)
        throw new Error('Failed to load queries');

    return {
        category: Category.fromResponse(categoryResponse.data),
        datasources,
        mappings: mappingsResponse.data.map(Mapping.fromResponse),
        adaptation: adaptationsResponse.data ? Adaptation.fromResponse(adaptationsResponse.data, datasources) : undefined,
        queries: queriesResponse.data.map(Query.fromResponse),
    };
};
