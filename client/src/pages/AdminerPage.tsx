import { useState, useEffect } from 'react';
import { CommonPage } from '@/components/CommonPage';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { TableMenu } from '@/components/adminer/TableMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { LimitForm } from '@/components/adminer/LimitForm';
import { ColumnForm } from '@/components/adminer/ColumnForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import type { Datasource } from '@/types/datasource';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';
import { View } from '@/types/adminer/View';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ tableName, setTableName ] = useState<string>();
    const [ filters, setFilters ] = useState<ColumnFilter[]>();
    const [ limit, setLimit ] = useState<number>(50);
    const [ view, setView ] = useState<View>(View.table);

    useEffect(() => {
        setTableName(undefined);
        setFilters(undefined);
    }, [ datasource ]);

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
                            <>
                                <div className='mt-5'>
                                    <LimitForm limit={limit} setLimit={setLimit}/>
                                </div>

                                <div className='mt-5'>
                                    <ColumnForm actualFilter={{ columnName:'', columnValue:'', operator:Operator.eq }} filters={filters} setFilters={setFilters} />
                                </div>

                                {filters?.map((filter, index) => (
                                    <div key={index} className='mt-5'>
                                        <ColumnForm actualFilter={filter} filters={filters} setFilters={setFilters} />
                                    </div>
                                ))}
                            </>
                        )}

                        <div className='mt-5'>
                            {typeof tableName === 'string' && (
                                <DatabaseView apiUrl={`${BACKEND_API_URL}/adminer/${datasource.id}/${tableName}`} filters={filters} datasourceType={datasource.type} limit={limit} view={view}/>
                            )}
                        </div>
                    </>
                )}
            </div>
        </CommonPage>
    );
}
