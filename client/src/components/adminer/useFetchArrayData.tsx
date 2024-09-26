import { useState, useEffect } from 'react';
import type { BackendArrayResponse } from '@/types/adminer/BackendResponse';

export function useFetchArrayData(url: string) {
    const [ fetchedData, setFetchedData ] = useState<BackendArrayResponse | null>(null);
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            try {
                setLoading(true);
                setError(null);

                const response = await fetch(url);

                if (!response.ok)
                    throw new Error(`Failed to fetch data from ${url}`);


                const data = await response.json() as BackendArrayResponse;
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
    }, [ url ]);

    return { fetchedData, loading, error };
}
