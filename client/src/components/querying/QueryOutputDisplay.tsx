import { type Dispatch, type Key, useEffect, useState } from 'react';
import { type QueryPartDescription, type QueryDescription, type QueryResult } from '@/types/query';
import { Tab, Tabs } from '@heroui/react';
import { cn } from '../utils';
import { QueryTreeDisplay } from './QueryTreeDisplay';
import { type Datasource } from '@/types/Datasource';
import { type QueryOutputFetched } from './QueryDisplay';
import { CopyToClipboardButton } from '../CopyToClipboardButton';

type QueryOutputDisplayProps = {
    result: QueryOutputFetched<QueryResult> | undefined;
    description: QueryOutputFetched<QueryDescription> | undefined;
    queryString: string;
    datasources: Datasource[];
};

export function QueryOutputDisplay({ result, description, queryString, datasources }: QueryOutputDisplayProps) {
    const [ selected, setSelected ] = useState('result');

    useEffect(() => {
        setSelected('result');
    }, [ result ]);

    useEffect(() => {
        setSelected('plan-optimized');
    }, [ description ]);

    if (!result && !description)
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

                    <Tab key='plan-original' title='Original plan' className='py-0 space-y-2'>
                        <QueryPlanDisplay type='planned' fetched={description} datasources={datasources} />
                    </Tab>
                </>)}
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
