import { useState, useEffect } from 'react';
import { CommonPage } from '@/components/CommonPage';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { TableMenu } from '@/components/adminer/TableMenu';
import { LimitForm } from '@/components/adminer/LimitForm';
import { ColumnForm } from '@/components/adminer/ColumnForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import type { Datasource } from '@/types/datasource';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ tableName, setTableName ] = useState<string>();
    const [ filters, setFilters ] = useState<ColumnFilter[]>();
    const [ limit, setLimit ] = useState<number>(50);

    useEffect(() => {
        setTableName(undefined);
    }, [ datasource ]);

    return (
        <CommonPage>
            <div className='mt-5' style={{ fontSize: '14px' }}>
                <DatasourceMenu datasource={datasource} setDatasource={setDatasource}/>
            </div>

            {datasource &&
            (
                <>
                    <div className='mt-5' style={{ fontSize: '14px' }}>
                        <TableMenu apiUrl={`${BACKEND_API_URL}/adminer/${datasource.id}`} tableName={tableName} setTableName={setTableName}/>
                    </div>

                    {tableName && (
                        <>
                            <div className='mt-5' style={{ fontSize: '14px' }}>
                                <LimitForm limit={limit} setLimit={setLimit}/>
                            </div>

                            <div className='mt-5' style={{ fontSize: '14px' }}>
                                <ColumnForm actualFilter={{ columnName:'', columnValue:'', operator:Operator.eq }} filters={filters} setFilters={setFilters} />
                            </div>

                            {filters?.map((filter, index) => (
                                <div key={index} className='mt-5' style={{ fontSize: '14px' }}>
                                    <ColumnForm actualFilter={filter} filters={filters} setFilters={setFilters} />
                                </div>
                            ))}
                        </>
                    )}

                    <div className='mt-5' style={{ fontSize: '14px' }}>
                        {typeof tableName === 'string' && (
                            <DatabaseView apiUrl={`${BACKEND_API_URL}/adminer/${datasource.id}/${tableName}`} filters={filters} datasourceType={datasource.type} limit={limit}/>
                        )}
                    </div>
                </>
            )}
        </CommonPage>
    );
}
