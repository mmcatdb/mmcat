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
import { dataSizeQuantity, plural, prettyPrintDouble, prettyPrintInt } from '@/types/utils/common';
import { type Query } from '@/types/query';
import { QueriesTable } from '../querying/QueriesTable';
import { InfoBanner, InfoTooltip } from '../common/components';
import { useBannerState } from '@/types/utils/useBannerState';
import { ArrowPathIcon } from '@heroicons/react/24/solid';
import { api } from '@/api';
import { ArrowLeftIcon } from '@heroicons/react/20/solid';

type AdaptationResultPageProps = {
    category: Category;
    adaptation: Adaptation;
    result: AdaptationResult;
    queries: Query[];
    onResume: () => void;
    onRestart: () => void;
};

export function AdaptationResultPage({ category, adaptation, result, queries, onResume, onRestart }: AdaptationResultPageProps) {
    const banner = useBannerState('adaptation-result-page');

    const [ selectedSolution, setSelectedSolution ] = useState<AdaptationSolution>();
    const [ isShowExcluded, setIsShowExcluded ] = useState(false);

    const { kinds, includedCount, excludedCount } = useMemo(() => {
        const all = category.getObjexes().filter(o => o.isEntity)
            .sort((a, b) => a.key.value - b.key.value);
        // Let's assume that a kind has datasource iff it had one in the adaptation settings.
        const included = all.filter(o => adaptation.settings.objexes.get(o.key)?.mappings.length);
        const excluded = isShowExcluded ? all.filter(o => !adaptation.settings.objexes.get(o.key)?.mappings.length) : [];

        return {
            kinds: [
                ...included,
                ...excluded,
            ] satisfies Objex[],
            includedCount: included.length,
            excludedCount: all.length - included.length,
        };
    }, [ category, adaptation, isShowExcluded ]);

    async function restart() {
        const response = await api.adaptations.stopAdaptation({ adaptationId: adaptation.id });
        if (!response.status) {
            // TODO handle error
            console.error(response.error);
            return;
        }
        onRestart();
    }

    function acceptSolution() {
        // TODO
    }

    return (
        <PageLayout className='space-y-2'>
            <div className='flex items-center gap-2 mb-4'>
                <h1 className='text-xl font-bold text-default-800'>Adaptation</h1>

                <InfoTooltip {...banner} />
            </div>

            <InfoBanner {...banner} className='mb-6'>
                <AdaptationResultInfoInner />
            </InfoBanner>

            <h2 className='text-lg font-semibold'>Solutions</h2>

            <div className='flex justify-center gap-4'>
                <div>
                    <div className='py-3 flex flex-col gap-1'>
                        <div className='h-5 font-semibold'>Id</div>
                        <div className='h-5 font-semibold'>Speed-up</div>
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
                        {`${excludedCount} ${plural(excludedCount, 'kind was', 'kinds were')} excluded from the adaptation.`}
                    </div>

                    <Button size='sm' variant='ghost' onPress={() => setIsShowExcluded(!isShowExcluded)}>
                        {isShowExcluded ? 'Hide' : 'Show'}
                    </Button>
                </div>
            )}

            <h2 className='mt-4 text-lg font-semibold'>{selectedSolution ? `Solution #${selectedSolution.id}` : 'Original'} Graph</h2>
            <AdaptationSolutionGraph category={category} adaptation={adaptation} solution={selectedSolution} />

            <h2 className='mt-4 text-lg font-semibold'>{selectedSolution ? `Solution #${selectedSolution.id}` : 'Original'} Queries</h2>
            <QueriesTable queries={queries} solution={selectedSolution} itemsPerPage={5} />

            <div className='mt-4 flex justify-end gap-2'>
                <Button onPress={onResume}>
                    <ArrowLeftIcon className='size-5' />
                    Back
                </Button>

                <Button color='danger' onPress={restart}>
                    <ArrowPathIcon className='size-5' />
                    Restart
                </Button>

                {/* <Button color='primary' onPress={acceptSolution} isDisabled={!selectedSolution}>
                    Accept Solution & Migrate
                </Button> */}
            </div>
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
                        <div>{prettyPrintDouble(100 * solution.speedup) + ' %'}</div>
                        {/* TODO Maybe this should be int? */}
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

                            return kind?.mappings.length ? (
                                <div key={kind.key.value}>
                                    {kind.mappings.map((mapping, index) => (
                                        <DatasourceBadge key={index} type={mapping.datasource.type} />
                                    ))}
                                </div>
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
    const { selection, dispatch } = useKindGraph();

    const { graph, selectedNode, objex } = useMemo(() => {
        const objexes = solution?.objexes ?? adaptation.settings.objexes;
        const datasorceGetter = (objex: Objex) => objexes.get(objex.key)?.mappings.map(m => m.datasource) ?? [];
        const graph = categoryToKindGraph(category, datasorceGetter);
        const selectedNode = selection?.firstNodeId ? graph.nodes.get(selection.firstNodeId) : undefined;
        const objex = selectedNode && solution?.objexes.get(selectedNode.objex.key);

        return { graph, selectedNode, objex };
    }, [ category, adaptation, solution, selection ]);

    return (
        <div className='grid grid-cols-4 gap-4'>
            <div className='col-span-3'>
                <Card>
                    <KindGraphDisplay graph={graph} selection={selection} dispatch={dispatch} className='h-[600px]' />
                </Card>
            </div>

            <Card className='p-4'>
                {selectedNode ? (<>
                    <h3 className='text-lg font-semibold'>{selectedNode.objex.metadata.label}</h3>

                    {objex?.mapping && (<>
                        <div className='mt-2 flex items-center gap-2'>
                            <DatasourceBadge type={objex.mapping.datasource.type} />

                            {/*
                                TODO There was the previous datasource
                            {selectedNode.adaptation && (<>
                                <ArrowLongRightIcon className='size-5' />
                                <DatasourceBadge type={selectedNode.adaptation.type} />
                            </>)} */}
                        </div>

                        {(objex.mapping.dataSizeInBytes || objex.mapping.recordCount) && (<>
                            <div className='mt-2 text-sm font-semibold text-foreground-400'>Data size</div>
                            {objex.mapping.dataSizeInBytes && (
                                <div>{dataSizeQuantity.prettyPrint(objex.mapping.dataSizeInBytes)}</div>
                            )}
                            {objex.mapping.recordCount && (
                                <div>{prettyPrintInt(objex.mapping.recordCount)} records</div>
                            )}
                        </>)}
                    </>)}

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

function AdaptationResultInfoInner() {
    return (<>
        <h2>Results & Comparison</h2>

        <p>
            Compare the recommended mappings side-by-side. Each column corresponds to a solution (except the first one, which shows the original state), showing per-kind mappings, estimated speed-up, and migration price. Speed-ups (-1, ∞) are relative to the original configuration.
        </p>

        <ul>
            <li>
                <span className='font-bold'>Table view:</span> Click a solution column to inspect it in the graph and query table.
            </li>
            <li>
                <span className='font-bold'>Graph view:</span> Shows which kinds are mapped to which datasources for the selected solution.
            </li>
            <li>
                <span className='font-bold'>Query table:</span> Query speed-ups are shown for each query under the chosen solution.
            </li>
        </ul>

        <p>
            Use the results to accept a solution and schedule migration, or rerun the search with adjusted settings.
        </p>
    </>);
}
