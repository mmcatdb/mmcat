import { useFetchData } from '@/components/adminer/useFetchData';
import { ListComponent } from '@/components/adminer/ListComponent';
import { Spinner } from '@nextui-org/react';

type DatabaseListProps = {
    apiUrl: string;
};

export const DatabaseList: React.FC<DatabaseListProps> = ({ apiUrl }) => {
    const { data, loading, error } = useFetchData(apiUrl);

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div>
            {data.length > 0 ? (
                <ListComponent value={data} />
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
};
