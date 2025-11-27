import { PageLayout } from '@/components/RootLayout';
import { type Datasource } from '@/types/Datasource';
import { type Mapping } from '@/types/mapping';
import { type Category } from '@/types/schema';
import { useMemo } from 'react';
import { mockAdaptationInput } from './adaptation';
import { AdaptationGraph } from './AdaptationGraph';
import { Button } from '@heroui/react';

type CreateAdaptationPageProps = {
    category: Category;
    datasources: Datasource[];
    mappings: Mapping[];
    onNext: () => void;
};

export function CreateAdaptationPage({ category, datasources, mappings, onNext }: CreateAdaptationPageProps) {
    const input = useMemo(() => mockAdaptationInput(datasources), [ datasources ]);

    return (
        <PageLayout className='space-y-2'>
            <h1 className='text-xl font-semibold'>New Adaptation</h1>

            <div className='flex gap-4'>
                <div>Datasources: {datasources.length}</div>
                <div>Kinds: {mappings.length}</div>
            </div>

            <h2 className='text-lg font-semibold'>Graph view</h2>

            <AdaptationGraph category={category} adaptation={input} />

            <div className='flex justify-end'>
                <Button color='primary' onPress={onNext}>
                    Next
                </Button>
            </div>
        </PageLayout>
    );
}
