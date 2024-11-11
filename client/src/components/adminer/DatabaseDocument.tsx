import { useEffect } from 'react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { Spinner } from '@nextui-org/react';
import type { FetchKindParams } from '@/types/adminer/FetchParams';
import { useFetchData } from './useFetchData';

type DatabaseDocumentProps = Readonly<{
    urlParams: FetchKindParams;
    setRowCount: (rowCount: number | undefined) => void;
}>;

export function DatabaseDocument({ urlParams, setRowCount }: DatabaseDocumentProps) {
    const { fetchedData, loading, error } = useFetchData(urlParams);

    useEffect(() => {
        setRowCount(fetchedData?.metadata.rowCount);
    }, [ fetchedData, setRowCount ]);

    if (loading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;


    return (
        <div>
            {fetchedData && fetchedData.data.length > 0 ? (
                <DocumentComponent value={fetchedData.data} depth={0}/>
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
}
