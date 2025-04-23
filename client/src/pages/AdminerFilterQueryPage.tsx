import { useEffect, useReducer, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getFilterQueryStateFromURLParams, getURLParamsFromFilterQueryState } from '@/components/adminer/URLParamsState';
import { KindMenu } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { filterQueryReducer } from '@/components/adminer/filterQueryReducer';
import { type Datasource, DatasourceType } from '@/types/datasource';

type AdminerFilterQueryPageProps = Readonly<{
    datasource: Datasource;
    datasources: Datasource[];
}>;

export function AdminerFilterQueryPage({ datasource, datasources }: AdminerFilterQueryPageProps) {
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ state, dispatch ] = useReducer(filterQueryReducer, searchParams, getFilterQueryStateFromURLParams);
    const stateRef = useRef(state);
    const searchParamsRef = useRef(searchParams);

    useEffect(() => {
        if (state.datasourceId !== datasource.id)
            dispatch({ type:'datasource', newDatasource: datasource });
    }, [ datasource ]);

    // Sync state with URL search parameters
    useEffect(() => {
        if (searchParamsRef.current != searchParams) {
            dispatch({ type:'update', newState: getFilterQueryStateFromURLParams(searchParams) });
            searchParamsRef.current = searchParams;
        }
    }, [ searchParams ]);

    // Update URL search parameters whenever state changes
    useEffect(() => {
        if (stateRef.current != state && searchParamsRef.current == searchParams) {
            setSearchParams(getURLParamsFromFilterQueryState(state));
            stateRef.current = state;
        }
    }, [ state, searchParams ]);

    return (
        <>
            <div className='flex mt-1'>
                <KindMenu datasourceId={datasource.id} kind={state.kindName} showUnlabeled={datasource.type === DatasourceType.neo4j} dispatch={dispatch}/>

                {state.kindName !== undefined && (
                    <ViewMenu datasourceType={datasource.type} view={state.view} dispatch={dispatch}/>
                )}
            </div>

            {datasource && state.kindName && typeof state.kindName === 'string' && (
                <DatabaseView state={state} datasources={datasources} dispatch={dispatch}/>
            )}
        </>
    );
}
