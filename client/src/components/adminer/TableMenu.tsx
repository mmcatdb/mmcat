import { useFetchArrayData } from '@/components/adminer/useFetchArrayData';
import { Spinner, ButtonGroup, Button } from '@nextui-org/react';

type TableMenuProps = Readonly<{
    apiUrl: string;
    tableName: string | undefined;
    setTableName: (tableName: string) => void;
}>;

export function TableMenu({ apiUrl, tableName, setTableName }: TableMenuProps) {
    const { fetchedData, loading, error } = useFetchArrayData(apiUrl);

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
                <ButtonGroup
                    style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        width: '100%',
                    }}
                >
                    {fetchedData.data.map((name, index) => (
                        <Button
                            key={index}
                            onPress={() => setTableName(name)}
                            color={tableName === name ? 'primary' : 'default'}
                            style={{
                                flex: 1,
                                minWidth: '50px',
                            }}
                        >
                            {name}
                        </Button>
                    ))}
                </ButtonGroup>
            ) : (
                <span>No tables to display.</span>
            )}
        </div>
    );
}
