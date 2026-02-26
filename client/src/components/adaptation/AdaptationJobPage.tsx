import { PageLayout } from '@/components/RootLayout';
import { type Adaptation, type MockAdaptationJob } from './adaptation';
import { Button, Card, CardBody } from '@heroui/react';
import { InfoBanner, InfoTooltip } from '../common/components';
import { useBannerState } from '@/types/utils/useBannerState';
import { prettyPrintDouble, prettyPrintInt, timeQuantity } from '@/types/utils/common';
import { JobStateLabel } from '@/pages/category/JobPage';

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

    // This is a mock - replace with real job data when available.
    // eslint-disable-next-line react-hooks/purity
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
                                    <div>Speed-up:</div>
                                    <div>Price [DB hits]:</div>
                                </div>

                                {job.solutions.map((solution, index) => (
                                    <div key={index} className='font-semibold text-end'>
                                        <div>#{index + 1}</div>
                                        <div>{prettyPrintDouble(100 * solution.speedup) + ' %'}</div>
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
                    Inspect Results
                </Button>
            </div>
        </PageLayout>
    );
}

function AdaptationJobInfoInner() {
    return (<>
        <h2>Monitoring the Adaptation</h2>

        <p>
            The advisor is actively exploring the search space using MCTS. Because the number of possible configurations can be extremely large, the process is designed to produce meaningful intermediate results long before the search completes.
        </p>

        <ul>
            <li>
                <span className='font-bold'>Live exploration:</span> The advisor reports processed states and continuously updates the best solutions found so far.
            </li>

            <li>
                <span className='font-bold'>Early stopping:</span> You can finish the adaptation at any moment. The current best solutions are fully usable and will be taken as the final result.
            </li>

            <li>
                <span className='font-bold'>Non-blocking workflow:</span> The job runs on the server; you can leave the page and return anytime to check progress.
            </li>
        </ul>

        <p>
            Stopping early is often practical - even a partially explored search space can yield high-quality recommendations.
        </p>
    </>);
}
