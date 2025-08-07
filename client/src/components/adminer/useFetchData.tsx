import { useState, useEffect } from 'react';
import { type Result } from '@/types/api/result';

type FetchFunction<T> = () => Promise<Result<T>>;

type FetchResult<T> = {
    fetchedData: T;
    loading: false;
    error: undefined;
} | {
    fetchedData: undefined;
    loading: true;
    error: string;
} | {
    fetchedData: undefined;
    loading: false;
    error: undefined;
};

/**
 * Fetches all data
 */
export function useFetchData<T>( fetchFunction: FetchFunction<T> ): FetchResult<T> {
    const [ result, setResult ] = useState<FetchResult<T>>({
        fetchedData: undefined,
        loading: true,
        error: `No fetched data`,
    });

    useEffect(() => {
        void (async () => {
            const response = await fetchFunction();

            if (!response.status && !response.error) {
                setResult({
                    fetchedData: undefined,
                    loading: false,
                    error: undefined,
                });
                return;
            }

            if (!response.status) {
                setResult({
                    fetchedData: undefined,
                    loading: true,
                    error: `Failed to fetch data`,
                });
                return;
            }

            if ('data' in response) {
                setResult({
                    fetchedData: response.data,
                    loading: false,
                    error: undefined,
                });
                return;
            }

            setResult({
                fetchedData: undefined,
                loading: true,
                error: `No fetched data`,
            });
        })();
    }, [ fetchFunction ]);

    return result;
}
