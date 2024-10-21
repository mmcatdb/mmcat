import { useState, useEffect, useReducer } from 'react';
import { CommonPage } from '@/components/CommonPage';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { TableMenu } from '@/components/adminer/TableMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { reducer } from '@/components/adminer/reducer';
import type { Datasource } from '@/types/datasource';
import { View } from '@/types/adminer/View';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ tableName, setTableName ] = useState<string>();
    const [ view, setView ] = useState<View>(View.table);
    const [ state, dispatch ] = useReducer(reducer, { submitted: { limit: 50, filters: [] }, new: { limit: 50, filters: [] } });

    useEffect(() => {
        setTableName(undefined);
        dispatch({ type:'delete_filters' });
    }, [ datasource ]);

    useEffect(() => {
        dispatch({ type:'delete_filters' });
    }, [ tableName ]);

    return (
        <CommonPage>
            <div className='mt-5 text-sm'>
                <DatasourceMenu datasource={datasource} setDatasource={setDatasource}/>

                {datasource &&
                (
                    <>
                        <div className='mt-5'>
                            <TableMenu apiUrl={`${BACKEND_API_URL}/adminer/${datasource.id}`} tableName={tableName} setTableName={setTableName}/>
                        </div>

                        <div className='mt-5'>
                            <ViewMenu datasourceType={datasource.type} view={view} setView={setView}/>
                        </div>

                        {tableName && (
                            <div className='mt-5'>
                                <FilterForm state={state} dispatch={dispatch}/>
                            </div>
                        )}

                        <div className='mt-5'>
                            {typeof tableName === 'string' && (
                                <DatabaseView apiUrl={`${BACKEND_API_URL}/adminer`} datasourceId={datasource.id} tableName={tableName} datasourceType={datasource.type} state={state} view={view}/>
                            )}
                        </div>
                    </>
                )}
            </div>
        </CommonPage>
    );
}
