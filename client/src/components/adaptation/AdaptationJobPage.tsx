import { PageLayout } from '@/components/RootLayout';
import { type Adaptation, type MockAdaptationJob } from './adaptation';
import { Button, Card, CardBody } from '@heroui/react';
import { InfoBanner, InfoTooltip } from '../common/components';
import { useBannerState } from '@/types/utils/useBannerState';
import { prettyPrintDouble, prettyPrintInt, timeQuantity } from '@/types/utils/common';
import { JobStateLabel } from '@/pages/category/JobPage';
import { XMarkIcon } from '@heroicons/react/24/outline';

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
                <CardBody className='grid grid-cols-2 gap-4'>
                    <div>
                        <div className='grid grid-cols-[auto_1fr] gap-x-4'>
                            <div>State:</div>
                            <div><JobStateLabel state={job.state} /></div>

                            <div>Processed states:</div>
                            <div>{prettyPrintInt(job.processedStates)}</div>

                            <div>Running time:</div>
                            <div>{timeQuantity.prettyPrint(runningTimeMs)}</div>
                        </div>
                    </div>

                    {job.solutions.length !== 0 && (
                        <div>
                            <div className='flex gap-x-8'>
                                <div>
                                    <div className='font-medium'>Best solutions</div>
                                    <div>Speed up [<XMarkIcon className='inline size-4' />]:</div>
                                    <div>Price [DB hits]:</div>
                                </div>

                                {job.solutions.map((solution, index) => (
                                    <div key={index} className='font-semibold text-end'>
                                        <div>#{index + 1}</div>
                                        <div>{prettyPrintDouble(solution.speedup)}</div>
                                        <div>{prettyPrintDouble(solution.price)}</div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
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
