import { useEffect, useReducer, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getStateFromURLParams, getURLParamsFromState } from '@/components/adminer/URLParamsState';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { KindMenu } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { reducer } from '@/components/adminer/reducer';
import { View } from '@/types/adminer/View';
import { DatasourceType } from '@/types/datasource';

export function AdminerPage() {
    const [ initialized, setInitialized ] = useState(false);
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ state, dispatch ] = useReducer(reducer, {
        form: { limit: 50, filters: [] },
        active: { limit: 50, filters: [] },
        view: View.table,
    });

    useEffect(() => {
        const getState = async () => {
            const stateFromParams = await getStateFromURLParams(searchParams);

            dispatch({ type: 'initialize', state: stateFromParams });
        };

        getState()
            .catch(() => console.error('Error getting state from URL parameters.'))
            .finally(() => setInitialized(true));
    }, [ searchParams ]);

    useEffect(() => {
        if (!initialized)
            return;

        const params = getURLParamsFromState(state);

        setSearchParams(params);
    }, [ state, initialized, setSearchParams ]);

    return (
        <div>
            <div className='mt-5 flex flex-wrap gap-3 items-center'>

                <DatasourceMenu dispatch={dispatch} datasource={state.datasource}/>

                {state.datasource &&
                (
                    <>
                        <KindMenu datasourceId={state.datasource.id} kind={state.kind} showUnlabeled={state.datasource.type === DatasourceType.neo4j} dispatch={dispatch}/>

                        {state.kind !== undefined && (
                            <ViewMenu datasourceType={state.datasource.type} view={state.view} dispatch={dispatch}/>
                        )}
                    </>
                )}
            </div>

            {state.datasource && (
                <div className='mt-5'>
                    {state.kind && (
                        <div className='mt-5'>
                            <FilterForm state={state} dispatch={dispatch}/>
                        </div>
                    )}

                    {typeof state.kind === 'string' && (
                        <div className='mt-5'>
                            <DatabaseView state={state}/>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
