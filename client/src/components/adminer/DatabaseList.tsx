import { useFetchObjectData } from '@/components/adminer/useFetchObjectData';
import { ListComponent } from '@/components/adminer/ListComponent';
import { Spinner } from '@nextui-org/react';

type DatabaseListProps = Readonly<{
    apiUrl: string;
}>;

export function DatabaseList({ apiUrl }: DatabaseListProps) {
    const { fetchedData, loading, error } = useFetchObjectData(apiUrl);

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
