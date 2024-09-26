import { useState, useEffect } from 'react';
import { CommonPage } from '@/components/CommonPage';
import { DatasourceMenu } from '@/components/adminer/DatasourceMenu';
import { TableMenu } from '@/components/adminer/TableMenu';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseList } from '@/components/adminer/DatabaseList';
import { DatasourceType } from '@/types/datasource';
import type { Datasource } from '@/types/datasource';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

export function AdminerPage() {
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ tableName, setTableName ] = useState<string>();

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

                    {datasource.type === DatasourceType.postgresql ? (
                        <div className='mt-5' style={{ fontSize: '14px' }}>
                            {typeof tableName === 'string' ? (
                                <DatabaseTable apiUrl={`${BACKEND_API_URL}/adminer/${datasource.id}/${tableName}`} />
                            ) : (
                                <span>No table selected.</span>
                            )}
                        </div>
                    ) : (
                        <div className='mt-5' style={{ fontSize: '14px' }}>
                            {typeof tableName === 'string' ? (
                                <DatabaseList apiUrl={`${BACKEND_API_URL}/adminer/${datasource.id}/${tableName}`} />
                            ) : (
                                <span>No table selected.</span>
                            )}
                        </div>
                    )}
                </>
            )}
        </CommonPage>
    );
}
