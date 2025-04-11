import { Button, Textarea } from '@nextui-org/react';
import { useEffect, useReducer, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getCustomQueryStateFromURLParams, getURLParamsFromCustomQueryState } from '@/components/adminer/URLParamsState';
import { api } from '@/api';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { customQueryReducer } from '@/components/adminer/customQueryReducer';
import { DatasourceType, type Datasource } from '@/types/datasource/Datasource';
import type { Id } from '@/types/id';
import type { DataResponse, DocumentResponse, ErrorResponse, GraphResponse, TableResponse } from '@/types/adminer/DataResponse';

async function handleOnPress(datasourceId: Id, query: string, setQueryResult: (queryResult: DataResponse|ErrorResponse) => void) {
    const queryResult = await api.adminer.getQueryResult({ datasourceId: datasourceId }, { query: query });

    if (queryResult.status) {
        setQueryResult(queryResult.data);
    }
    else {
        setQueryResult({ message: queryResult.error?.data
            ? String(queryResult.error.data)
            : `Failed to fetch query result` });
    }
}

type AdminerCustomQueryPageProps = Readonly<{
    datasource: Datasource;
    datasources: Datasource[];
}>;

export function AdminerCustomQueryPage({ datasource, datasources }: AdminerCustomQueryPageProps) {
    const [ queryResult, setQueryResult ] = useState<DataResponse | ErrorResponse>();
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ state, dispatch ] = useReducer(customQueryReducer, searchParams, getCustomQueryStateFromURLParams);
    const stateRef = useRef(state);
    const searchParamsRef = useRef(searchParams);

    useEffect(() => {
        if (state.datasourceId !== datasource.id)
            dispatch({ type:'datasource', newDatasource: datasource });

    }, [ datasource ]);

    // Sync state with URL search parameters
    useEffect(() => {
        if (searchParamsRef.current != searchParams) {
            dispatch({ type:'update', newState: getCustomQueryStateFromURLParams(searchParams) });
            searchParamsRef.current = searchParams;
        }
    }, [ searchParams ]);

    // Update URL search parameters whenever state changes
    useEffect(() => {
        if (stateRef.current != state && searchParamsRef.current == searchParams) {
            setSearchParams(getURLParamsFromCustomQueryState(state));
            stateRef.current = state;
        }
    }, [ state, searchParams ]);

    return (
        <div>
            <Textarea
                className='mt-4'
                minRows={5}
                maxRows={Infinity}
                label='Custom query'
                placeholder='Enter your query'
                value={state.query}
                onChange={e => dispatch({ type:'query', newQuery: e.target.value })}
                onKeyDown={async e => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        await handleOnPress(datasource.id, state.query, setQueryResult);
                    }
                }}
            />

            <Button
                className='mt-5 items-center gap-1 min-w-40'
                size='sm'
                aria-label='Execute query'
                type='submit'
                color='primary'
                onPress={() => handleOnPress(datasource.id, state.query, setQueryResult)}
            >
                EXECUTE QUERY
            </Button>

            <div className='mt-5'>
                {queryResult && 'message' in queryResult && (
                    <>{queryResult.message}</>
                )}

                {queryResult && 'data' in queryResult && (
                    <>
                        {datasource.type === DatasourceType.postgresql ? (
                            <DatabaseTable fetchedData={queryResult as TableResponse} kindReferences={[]} kind={''} datasourceId={datasource.id} datasources={datasources}/>
                        ) : (
                            <DatabaseDocument fetchedData={queryResult as DocumentResponse | GraphResponse} kindReferences={[]} kind={''} datasourceId={datasource.id} datasources={datasources}/>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}
