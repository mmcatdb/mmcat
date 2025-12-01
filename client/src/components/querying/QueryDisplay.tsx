import { api } from '@/api';
import { Query } from '@/types/query';
import { type Datasource } from '@/types/Datasource';
import { Button, Checkbox, Input, Textarea } from '@heroui/react';
import { useEffect, useState } from 'react';
import { useLoaderData, useNavigate, useRevalidator } from 'react-router-dom';
import { QueryOutputDisplay } from './QueryOutputDisplay';
import { routes } from '@/routes/routes';
import { globalCache } from '../hooks/useGlobalCache';
import { type PullResult } from '@/types/api/routes';
import { type Id } from '@/types/id';
import { SpinnerButton } from '../common';
import { QueryExampleSelect } from './QueryExampleSelect';
import { useCategoryInfo } from '../CategoryInfoProvider';

type QueryDisplayProps = {
    query: Query;
    otherWeights: number;
} | {
    defaultQueryString: string | undefined;
    onOutput: (queryString: string) => void;
};

export function QueryDisplay(props: QueryDisplayProps) {
    const { query, otherWeights, defaultQueryString, onOutput } = props as Partial<{
        query: Query;
        otherWeights: number;
        defaultQueryString: string;
        onOutput: (queryString: string) => void;
    }>;

    const { category } = useCategoryInfo();
    const { datasources } = useLoaderData() as { datasources: Datasource[] };

    const [ queryString, setQueryString ] = useState<string>(query?.content ?? defaultQueryString ?? '');

    useEffect(() => {
        if (defaultQueryString !== undefined)
            setQueryString(defaultQueryString);
    }, [ defaultQueryString ]);

    const resultOutput = useQueryOutput('result', query?.id, queryString => {
        onOutput?.(queryString);
        return api.queries.execute({}, { categoryId: category.id, queryId: query?.id, queryString });
    });

    const descriptionOutput = useQueryOutput('description', query?.id, queryString => {
        onOutput?.(queryString);
        return api.queries.describe({}, { categoryId: category.id, queryId: query?.id, queryString });
    });

    const revalidator = useRevalidator();
    const navigate = useNavigate();
    const [ stats, setStats ] = useState(query?.stats);

    const fetchedStats = (resultOutput.fetched && 'data' in resultOutput.fetched ? resultOutput.fetched.data.stats : undefined);
    useEffect(() => {
        if (fetchedStats) {
            setStats(fetchedStats);
            // TODO This is extremely inefficient - we have to fetch everything for the route again ... just because react router devs are absolute morons who just can't add a simple setLoaderData function ...
            revalidator.revalidate();
        }
    }, [ fetchedStats ]);

    function onUpdate(newQuery: Query) {
        // TODO something like setLoaderData?
        void newQuery;
        revalidator.revalidate();
        setStats(newQuery.stats);
    }

    function onCreate(newQuery: Query) {
        // Save current state to cache so that it can be restored on the query detail page.
        resultOutput.cache(newQuery.id);
        descriptionOutput.cache(newQuery.id);
        navigate(routes.category.queries.detail.resolve({ categoryId: category.id, queryId: newQuery.id }));
    }

    return (<>
        <Textarea
            className='font-mono'
            placeholder='Enter your query ...'
            value={queryString}
            onChange={e => setQueryString(e.target.value)}
            disableAnimation
            maxRows={Infinity}
        />

        <div className='flex items-center gap-2'>
            <SpinnerButton color='primary' onPress={() => resultOutput.fetch(queryString)} isFetching={resultOutput.isFetching}>
                Execute
            </SpinnerButton>

            <SpinnerButton color='primary' onPress={() => descriptionOutput.fetch(queryString)} isFetching={descriptionOutput.isFetching}>
                Describe
            </SpinnerButton>

            {!query && (
                <QueryExampleSelect queryString={queryString} onSelect={setQueryString} />
            )}

            <div className='grow' />

            {query ? (
                <UpdateQueryContentButton query={query} content={queryString} onUpdate={onUpdate} />
            ) : (
                <CreateQueryButton content={queryString} onCreate={onCreate} />
            )}
        </div>

        <QueryOutputDisplay
            query={query}
            queryString={queryString}
            datasources={datasources}
            result={resultOutput.fetched}
            description={descriptionOutput.fetched}
            stats={stats}
            otherWeights={otherWeights}
        />
    </>);
}

type QueryOutput<TData> = QueryOutputState<TData> & {
    fetch: (queryString: string) => Promise<void>;
    cache: (queryId: Id) => void;
};

type QueryOutputState<TData> = {
    isFetching: boolean;
    fetched?: QueryOutputFetched<TData>;
};

export type QueryOutputFetched<TData> = {
    /** The input used to fetch this data. */
    queryString: string;
} & ({
    data: TData;
} | {
    error: unknown;
});

function useQueryOutput<TData>(name: string, queryId: Id | undefined, fetcher: (queryString: string) => PullResult<TData>): QueryOutput<TData> {
    const [ state, setState ] = useState(getOutputInitialState<TData>(name, queryId));

    async function fetchOutput(queryString: string) {
        setState(prev => ({ ...prev, isFetching: true }));
        const response = await fetcher(queryString);
        setState({
            isFetching: false,
            fetched: response.status ? {
                queryString,
                data: (response as { data: TData }).data,
            } : {
                queryString,
                error: response.error,
            },
        });
    }

    function cacheOutput(queryId: Id) {
        globalCache.set(getOutputCacheKey(name, queryId), state);
    }

    return {
        ...state,
        fetch: fetchOutput,
        cache: cacheOutput,
    };
}

function getOutputInitialState<TData>(name: string, queryId: Id | undefined): QueryOutputState<TData> {
    const defaultState: QueryOutputState<TData> = { isFetching: false };
    if (!queryId)
        return defaultState;

    // Restore cached state when coming from the NewQueryPage.
    const cacheKey = getOutputCacheKey(name, queryId);
    const cached = globalCache.get<QueryOutputState<TData>>(cacheKey);
    if (!cached)
        return defaultState;

    globalCache.set(cacheKey, undefined);

    return cached;
}

function getOutputCacheKey(name: string, queryId: Id): string {
    return `${queryId}-${name}`;
}

type CreateQueryButtonProps = {
    content: string;
    onCreate: (query: Query) => void;
};

function CreateQueryButton({ content, onCreate }: CreateQueryButtonProps) {
    const { category } = useCategoryInfo();
    const [ phase, setPhase ] = useState(CreatePhase.default);
    const [ label, setLabel ] = useState('');

    async function save() {
        setPhase(CreatePhase.fetching);
        const response = await api.queries.createQuery({}, {
            categoryId: category.id,
            label,
            content,
        });
        setPhase(CreatePhase.default);
        if (!response.status) {
            // TODO error
            return;
        }

        onCreate(Query.fromResponse(response.data));
    }

    function cancelSave() {
        setPhase(CreatePhase.default);
        setLabel('');
    }

    return (
        <div className='flex items-center gap-2'>
            {phase === CreatePhase.default ? (
                <Button color='success' onPress={() => setPhase(CreatePhase.label)} isDisabled={!isValidQueryContent(content)}>
                    Save
                </Button>
            ) : (<>
                <Input
                    value={label}
                    onChange={e => setLabel(e.target.value)}
                    placeholder='Query label'
                    autoFocus
                />

                <SpinnerButton color='success' onPress={save} isFetching={phase === CreatePhase.fetching} isDisabled={!isValidQueryContent(content)}>
                    Save
                </SpinnerButton>

                <Button onPress={cancelSave} isDisabled={phase === CreatePhase.fetching}>
                    Cancel
                </Button>
            </>)}
        </div>
    );
}

enum CreatePhase {
    default = 'default',
    label = 'label',
    fetching = 'fetching',
}

type UpdateQueryContentButtonProps = {
    query: Query;
    content: string;
    onUpdate: (query: Query) => void;
};

function UpdateQueryContentButton({ query, content, onUpdate }: UpdateQueryContentButtonProps) {
    const [ isResetStats, setIsResetStats ] = useState(false);
    const [ isFetching, setIsFetching ] = useState(false);

    const isEnabled = content !== query.content && isValidQueryContent(content);

    useEffect(() => {
        if (isEnabled)
            setIsResetStats(false);
    }, [ isEnabled ]);

    async function save() {
        setIsFetching(true);
        const response = await api.queries.updateQueryContent({ queryId: query.id }, {
            content,
            // TODO fix the errors
            // TODO enable editing label - maybe in a separate field that is saved on blur?
            errors: [],
            isResetStats,
        });
        setIsFetching(false);
        if (!response.status) {
            // TODO error
            return;
        }

        onUpdate(Query.fromResponse(response.data));
    }

    return (
        <div className='flex items-center gap-4'>
            {isEnabled && (
                <Checkbox
                    isSelected={isResetStats}
                    onValueChange={setIsResetStats}
                >
                    Clear stats?
                </Checkbox>
            )}

            <SpinnerButton onPress={save} isFetching={isFetching} isDisabled={!isEnabled} color='success'>
                Save
            </SpinnerButton>
        </div>
    );
}

function isValidQueryContent(content: string): boolean {
    return content.trim().length > 0;
}
