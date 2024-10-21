import { useEffect } from 'react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { Spinner } from '@nextui-org/react';
import { useFetchData } from './useFetchData';

type DatabaseDocumentProps = Readonly<{
    apiUrl: string;
    setRowCount: (rowCount: number | undefined) => void;
}>;

export function DatabaseDocument({ apiUrl, setRowCount }: DatabaseDocumentProps) {
    const { fetchedData, loading, error } = useFetchData(apiUrl);

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
