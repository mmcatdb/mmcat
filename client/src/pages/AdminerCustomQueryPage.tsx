import type React from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { materialLight, materialDark } from '@uiw/codemirror-theme-material';
import { PostgreSQL, sql } from '@codemirror/lang-sql';
import { javascript } from '@codemirror/lang-javascript';
import { Button } from '@nextui-org/react';
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
import type { AdminerCustomQueryStateAction } from '@/types/adminer/ReducerTypes';
import type { Theme } from '@/components/PreferencesProvider';

async function handleOnPress(datasourceId: Id, query: string, setQueryResult: (queryResult: DataResponse|ErrorResponse) => void, dispatch: React.Dispatch<AdminerCustomQueryStateAction>) {
    dispatch({ type:'submit' });

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
    theme: Theme;
}>;

export function AdminerCustomQueryPage({ datasource, datasources, theme }: AdminerCustomQueryPageProps) {
    const [ executed, setExecuted ] = useState<boolean>(false);
    const [ queryResult, setQueryResult ] = useState<DataResponse | ErrorResponse>();
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ state, dispatch ] = useReducer(customQueryReducer, searchParams, getCustomQueryStateFromURLParams);
    const stateRef = useRef(state);
    const searchParamsRef = useRef(searchParams);

    const getLanguageExtension = (datasourceType: DatasourceType) => {
        switch (datasourceType) {
        case DatasourceType.postgresql:
            return sql({ dialect: PostgreSQL });
        case DatasourceType.mongodb:
            return javascript();
        default:
            return sql();
        }
    };

    useEffect(() => {
        const currentDatasource = datasource;
        const stateDatasourceId = state.datasourceId;
        const stateDatasource = datasources?.find(source => source.id === stateDatasourceId);

        if (stateDatasourceId !== currentDatasource.id) {
            dispatch({ type:'datasource', newDatasource: datasource });
            setQueryResult(undefined);
        }

        if (stateDatasource?.type !== currentDatasource.type)
            setQueryResult(undefined);

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

    useEffect(() => {
        async function callHandleOnPress() {
            await handleOnPress(datasource.id, state.unsubmittedQuery, setQueryResult, dispatch);
        }

        if (executed === true) {
            callHandleOnPress();
            setExecuted(false);
        }
    }, [ executed ]);

    return (
        <div
            className='mt-4'
        >
            <CodeMirror
                value={state.unsubmittedQuery}
                onChange={value => dispatch({ type:'query', newQuery: value })}
                extensions={[ getLanguageExtension(datasources?.find(source => source.id === state.datasourceId)!.type) ]}
                theme={theme === 'light' ? materialLight : materialDark}
                onKeyDown={e => {
                    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                        e.preventDefault();
                        setExecuted(true);
                    }
                }}
            />

            <Button
                className='mt-5 items-center gap-1 min-w-40'
                size='sm'
                aria-label='Execute query'
                type='submit'
                color='primary'
                onPress={() => setExecuted(true)}
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
