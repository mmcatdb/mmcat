import { useRef, useState } from 'react';
import { useLoaderData, type Params } from 'react-router-dom';
import { api } from '@/api';
import { Datasource } from '@/types/Datasource';
import { Mapping } from '@/types/mapping';
import { Category } from '@/types/schema';
import { AdaptationResultPage } from '@/components/adaptation/AdaptationResultPage';
import { CreateAdaptationPage } from '@/components/adaptation/CreateAdaptationPage';
import { Adaptation, type AdaptationResult, adaptationResultFromResponse, mockAdaptationResultResponse } from '@/components/adaptation/adaptation';
import { AdaptationSettingsPage } from '@/components/adaptation/AdaptationSettingsPage';
import { JobState, type Job } from '@/types/job';
import { AdaptationJobPage, type MockAdaptationJob } from '@/components/adaptation/AdaptationJobPage';
import { v4 } from 'uuid';
import { Query } from '@/types/query';

export function AdaptationPage() {
    const loaderData = useLoaderData() as AdaptationPageData;
    const [ state, setState ] = useState(createState(loaderData));
    /** @deprecated */
    const [ mockJob, setMockJob ] = useState<MockAdaptationJob>();
    const jobIntervalRef = useRef<NodeJS.Timeout>();
    /** @deprecated */
    const [ mockResult, setMockResult ] = useState<AdaptationResult>();

    const { category, datasources, queries } = loaderData;

    if (!state.adaptation) {
        return (
            <CreateAdaptationPage category={category} datasources={datasources} onNext={adaptation => setState(prev => ({ ...prev, adaptation }))} />
        );
    }

    function startMockJob() {
        const job: MockAdaptationJob = {
            id: v4(),
            state: JobState.Running,
            createdAt: new Date(Date.now()),
            processedStates: 0,
        };
        setMockJob(job);

        jobIntervalRef.current = setInterval(() => {
            setMockJob(prev => prev ? { ...prev, processedStates: prev.processedStates + Math.round(30 + Math.random() * 96) } : undefined);
        }, 1000);
    }

    if (!mockJob) {
        return (
            <AdaptationSettingsPage category={category} datasources={datasources} adaptation={state.adaptation} onNext={job => setState(prev => ({ ...prev, job }))} onNextMock={startMockJob} />
        );
    }

    function finishMockJob() {
        if (jobIntervalRef.current)
            clearInterval(jobIntervalRef.current);

        setMockResult(adaptationResultFromResponse(mockAdaptationResultResponse(state.adaptation!, datasources), datasources));
    }

    if (!mockResult) {
        return (
            <AdaptationJobPage adaptation={state.adaptation} job={mockJob} onNext={() => {}} onNextMock={finishMockJob} />
        );
    }

    return (
        <AdaptationResultPage category={category} adaptation={state.adaptation} result={mockResult} queries={queries} />
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
        api.datasources.getAllDatasources({}, { categoryId }),
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

type AdaptationPageState = {
    adaptation?: Adaptation;
    job?: Job;
};

function createState({ adaptation }: AdaptationPageData): AdaptationPageState {
    return {
        adaptation,
        // TODO job
    };
}
