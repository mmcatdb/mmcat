import { PageLayout } from '@/components/RootLayout';
import { Button, Card, CardBody } from '@heroui/react';
import { DatasourceBadge } from '@/components/datasource/DatasourceBadge';
import { type Adaptation, type AdaptationResult, type AdaptationSolution } from '@/components/adaptation/adaptation';
import { type Objex, type Category } from '@/types/schema';
import { cn } from '../common/utils';
import { useMemo, useState } from 'react';
import { categoryToKindGraph } from './kindGraph';
import { useKindGraph } from './useKindGraph';
import { KindGraphDisplay } from './KindGraphDisplay';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { dataSizeQuantity, plural, prettyPrintDouble, prettyPrintInt } from '@/types/utils/common';
import { type Query } from '@/types/query';
import { QueriesTable } from '../querying/QueriesTable';

type AdaptationResultPageProps = {
    category: Category;
    adaptation: Adaptation;
    result: AdaptationResult;
    queries: Query[];
};

export function AdaptationResultPage({ category, adaptation, result, queries }: AdaptationResultPageProps) {
    const [ selectedSolution, setSelectedSolution ] = useState<AdaptationSolution>();
    const [ isShowExcluded, setIsShowExcluded ] = useState(false);

    const { kinds, includedCount, excludedCount } = useMemo(() => {
        const all = category.getObjexes().filter(o => o.isEntity)
            .sort((a, b) => a.key.value - b.key.value);
        // Let's assume that a kind has datasource iff it had one in the adaptation settings.
        const included = all.filter(o => adaptation.settings.objexes.get(o.key)?.datasource);
        const excluded = isShowExcluded ? all.filter(o => !adaptation.settings.objexes.get(o.key)?.datasource) : [];

        return {
            kinds: [
                ...included,
                ...excluded,
            ] satisfies Objex[],
            includedCount: included.length,
            excludedCount: all.length - included.length,
        };
    }, [ category, adaptation, isShowExcluded ]);

    return (
        <PageLayout className='space-y-2'>
            <h1 className='text-xl font-semibold'>Adaptation</h1>

            <h2 className='text-lg font-semibold'>Solutions</h2>

            <div className='flex justify-center gap-4'>
                <div>
                    <div className='py-3 flex flex-col gap-1'>
                        <div className='h-5 font-semibold'>Id</div>
                        <div className='h-5 font-semibold'>Speed up [<XMarkIcon className='inline size-4' />]</div>
                        <div className='h-5 font-semibold'>Price [DB hits]</div>
                    </div>

                    <div className='mt-3 py-3 flex flex-col gap-1'>
                        {kinds.map((kind, index) => (
                            <div key={kind.key.value} className={cn('leading-6 font-medium', index >= includedCount && 'text-foreground-400')}>
                                {category.getObjex(kind.key).metadata.label}
                            </div>
                        ))}
                    </div>
                </div>

                <AdaptationSolutionColumn
                    adaptation={adaptation}
                    kinds={kinds}
                    isSelected={!selectedSolution}
                    onClick={() => setSelectedSolution(undefined)}
                />

                {result.solutions.map((solution, index) => (
                    <AdaptationSolutionColumn
                        key={index}
                        adaptation={adaptation}
                        kinds={kinds}
                        solution={solution}
                        isSelected={selectedSolution === solution}
                        onClick={() => setSelectedSolution(solution)}
                    />
                ))}
            </div>

            {excludedCount > 0 && (
                <div className='flex items-center justify-center gap-2'>
                    <div className='italic'>
                        {`${excludedCount} ${plural('kind', excludedCount)} was excluded from the adaptation.`}
                    </div>

                    <Button size='sm' variant='ghost' onPress={() => setIsShowExcluded(!isShowExcluded)}>
                        {isShowExcluded ? 'Hide' : 'Show'}
                    </Button>
                </div>
            )}

            <h2 className='mt-4 text-lg font-semibold'>{selectedSolution ? `Solution #${selectedSolution.id}` : 'Original'} Graph</h2>
            <AdaptationSolutionGraph category={category} adaptation={adaptation} solution={selectedSolution} />

            <h2 className='mt-4 text-lg font-semibold'>{selectedSolution ? `Solution #${selectedSolution.id}` : 'Original'} Queries</h2>
            <QueriesTable queries={queries} solution={selectedSolution} />
        </PageLayout>
    );
}

type AdaptationSolutionColumnProps = {
    /** Sorted objexes that should be displayed. */
    kinds: Objex[];
    adaptation: Adaptation;
    solution?: AdaptationSolution;
    isSelected: boolean;
    onClick?: () => void;
};

function AdaptationSolutionColumn({ kinds, adaptation, solution, isSelected, onClick }: AdaptationSolutionColumnProps) {
    const objexes = solution?.objexes ?? adaptation.settings.objexes;

    return (
        <div>
            <Card className={cn('w-full', !solution && 'bg-canvas')}>
                <CardBody className='flex flex-col items-end gap-1 font-semibold [&>*]:h-5'>
                    {solution ? (<>
                        <div>#{solution.id}</div>
                        {/* TODO Maybe this should be int? */}
                        <div>{prettyPrintDouble(solution.speedup)}</div>
                        <div>{prettyPrintDouble(solution.price)}</div>
                    </>) : (<>
                        <div>Original</div>
                        <div>{1}</div>
                        <div>{0}</div>
                    </>)}
                </CardBody>
            </Card>

            <div className='h-3' />

            <button onClick={onClick} className='w-full group'>
                <Card className={cn(
                    !solution && 'bg-canvas',
                    onClick && !isSelected && 'cursor-pointer shadow-primary-500 hover:shadow-[0_0_20px_0_rgba(0,0,0,0.3)] group-active:shadow-primary-400',
                    isSelected && 'outline-2 outline-primary',
                )}>
                    <CardBody className='flex flex-col items-center gap-1'>
                        {kinds.map(k => {
                            const kind = objexes.get(k.key);

                            return kind?.datasource ? (
                                <DatasourceBadge key={kind.key.value} type={kind.datasource.type} />
                            ) : (
                                <div key={k.key.value} className='h-6 italic'>
                                    None
                                </div>
                            );
                        })}
                    </CardBody>
                </Card>
            </button>
        </div>
    );
}

type AdaptationSolutionGraphProps = {
    category: Category;
    adaptation: Adaptation;
    solution: AdaptationSolution | undefined;
};

function AdaptationSolutionGraph({ category, adaptation, solution }: AdaptationSolutionGraphProps) {
    const graph = useMemo(() => {
        const objexes = solution?.objexes ?? adaptation.settings.objexes;
        return categoryToKindGraph(category, objex => objexes.get(objex.key)?.datasource);
    }, [ category, adaptation, solution ]);
    const { selection, dispatch } = useKindGraph();

    const selectedNode = selection?.firstNodeId ? graph.nodes.get(selection.firstNodeId) : undefined;

    // FIXME Use real values.
    // TODO Some of these properties might be undefined if the DB doesn't support it (or if it would be too much pain to implement).
    const tempDataSizeInBytes = Math.round(Math.random() * 29483553);
    const tempRecordCount = Math.round(tempDataSizeInBytes / 150);

    return (
        <div className='grid grid-cols-4 gap-4'>
            <Card className='col-span-3'>
                <KindGraphDisplay graph={graph} selection={selection} dispatch={dispatch} className='h-[300px]' />
            </Card>

            <Card className='p-4'>
                {selectedNode ? (<>
                    <h3 className='mb-2 text-lg font-semibold'>{selectedNode.objex.metadata.label}</h3>

                    {selectedNode.datasource && (
                        <div className='mb-2 flex items-center gap-2'>
                            <DatasourceBadge type={selectedNode.datasource.type} />

                            {/*
                                TODO There was the previous datasource
                            {selectedNode.adaptation && (<>
                                <ArrowLongRightIcon className='size-5' />
                                <DatasourceBadge type={selectedNode.adaptation.type} />
                            </>)} */}
                        </div>
                    )}

                    <div className='text-sm font-semibold text-foreground-400'>Data size</div>
                    <div>{dataSizeQuantity.prettyPrint(tempDataSizeInBytes)}</div>
                    <div>{prettyPrintInt(tempRecordCount)} records</div>

                    <div className='mt-2 text-sm font-semibold text-foreground-400'>Properties</div>
                    <ul className='pl-5 list-disc'>
                        {[ ...selectedNode.properties.values() ].map(value => (
                            <li key={value.key.toString()} className=''>
                                {value.metadata.label}
                            </li>
                        ))}
                    </ul>
                </>) : (
                    <div>Select a kind to see its details.</div>
                )}
            </Card>
        </div>
    );
}
