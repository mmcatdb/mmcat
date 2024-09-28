import { useFetchTableData } from '@/components/adminer/useFetchTableData';
import { Spinner, Button } from '@nextui-org/react';

type TableMenuProps = Readonly<{
    apiUrl: string;
    tableName: string | undefined;
    setTableName: (tableName: string) => void;
}>;

export function TableMenu({ apiUrl, tableName, setTableName }: TableMenuProps) {
    const { fetchedData, loading, error } = useFetchTableData(apiUrl);

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
        <div className='flex flex-wrap gap-3 items-center'>
            {fetchedData !== null && fetchedData.data.length > 0 ? (
                fetchedData.data.map((name, index) => (
                    <Button
                        key={index}
                        onPress={() => setTableName(name)}
                        color={tableName === name ? 'primary' : 'default'}
                        variant={tableName === name ? 'solid' : 'ghost'}
                        className='flex-1 min-w-[50px]'
                    >
                        <span className='truncate'>{name}</span>
                    </Button>
                ))
            ) : (
                <span>No tables to display.</span>
            )}
        </div>
    );
}
