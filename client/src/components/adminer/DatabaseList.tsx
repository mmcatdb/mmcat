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
        return <p style={{ fontSize: '14px' }}>{error}</p>;
    }

    return (
        <div style={{ fontSize: '14px' }}>
            {data.length > 0 ? (
                <ListComponent value={data} depth={0}/>
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
};
