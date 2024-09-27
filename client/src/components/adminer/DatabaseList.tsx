import { useEffect } from 'react';
import { useFetchObjectData } from '@/components/adminer/useFetchObjectData';
import { ListComponent } from '@/components/adminer/ListComponent';
import { Spinner } from '@nextui-org/react';

type DatabaseListProps = Readonly<{
    apiUrl: string;
    setRowCount: (rowCount: number | undefined) => void;
}>;

export function DatabaseList({ apiUrl, setRowCount }: DatabaseListProps) {
    const { fetchedData, loading, error } = useFetchObjectData(apiUrl);

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
                <ListComponent value={fetchedData.data} depth={0}/>
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
}
