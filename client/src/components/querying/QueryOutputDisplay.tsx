import { type Dispatch, type Key, useEffect, useRef, useState } from 'react';
import { type QueryPartDescription, type QueryDescription, type QueryResult, type QueryStats, type Query, type AggregatedNumber } from '@/types/query';
import { Button, Tab, Tabs } from '@heroui/react';
import { cn } from '../utils';
import { QueryTreeDisplay } from './QueryTreeDisplay';
import { type Datasource } from '@/types/Datasource';
import { type QueryOutputFetched } from './QueryDisplay';
import { CopyToClipboardButton } from '../CopyToClipboardButton';
import { dataSizeQuantity, prettyPrintNumber, type Quantity, timeQuantity } from '@/types/utils/common';
import { QueryStatsForm } from './QueryStatsForm';
import { type Id } from '@/types/id';

type QueryOutputDisplayProps = {
    query: Query | undefined;
    queryString: string;
    datasources: Datasource[];
    result: QueryOutputFetched<QueryResult> | undefined;
    description: QueryOutputFetched<QueryDescription> | undefined;
    stats: QueryStats | undefined;
    setStats: Dispatch<QueryStats>;
};

export function QueryOutputDisplay({ query, queryString, datasources, result, description, stats, setStats }: QueryOutputDisplayProps) {
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
                        <QueryStatsDisplay queryId={query?.id} stats={stats} setStats={setStats} />
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
    queryId: Id | undefined;
    stats: QueryStats;
    setStats: Dispatch<QueryStats>;
};

function QueryStatsDisplay({ queryId, stats, setStats }: QueryStatsDisplayProps) {
    const [ isUpdating, setIsUpdating ] = useState(false);

    function statsUpdated(newStats?: QueryStats) {
        setIsUpdating(false);
        if (newStats)
            setStats(newStats);
    }

    return (<>
        <h3 className='mb-1 text-lg font-semibold'>Aggregated stats</h3>

        {isUpdating ? (
            <QueryStatsForm queryId={queryId!} stats={stats} onCancel={() => setIsUpdating(false)} onSuccess={statsUpdated} />
        ) : (
            <div className='w-fit grid grid-cols-4 gap-x-2 gap-y-2'>
                <div className='mt-1 col-span-4'>
                    <div className='text-sm font-semibold text-foreground-400'>Executions</div>
                    <div>{prettyPrintNumber(stats.executionCount)}</div>
                </div>

                {renderStatsRow(dataSizeQuantity, stats.executionCount, stats.resultSizeInBytes, 'Result size')}
                {renderStatsRow(timeQuantity, stats.executionCount, stats.planningTimeInMs, 'Planning time')}
                {renderStatsRow(timeQuantity, stats.executionCount, stats.evaluationTimeInMs, 'Evaluation time')}

                {queryId && (
                    <div>
                        <Button onPress={() => setIsUpdating(true)} className='mt-1'>
                            Edit stats
                        </Button>
                    </div>
                )}
            </div>
        )}
    </>);
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
        <div className='-mb-2 col-span-4 text-sm font-semibold text-foreground-400'>{label}</div>

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
