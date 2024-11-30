import { useState, useEffect } from 'react';
import { type Result } from '@/types/api/result';

type FetchFunction<T> = () => Promise<Result<T>>;

type FetchResult<T> = {
    fetchedData: T;
    loading: boolean;
    error: undefined;
} | {
    fetchedData: undefined;
    loading: boolean;
    error: string;
};

export function useFetchData<T>( fetchFunction: FetchFunction<T> ): FetchResult<T> {
    const [ result, setResult ] = useState<FetchResult<T>>({
        fetchedData: undefined,
        loading: true,
        error: `No fetched data`,
    });

    useEffect(() => {
        (async () => {
            setResult({
                fetchedData: undefined,
                loading: true,
                error: `No fetched data`,
            });

            const response = await fetchFunction();

            if (!response.status) {
                setResult({
                    fetchedData: undefined,
                    loading: false,
                    error: `Failed to fetch data`,
                });
            }

            if ('data' in response) {
                setResult({
                    fetchedData: response.data,
                    loading: false,
                    error: undefined,
                });
            }
        })();
    }, [ fetchFunction ]);

    return result;
}
