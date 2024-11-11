import { useReducer } from 'react';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { KindMenu } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { reducer } from '@/components/adminer/reducer';
import { View } from '@/types/adminer/View';
import { DatasourceType } from '@/types/datasource';

export function AdminerPage() {
    const [ state, dispatch ] = useReducer(reducer, {
        form: { limit: 50, filters: [] },
        active: { limit: 50, filters: [] },
        view: View.table,
    });

    return (
        <div>
            <div className='mt-5 flex flex-wrap gap-3 items-center'>

                <DatasourceMenu dispatch={dispatch}/>

                {state.datasource &&
                (
                    <>
                        <KindMenu datasourceId={state.datasource.id} showUnlabeled={state.datasource.type === DatasourceType.neo4j} dispatch={dispatch}/>

                        <ViewMenu datasourceType={state.datasource.type} dispatch={dispatch}/>
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

                    <div className='mt-5'>
                        {typeof state.kind === 'string' && (
                            <DatabaseView state={state}/>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
