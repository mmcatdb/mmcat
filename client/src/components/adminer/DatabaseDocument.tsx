import { useEffect } from 'react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { Spinner } from '@nextui-org/react';
import type { BackendTableResponse, BackendDocumentResponse, BackendGraphResponse } from '@/types/adminer/BackendResponse';

type DatabaseDocumentProps = Readonly<{
    apiUrl: string;
    fetchData: (url: string) => {
        fetchedData: BackendTableResponse | BackendDocumentResponse | BackendGraphResponse | null;
        loading: boolean;
        error: string | null;
    };
    setRowCount: (rowCount: number | undefined) => void;
}>;

export function DatabaseDocument({ apiUrl, fetchData, setRowCount }: DatabaseDocumentProps) {
    const { fetchedData, loading, error } = fetchData(apiUrl);

    useEffect(() => {
        setRowCount(fetchedData?.metadata.rowCount);
    }, [ fetchedData, setRowCount ]);

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;


    return (
        <div>
            {fetchedData !== null && fetchedData.data.length > 0 ? (
                <DocumentComponent value={fetchedData.data} depth={0}/>
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
}
