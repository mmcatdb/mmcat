import { useState, useEffect } from 'react';

export const useFetchData = (url: string) => {
    const [data, setData] = useState<any[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const response = await fetch(url);

                if (!response.ok) {
                    throw new Error(`Failed to fetch data from ${url}`);
                }

                const data = await response.json();
                setData(data);
            } catch (err) {
                if (err instanceof Error) {
                    setError(err.message);
                }
                else {
                    setError('Failed to load data')
                }
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [url]);

    return { data, loading, error };
};
