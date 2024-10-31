import { useReducer } from 'react';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { KindMenu } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { reducer } from '@/components/adminer/reducer';
import { View } from '@/types/adminer/View';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    const [ state, dispatch ] = useReducer(reducer, {
        form: { limit: 50, filters: [] },
        active: { limit: 50, filters: [] },
        view: View.table,
    });

    return (
        <div>
            <div className='mt-5'>
                <DatasourceMenu datasource={state.datasource} dispatch={dispatch}/>

                {state.datasource &&
                (
                    <>
                        <div className='mt-5'>
                            <KindMenu apiUrl={`${BACKEND_API_URL}/adminer/${state.datasource.id}`} kindName={state.kind} dispatch={dispatch}/>
                        </div>

                        <div className='mt-5'>
                            <ViewMenu datasourceType={state.datasource.type} view={state.view} dispatch={dispatch}/>
                        </div>

                        {state.kind && (
                            <div className='mt-5'>
                                <FilterForm state={state} dispatch={dispatch}/>
                            </div>
                        )}

                        <div className='mt-5'>
                            {typeof state.kind === 'string' && (
                                <DatabaseView apiUrl={`${BACKEND_API_URL}/adminer`} state={state}/>
                            )}
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}
