import { useEffect, useState } from 'react';
import { useLoaderData, useSearchParams } from 'react-router-dom';
import { Button, Textarea } from '@nextui-org/react';
import { api } from '@/api';
import type { Id } from '@/types/id';
import type { Datasource } from '@/types/datasource/Datasource';
import type { DataResponse } from '@/types/adminer/DataResponse';

async function handleOnPress(datasourceId: Id, query: string, setQueryResult: (queryResult: DataResponse) => void) {
    const queryResult = await api.adminer.getQueryResult({ datasourceId: datasourceId }, { query: query });

    if (queryResult.status)
        setQueryResult(queryResult.data);
    else
        throw new Error(`Failed to fetch query result`);
}

export function AdminerCustomQueryPage() {
    const allDatasources = useLoaderData() as Datasource[];
    const [ searchParams ] = useSearchParams();
    const [ query, setQuery ] = useState('');
    const [ datasource, setDatasource ] = useState<Datasource>();
    const [ queryResult, setQueryResult ] = useState<DataResponse>();

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
        </div>
    );
}
