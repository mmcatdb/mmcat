import { PageLayout } from '@/components/RootLayout';
import { DatasourceType, type Datasource } from '@/types/Datasource';
import { type Category } from '@/types/schema';
import { useMemo } from 'react';
import { type AdaptationMorphism, type Adaptation } from './adaptation';
import { Button, Card, CardBody, Checkbox, Select, SelectItem } from '@heroui/react';
import { InfoBanner, InfoTooltip } from '../common';
import { useBannerState } from '@/types/utils/useBannerState';
import { type JobState } from '@/types/job';
import { type AdaptationSettingsDispatch, type AdaptationSettingsState, useAdaptationSettings } from './useAdaptationSettings';
import { DatasourceBadge } from '../datasource/DatasourceBadge';
import { getEdgeSignature } from '../category/graph/categoryGraph';
import { type Id } from '@/types/id';
import { prettyPrintInt, timeQuantity } from '@/types/utils/common';
import { JobStateLabel } from '@/pages/category/JobPage';

export type MockAdaptationJob = {
    id: Id;
    state: JobState;
    createdAt: Date;
    processedStates: number;
};

type AdaptationJobPageProps = {
    adaptation: Adaptation;
    // TODO
    job: MockAdaptationJob;
    onNext: (job: MockAdaptationJob) => void;
    /** @deprecated */
    onNextMock?: () => void;
};

export function AdaptationJobPage({ adaptation, job, onNext, onNextMock }: AdaptationJobPageProps) {
    const banner = useBannerState('adaptation-settings-page');

    function finishJob() {
        onNextMock?.();
    }

    const runningTimeMs = Date.now() - job.createdAt.getTime();

    return (
        <PageLayout>
            <div className='flex items-center gap-2 mb-4'>
                <h1 className='text-xl font-bold text-default-800'>Adaptation</h1>

                <InfoTooltip {...banner} />
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <AdaptationJobInfoInner />
            </InfoBanner>

            <h2 className='mb-2 text-lg font-semibold'>Job</h2>

            <Card>
                <CardBody className='grid grid-cols-[auto_1fr] gap-x-4'>
                    <div>State:</div>
                    <div><JobStateLabel state={job.state} /></div>

                    <div>Processed states:</div>
                    <div>{prettyPrintInt(job.processedStates)}</div>

                    <div>Running time:</div>
                    <div>{timeQuantity.prettyPrint(runningTimeMs)}</div>
                </CardBody>
            </Card>

            <div className='mt-4 flex justify-center'>
                <Button color='primary' onPress={finishJob}>
                    Finish Adaptation
                </Button>
            </div>
        </PageLayout>
    );
}

function AdaptationJobInfoInner() {
    return (<>
        <h2 className='text-lg font-semibold mb-2'>Adaptation Job</h2>
        <p>
            The adaptation process will modify the data model to better fit the selected
        </p>

        TODO
    </>);
}
