import { PageLayout } from '@/components/RootLayout';
import { type Datasource } from '@/types/Datasource';
import { type Category } from '@/types/schema';
import { useState } from 'react';
import { Adaptation } from './adaptation';
import { api } from '@/api';
import { InfoBanner, InfoTooltip, SpinnerButton } from '../common/components';
import { useBannerState } from '@/types/utils/useBannerState';
import { toast } from 'react-toastify';
import { FeatureCard } from '@/pages/HomePage';
import { ArrowPathIcon } from '@heroicons/react/24/solid';

type CreateAdaptationPageProps = {
    category: Category;
    datasources: Datasource[];
    onNext: (adaptation: Adaptation) => void;
};

export function CreateAdaptationPage({ category, datasources, onNext }: CreateAdaptationPageProps) {
    const banner = useBannerState('create-adaptation-page');
    const [ isFetching, setIsFetching ] = useState(false);

    async function createAdaptation() {
        setIsFetching(true);
        const response = await api.adaptations.createAdaptationForCategory({ categoryId: category.id });
        setIsFetching(false);
        if (!response.status) {
            toast.error('Failed to start adaptation');
            return;
        }

        const adaptation = Adaptation.fromResponse(response.data, datasources);
        onNext(adaptation);
    }

    return (
        <PageLayout>
            <div className='flex items-center gap-2 mb-4'>
                <h1 className='text-xl font-bold text-default-800'>Adaptation</h1>

                <InfoTooltip {...banner} />
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <CreateAdaptationInfoInner />
            </InfoBanner>

            <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
                <div className='max-md:none' />

                <FeatureCard
                    Icon={ArrowPathIcon}
                    colorClass='bg-primary-100 text-primary-600'
                    title='Start Adaptation'
                    description='Begin the adaptation process for the selected category using available datasources.'
                    button={props => (
                        <SpinnerButton color='primary' onPress={createAdaptation} isFetching={isFetching} {...props}>
                            Start Now
                        </SpinnerButton>
                    )}
                />
            </div>
        </PageLayout>
    );
}

function CreateAdaptationInfoInner() {
    return (<>
        <h2>Understanding Adaptation</h2>

        <p>
            Adaptation is an automated process that searches for a better multi-model configuration of your schema and queries.
            It explores alternative mappings of entities and relationships to available datasources and estimates how each option
            affects performance and cost.
        </p>

        <ul>
            <li>
                <span className='font-bold'>Search-based optimization:</span> Uses MCTS to explore many mapping alternatives efficiently.
            </li>
            <li>
                <span className='font-bold'>Workload-aware:</span> Evaluates solutions based on your queries and their execution frequencies.
            </li>
            <li>
                <span className='font-bold'>Cost vs. performance:</span> Each candidate mapping is scored by expected speed-up and transformation price.
            </li>
        </ul>

        <p>
            The result is a ranked set of recommended configurations that align your schema with the capabilities of your datasources.
        </p>
    </>);
}
