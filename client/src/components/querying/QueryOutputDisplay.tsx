import { type Dispatch, type Key, useEffect, useRef, useState } from 'react';
import { type QueryPartDescription, type QueryDescription, type QueryResult, type QueryStats, type Query, type AggregatedNumber } from '@/types/query';
import { Button, NumberInput, Tab, Tabs } from '@heroui/react';
import { cn } from '../utils';
import { QueryTreeDisplay } from './QueryTreeDisplay';
import { type Datasource } from '@/types/Datasource';
import { type QueryOutputFetched } from './QueryDisplay';
import { CopyToClipboardButton, SpinnerButton } from '../common';
import { dataSizeQuantity, prettyPrintDouble, prettyPrintInt, type Quantity, timeQuantity } from '@/types/utils/common';
import { PencilIcon } from '@heroicons/react/24/solid';
import { api } from '@/api';
import { toast } from 'react-toastify';

type QueryOutputDisplayProps = {
    query: Query | undefined;
    queryString: string;
    datasources: Datasource[];
    result: QueryOutputFetched<QueryResult> | undefined;
    description: QueryOutputFetched<QueryDescription> | undefined;
    stats: QueryStats | undefined;
    otherWeights: number | undefined;
};

export function QueryOutputDisplay({ query, queryString, datasources, result, description, stats, otherWeights }: QueryOutputDisplayProps) {
    const [ selected, setSelected ] = useState(result ? 'result' : description ? 'plan-optimized' : query ? 'stats' : 'result');
    const isResultRef = useRef(!!result);

    useEffect(() => {
        if (result) {
            // This ensures that when the user selects stats, he can keep re-executing the query without being forced back to the result tab.
            // Except for the first fetched result.
            const isFirstTime = !isResultRef.current;
            setSelected(prev => (isFirstTime ||  prev !== 'stats') ? 'result' : prev);
            isResultRef.current = true;
        }
    }, [ result ]);

    useEffect(() => {
        if (description)
            setSelected('plan-optimized');
    }, [ description ]);

    if (!result && !description && !stats)
        return null;

    return (
        <div className='grid grid-cols-[auto_1fr] gap-2'>
            <div className='order-1 pl-2 flex items-center'>
                {result && selected === 'result' && (
                    <OutdatedWarning fetched={result} queryString={queryString} />
                )}

                {description && (selected === 'plan-optimized' || selected === 'plan-original') && (
                    <OutdatedWarning fetched={description} queryString={queryString} />
                )}
            </div>

            <Tabs classNames={{ panel: 'order-1 col-span-2 px-0' }} selectedKey={selected} onSelectionChange={setSelected as Dispatch<Key>}>
                {result && (
                    <Tab key='result' title='Result' className='py-0'>
                        <ResultTabDisplay fetched={result} />
                    </Tab>
                )}

                {description && (<>
                    <Tab key='plan-optimized' title='Optimized plan' className='py-0'>
                        <QueryPlanDisplay type='optimized' fetched={description} datasources={datasources} />
                    </Tab>

                    <Tab key='plan-original' title='Original plan' className='py-0'>
                        <QueryPlanDisplay type='planned' fetched={description} datasources={datasources} />
                    </Tab>
                </>)}

                {stats && (
                    <Tab key='stats' title='Stats' className='py-0'>
                        {/* Should be defined at the same time stats are defined. */}
                        <QueryStatsDisplay query={query} stats={stats} otherWeights={otherWeights} />
                    </Tab>
                )}
            </Tabs>
        </div>
    );
}

type ResultTabDisplayProps = {
    fetched: QueryOutputFetched<QueryResult>;
};

function ResultTabDisplay({ fetched }: ResultTabDisplayProps) {
    if ('error' in fetched)
        return <ErrorDisplay error={fetched.error} />;

    const rows = fetched.data.rows;
    const indexWidth = getIndexWidth(rows.length);

    return (<>
        {rows.map((row, index) => (
            <div
                key={index}
                className={cn('px-2 py-1 flex odd:bg-default-100 even:bg-default-50 font-mono',
                    index === 0 && 'rounded-t-small',
                    index === rows.length - 1 && 'rounded-b-small',
                )}
            >
                <div className='shrink-0 text-gray-400 select-none mr-2' style={{ width: indexWidth }}>
                    {index}
                </div>
                <pre className='min-w-0 whitespace-pre-wrap break-words'>
                    {row}
                </pre>
            </div>
        ))}
    </>);
}

function getIndexWidth(rowsCount: number) {
    const numbers = String(rowsCount - 1).length;
    const widthInRem = numbers * 0.525 + 1/16;
    return `${widthInRem}rem`;
}

type QueryPlanDisplayProps = {
    type: keyof QueryDescription;
    fetched: QueryOutputFetched<QueryDescription>;
    datasources: Datasource[];
};

function QueryPlanDisplay({ type, fetched, datasources }: QueryPlanDisplayProps) {
    if ('error' in fetched)
        return <ErrorDisplay error={fetched.error} />;

    const plan = fetched.data[type];

    return (
        <div>
            <h2 className='mb-2 text-xl font-semibold'>Tree</h2>

            <QueryTreeDisplay datasources={datasources} tree={plan.tree} />

            <h2 className='mt-4 mb-2 text-xl font-semibold'>Parts</h2>

            <div className='space-y-4'>
                {plan.parts.map((part, index) => (
                    <QueryPartDisplay key={index} part={part} index={index} datasources={datasources} />
                ))}
            </div>
        </div>
    );
}

type QueryPartDisplayProps = {
    part: QueryPartDescription;
    index: number;
    datasources: Datasource[];
};

function QueryPartDisplay({ part, index, datasources }: QueryPartDisplayProps) {
    const datasource = datasources.find(d => d.id === part.datasourceIdentifier)!;

    const bgColor = index % 2 === 0 ? 'bg-default-50' : 'bg-default-100';

    return (
        <div>
            <h3 className='mb-1 text-lg font-semibold'>
                <span className='text-foreground-400 mr-1'>{`#${index}`}</span>
                {' '}
                {datasource.label}
            </h3>

            <div className='font-semibold text-foreground-400'>Structure:</div>
            <pre className={cn('px-2 py-1 rounded-small font-mono whitespace-pre-wrap break-words', bgColor)}>
                {JSON.stringify(part.structure, undefined, 4)}
            </pre>

            <div className='mt-2 font-semibold text-foreground-400'>Content:</div>
            <pre className={cn('px-2 py-1 rounded-small font-mono whitespace-pre-wrap break-words', bgColor)}>
                {part.content}
            </pre>
        </div>
    );
}

type QueryStatsDisplayProps = {
    query: Query | undefined;
    stats: QueryStats;
    otherWeights: number | undefined;
};

function QueryStatsDisplay({ query, stats, otherWeights }: QueryStatsDisplayProps) {
    return (
        <div className='space-y-2'>
            {query && (<>
                <h3 className='text-lg font-semibold'>Query weight</h3>

                <QueryWeightDisplay query={query} otherWeights={otherWeights!} />
            </>)}

            <h3 className='text-lg font-semibold'>Aggregated stats</h3>

            <div className=''>
                <div className='text-sm font-semibold text-foreground-400'>Executions</div>
                <div>{prettyPrintInt(stats.executionCount)}</div>
            </div>

            <div className='w-fit grid grid-cols-4 gap-x-2 gap-y-2'>
                {renderStatsRow(dataSizeQuantity, stats.executionCount, stats.resultSizeInBytes, 'Result size')}
                {renderStatsRow(timeQuantity, stats.executionCount, stats.planningTimeInMs, 'Planning time')}
                {renderStatsRow(timeQuantity, stats.executionCount, stats.evaluationTimeInMs, 'Evaluation time')}
            </div>
        </div>
    );
}

function renderStatsRow(quantity: Quantity, count: number, number: AggregatedNumber, label: string) {
    const title = (
        <div className='-mb-2 col-span-4 text-sm font-semibold text-foreground-400'>{label}</div>
    );

    const avgValue = number.sum / count;
    // Force min, avg, and max to use the same unit for better comparability.
    const unit = quantity.findUnit(avgValue).unit;
    const min = quantity.prettyPrint(number.min, unit);
    const avg = quantity.prettyPrint(avgValue, unit);
    const max = quantity.prettyPrint(number.max, unit);
    const sum = quantity.prettyPrint(number.sum);

    const isMinMaxSame = avg === min && avg === max;
    if (isMinMaxSame) {
        const isSumSame = avg === sum;
        if (isSumSame) {
            return (<>
                {title}
                <div className='col-span-4'>
                    {avg}
                </div>
            </>);
        }

        return (<>
            {title}
            {renderStatsCol('val:', avg, 'col-span-3')}
            {renderStatsCol('sum:', sum)}
        </>);
    }

    // Only force the decimal if we know that avg is not equal to min or max - i.e., it's surely not an integer.
    const avgDecimal = quantity.prettyPrint(avgValue, unit, false);

    return (<>
        {title}
        {renderStatsCol('min:', min)}
        {renderStatsCol('avg:', avgDecimal)}
        {renderStatsCol('max:', max)}
        {renderStatsCol('sum:', sum)}
    </>);
}

function renderStatsCol(label: string, value: string, className?: string) {
    return (
        <div className={className}>
            <span className='mr-2 font-mono text-sm/4 text-foreground-400'>{label}</span>
            <span>{value}</span>
        </div>
    );
}

type QueryWeightDisplayProps = {
    query: Query;
    otherWeights: number;
};

function QueryWeightDisplay({ query, otherWeights }: QueryWeightDisplayProps) {
    const [ weight, setWeight ] = useState(query.weight);
    const finalWeight = weight ?? query.stats?.executionCount ?? 0;

    const [ phase, setPhase ] = useState<'view' | 'edit' | 'fetch'>('view');
    const [ formWeight, setFormWeight ] = useState(weight ?? NaN);

    const isInvalid = formWeight === undefined || isNaN(formWeight);
    const finalFormWeight = isInvalid ? query.stats?.executionCount ?? 0 : formWeight;
    // This is not updated real-time because the `onValueChange` is only fired on blur.
    const displayedWeight = phase === 'view' ? finalWeight : finalFormWeight;
    const allWeights = otherWeights + displayedWeight;

    function edit() {
        setPhase('edit');
        setFormWeight(weight ?? NaN);
    }

    async function save() {
        setPhase('fetch');
        const response = await api.queries.updateQuery({ queryId: query.id }, isInvalid ? { isResetWeight: true } : { weight: formWeight });
        setPhase('view');

        if (!response.status) {
            toast.error(`Failed to update query weight: ${response.error}`);
            return;
        }

        toast.success('Query weight updated successfully.');
        setWeight(response.data.weight ?? undefined);
    }

    return (<>
        <div className='flex gap-2'>
            {phase !== 'view' ? (<>
                <NumberInput
                    hideStepper
                    isWheelDisabled
                    className='max-w-50'
                    classNames={{ label: 'text-sm font-semibold !text-foreground-400' }}
                    labelPlacement='outside'
                    label='Absolute weight'
                    placeholder='Automatic'
                    value={formWeight}
                    onValueChange={value => setFormWeight(value)}
                />

                <SpinnerButton color='success' className='self-end' onPress={save} isFetching={phase === 'fetch'}>
                    Save
                </SpinnerButton>

                <Button className='self-end' onPress={() => setPhase('view')} isDisabled={phase === 'fetch'}>
                    Cancel
                </Button>
            </>) : (<>
                <div className='min-w-32'>
                    <div className='text-sm font-semibold text-foreground-400'>Absolute weight</div>
                    {prettyPrintDouble(finalWeight)}
                    {weight === undefined && (
                        <span className='ml-2 text-sm text-foreground-400'>
                            (automatic)
                        </span>
                    )}
                </div>

                <div className='self-center'>
                    <Button isIconOnly size='sm' onPress={edit}>
                        <PencilIcon className='size-5' />
                    </Button>
                </div>
            </>)}
        </div>

        <div>
            <div className='text-sm font-semibold text-foreground-400'>Normalized weight</div>
            {prettyPrintDouble(displayedWeight / allWeights)}
        </div>
    </>);
}

type OutdatedWarningProps = {
    fetched: QueryOutputFetched<unknown>;
    queryString: string;
};

function OutdatedWarning({ fetched, queryString }: OutdatedWarningProps) {
    const isOutdated = fetched.queryString !== queryString;
    if (!isOutdated)
        return null;

    return (
        <div className='rounded-small text-warning-500'>
            The query changed since this data was fetched.{' '}
            <CopyToClipboardButton title={undefined} textToCopy={fetched.queryString} className='underline hover:text-warning-700'>
                Copy original query
            </CopyToClipboardButton>
            .
        </div>
    );
}

function ErrorDisplay({ error }: { error: unknown }) {
    return (
        <pre className='px-2 py-1 rounded-small border border-danger-400 bg-default-100 whitespace-pre-wrap break-words font-mono text-danger-400'>
            {JSON.stringify(error, undefined, 4)}
        </pre>
    );
}
