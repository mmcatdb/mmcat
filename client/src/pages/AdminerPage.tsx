import { useState, useEffect } from 'react';
import { CommonPage } from '@/components/CommonPage';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { TableMenu } from '@/components/adminer/TableMenu';
import { ColumnForm } from '@/components/adminer/ColumnForm';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import type { Datasource } from '@/types/datasource';
import type { ColumnFilter } from '@/types/adminer/ColumnFilter';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ tableName, setTableName ] = useState<string>();
    const [ filter, setFilter ] = useState<ColumnFilter>();

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
                        <div className='mt-5' style={{ fontSize: '14px' }}>
                            <ColumnForm setFilter={setFilter}/>
                        </div>
                    )}

                    <div className='mt-5' style={{ fontSize: '14px' }}>
                        {typeof tableName !== 'string' ? (
                            <span>No table selected.</span>

                        ) : (
                            <DatabaseView apiUrl={`${BACKEND_API_URL}/adminer/${datasource.id}/${tableName}`} datasourceType={datasource.type} />
                        )}
                    </div>
                </>
            )}
        </CommonPage>
    );
}
