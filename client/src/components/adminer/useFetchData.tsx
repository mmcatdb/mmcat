import { type BackendDocumentResponse, type BackendGraphResponse, type BackendTableResponse } from '@/types/adminer/BackendResponse';
import { useState, useEffect } from 'react';

type BackendResponse = BackendTableResponse | BackendDocumentResponse | BackendGraphResponse;

export function useFetchData<T extends BackendResponse>(url: string) {
    const [ fetchedData, setFetchedData ] = useState<T | null>(null);
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


                const data = await response.json() as T;

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
