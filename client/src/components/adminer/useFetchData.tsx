import { useState, useEffect } from 'react';
import { api } from '@/api';
import type { FetchParams } from '@/types/adminer/FetchParams';
import type { BackendResponse } from '@/types/adminer/BackendResponse';

export function useFetchData<T extends BackendResponse>( params: FetchParams ) {
    const [ fetchedData, setFetchedData ] = useState<T | undefined>();
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | undefined>();

    useEffect(() => {
        (async () => {
            try {
                setLoading(true);
                setError(undefined);

                const response = await ('kindId' in params
                    ? api.adminer.getKind({ datasourceId: params.datasourceId, kindId: params.kindId }, params.queryParams)
                    : api.adminer.getKindNames({ datasourceId: params.datasourceId })).then(data => data);

                if (!response.status)
                    throw new Error(`Failed to fetch data`);

                const data = await response.data as T;

                setFetchedData(data);
            }
            catch (err) {
                if (err instanceof Error)
                    setError(err.message);

                else
                    setError('Failed to load data');

            }
            finally {
                setLoading(false);
            }
        })();
    }, [ params ]);

    return { fetchedData, loading, error };
}
