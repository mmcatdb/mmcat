import { useEffect, useReducer, useState, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Spinner } from '@nextui-org/react';
import { getStateFromURLParams, getURLParamsFromState } from '@/components/adminer/URLParamsState';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { KindMenu } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { reducer } from '@/components/adminer/reducer';
import { api } from '@/api';
import { View } from '@/types/adminer/View';
import { type Datasource, DatasourceType } from '@/types/datasource';

export function AdminerPage() {
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ state, dispatch ] = useReducer(reducer, {
        form: { limit: 50, filters: [] },
        active: { limit: 50, filters: [] },
        view: View.table,
    });
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ allDatasources, setAllDatasources ] = useState<Datasource[]>();

    useMemo(() => {
        const stateFromParams = getStateFromURLParams(searchParams);

        if (stateFromParams.active !== state.active || stateFromParams.datasourceId !== state.datasourceId || stateFromParams.kindName !== state.kindName || stateFromParams.view !== state.view)
            dispatch({ type: 'initialize', state: stateFromParams });
    }, [ searchParams ]);

    useMemo(() => {
        const params = getURLParamsFromState(state);

        if (params !== searchParams)
            setSearchParams(params);
    }, [ state ]);

    useEffect(() => {
        (async () => {
            try {
                const response = await api.datasources.getAllDatasources({});

                if (response.status && response.data)
                    setAllDatasources(response.data);
            }
            catch {
                console.error('Failed to load datasources data');
            }
        })();
    }, []);

    useMemo(() => {
        setDatasource(allDatasources?.find((source) => source.id === state.datasourceId));
    }, [ state.datasourceId, allDatasources ]);

    return (
        <div>
            <div className='mt-5 flex flex-wrap gap-3 items-center'>

                {allDatasources ? (
                    <DatasourceMenu dispatch={dispatch} datasourceId={state.datasourceId} datasources={allDatasources}/>
                ) : (
                    <div className='h-10 flex items-center justify-center'>
                        <Spinner />
                    </div>
                )}

                {datasource &&
                (
                    <>
                        <KindMenu datasourceId={datasource.id} kind={state.kindName} showUnlabeled={datasource.type === DatasourceType.neo4j} dispatch={dispatch}/>

                        {state.kindName !== undefined && (
                            <ViewMenu datasourceType={datasource.type} view={state.view} dispatch={dispatch}/>
                        )}
                    </>
                )}
            </div>

            {datasource && state.kindName && (
                <div className='mt-5'>
                    <div className='mt-5'>
                        <FilterForm state={state} dispatch={dispatch}/>
                    </div>

                    {typeof state.kindName === 'string' && (
                        <div className='mt-5'>
                            <DatabaseView state={state}/>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
