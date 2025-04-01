import { useEffect, useState } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Button, Textarea } from '@nextui-org/react';
import { api } from '@/api';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { DatasourceType, type Datasource } from '@/types/datasource/Datasource';
import type { Id } from '@/types/id';
import type { DataResponse, DocumentResponse, ErrorResponse, GraphResponse, TableResponse } from '@/types/adminer/DataResponse';

async function handleOnPress(datasourceId: Id, query: string, setQueryResult: (queryResult: DataResponse|ErrorResponse) => void) {
    const queryResult = await api.adminer.getQueryResult({ datasourceId: datasourceId }, { query: query });

    if (queryResult.status) {
        setQueryResult(queryResult.data);
    }
    else {
        setQueryResult({ message: queryResult.error?.data
            ? String(queryResult.error.data)
            : `Failed to fetch query result` });
    }
}

export function AdminerCustomQueryPage() {
    const allDatasources = useLoaderData() as Datasource[];
    const [ searchParams ] = useSearchParams();
    const [ query, setQuery ] = useState('');
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ queryResult, setQueryResult ] = useState<DataResponse | ErrorResponse>();

    useEffect(() => {
        const datasourceId = searchParams.get('datasourceId');

        if (!datasource || datasourceId != datasource.id)
            setDatasource(allDatasources?.find(source => source.id === datasourceId));

    }, [ searchParams ]);

    if (!datasource){
        return (
            <div>
                Datasource not found.
            </div>
        );
    }

    return (
        <div>
            <Textarea
                minRows={5}
                maxRows={Infinity}
                label='Custom query'
                placeholder='Enter your query'
                value={query}
                onChange={e => setQuery(e.target.value)}
                onKeyDown={async e => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        await handleOnPress(datasource.id, query, setQueryResult);
                    }
                }}
            />

            <Button
                className='mt-5 items-center gap-1 min-w-40'
                size='sm'
                aria-label='Execute query'
                type='submit'
                color='primary'
                onPress={() => handleOnPress(datasource.id, query, setQueryResult)}
            >
                EXECUTE QUERY
            </Button>

            <div className='mt-5'>
                {queryResult && 'message' in queryResult && (
                    <>{queryResult.message}</>
                )}

                {queryResult && 'data' in queryResult && (
                    <>
                        {datasource.type === DatasourceType.postgresql ? (
                            <DatabaseTable fetchedData={queryResult as TableResponse} kindReferences={[]} kind={''} datasourceId={datasource.id} datasources={allDatasources}/>
                        ) : (
                            <DatabaseDocument fetchedData={queryResult as DocumentResponse | GraphResponse} kindReferences={[]} kind={''} datasourceId={datasource.id} datasources={allDatasources}/>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}
