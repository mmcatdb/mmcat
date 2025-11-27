import { useMemo } from 'react';
import { PageLayout } from '@/components/RootLayout';
import { type Datasource } from '@/types/Datasource';
import { type Mapping } from '@/types/mapping';
import { Card, CardBody } from '@heroui/react';
import { DatasourceBadge } from '@/components/datasource/DatasourceBadge';
import { type AdaptationInput, type AdaptationResult, mockAdaptationInput, mockAdaptationResults } from '@/components/adaptation/adaptation';
import { type Category } from '@/types/schema';
import { Key } from '@/types/identifiers';
import { AdaptationGraph } from './AdaptationGraph';
import { cn } from '../utils';

type AdaptationResultPageProps = {
    category: Category;
    datasources: Datasource[];
    mappings: Mapping[];
};

export function AdaptationResultPage({ category, datasources, mappings }: AdaptationResultPageProps) {
    const input = useMemo(() => mockAdaptationInput(datasources), [ datasources ]);
    const results = useMemo(() => mockAdaptationResults(datasources), [ datasources ]);

    return (
        <PageLayout className='space-y-2'>
            <h1 className='text-xl font-semibold'>Adaptation</h1>

            <div className='flex gap-4'>
                <div>Datasources: {datasources.length}</div>
                <div>Kinds: {mappings.length}</div>
            </div>

            <h2 className='text-lg font-semibold'>Table view</h2>

            <div className='flex justify-center gap-4'>
                <div className='py-3 flex flex-col gap-1'>
                    <div className='h-6' />

                    {input.kinds.map(kind => (
                        <div key={kind.key} className='leading-6 font-medium'>
                            {category.getObjex(Key.fromResponse(kind.key)).metadata.label}
                        </div>
                    ))}
                </div>

                <AdaptationResultColumn adaptation={input} />

                {results.map((result, index) => (
                    <AdaptationResultColumn key={index} adaptation={result} />
                ))}
            </div>

            <h2 className='text-lg font-semibold'>Graph view</h2>

            <div className='flex flex-col gap-4'>
                {results.map((result, index) => (
                    <AdaptationGraph key={index} category={category} adaptation={result} />
                ))}
            </div>
        </PageLayout>
    );
}

function AdaptationResultColumn({ adaptation }: { adaptation: AdaptationInput | AdaptationResult }) {
    const isInput = !('price' in adaptation);

    return (
        <Card className={cn(isInput && 'bg-canvas')}>
            <CardBody className='flex flex-col items-center gap-1'>
                <div className={cn('h-6 font-semibold', !isInput && 'self-end')}>
                    {'price' in adaptation ? adaptation.price : 'Original'}
                </div>

                {adaptation.kinds.map(kind => (
                    <DatasourceBadge key={kind.key} type={kind.kind?.type} />
                ))}
            </CardBody>
        </Card>
    );
}
